package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.horn.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class HornVerification {
    static void run(ServerLevel level, FakePlayer player) {
        var item = ModContent.COPPER_HORN.get();
        var horn = new ItemStack(item);
        check(!horn.hasFoil(), "Unlinked horn must not glint");
        var wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.COMMAND);
        var horse = EntityTypes.HORSE.create(level, EntitySpawnReason.COMMAND);
        var cow = EntityTypes.COW.create(level, EntitySpawnReason.COMMAND);
        check(wolf != null && horse != null && cow != null, "Entity creation");
        var origin = player.position();
        try {
            horse.setCustomName(net.minecraft.network.chat.Component.literal("Copper Mount"));
            wolf.setTame(true, true); wolf.setOwner(player); wolf.setOrderedToSit(true);
            wolf.setPos(origin.add(2, 0, 0)); level.addFreshEntity(wolf);
            horse.setTamed(true); horse.setOwner(player); horse.setPos(origin.add(2, 0, 0)); level.addFreshEntity(horse);
            check(!HornRecall.bind(player, horn, cow), "Non-mount must not link");
            check(!HornRecall.bind(player, horn, wolf), "Tamed pet must not link");
            var legacy = new ItemStack(item);
            CopperHornItem.link(legacy, wolf);
            player.setItemInHand(InteractionHand.MAIN_HAND, legacy);
            check(item.use(level, player, InteractionHand.MAIN_HAND).consumesAction()
                    && wolf.position().distanceToSqr(origin) > 1, "Previously linked pet is not recalled");
            player.stopUsingItem();
            player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(legacy));
            player.setItemInHand(InteractionHand.MAIN_HAND, horn);
            var interaction = new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, horse, net.minecraft.world.phys.Vec3.ZERO);
            HornRecall.interact(interaction);
            check(interaction.isCanceled() && interaction.getCancellationResult().consumesAction(), "Binding intercepts mount interaction");
            check(horse.getUUID().equals(CopperHornItem.target(horn)) && horn.hasFoil(), "Link and glint");
            var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            var saved = ItemStack.CODEC.encodeStart(ops, horn).getOrThrow();
            horn = ItemStack.CODEC.parse(ops, saved).getOrThrow();
            check(horse.getUUID().equals(CopperHornItem.target(horn)) && horn.hasFoil(), "Item save round trip");
            var tooltip = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            item.appendHoverText(horn, Item.TooltipContext.of(level), net.minecraft.world.item.component.TooltipDisplay.DEFAULT,
                    tooltip::add, TooltipFlag.NORMAL);
            check(tooltip.stream().anyMatch(line -> line.getString().equals("Call Copper Mount")), "Persisted linked-name tooltip");
            var memory = HornLinks.CODEC.encodeStart(JsonOps.INSTANCE, HornLinks.get(level.getServer())).getOrThrow();
            check(HornLinks.CODEC.parse(JsonOps.INSTANCE, memory).getOrThrow().find(horse.getUUID()) != null, "Unloaded location save round trip");
            int variants = 0;
            for (var instrument : level.registryAccess().lookupOrThrow(Registries.INSTRUMENT).listElements().toList()) {
                if (!instrument.is(net.minecraft.tags.InstrumentTags.GOAT_HORNS)) continue;
                var copper = InstrumentItem.create(item, instrument);
                var vanilla = InstrumentItem.create(Items.GOAT_HORN, instrument);
                check(item.getUseDuration(copper, player) == vanilla.getUseDuration(player)
                        && item.getUseAnimation(copper) == vanilla.getUseAnimation(), "Vanilla instrument behavior");
                variants++;
            }
            check(variants == 8, "All eight native horn sounds");
            player.setItemInHand(InteractionHand.MAIN_HAND, horn);
            player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(horn));
            check(item.use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "Horn plays");
            check(horse.position().distanceToSqr(player.position()) < .01, "Recall reaches caller");
            check(player.getCooldowns().isOnCooldown(horn), "Native horn cooldown");
            horse.setPos(origin.add(20, 0, 0));
            check(!item.use(level, player, InteractionHand.MAIN_HAND).consumesAction() && horse.position().distanceToSqr(origin) > 100, "Cooldown prevents repeated recall");
            check(HornRecall.recall(player, horse) && horse.position().distanceToSqr(origin) < .01, "Horse recall");
            var nether = level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            var away = horse.teleport(new net.minecraft.world.level.portal.TeleportTransition(nether, origin,
                    net.minecraft.world.phys.Vec3.ZERO, 0, 0, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
            check(away != null && away.level() == nether, "Native dimension travel");
            check(HornRecall.recall(player, away), "Cross-dimension recall");
            var returned = HornRecall.loaded(level.getServer(), horse.getUUID());
            check(returned != null && returned.level() == level && returned.position().distanceToSqr(origin) < .01, "Returned same UUID across dimensions");
            ((net.minecraft.world.entity.animal.equine.AbstractHorse) returned).setOwner(wolf);
            check(!HornRecall.bind(player, horn, returned) && !HornRecall.recall(player, returned), "Other player's mount protected");
            returned.discard();
            ChocolateFlavored.LOGGER.info("HORN_VERIFICATION_PASSED: eight native instruments, linking event, glint, item/location persistence, mount recall, pet rejection, ownership, cooldown");
        } finally {
            player.stopUsingItem(); player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(horn));
            wolf.discard(); horse.discard(); cow.discard();
        }
    }
    private static void check(boolean value, String message) { if (!value) throw new IllegalStateException("Horn: " + message); }
}
