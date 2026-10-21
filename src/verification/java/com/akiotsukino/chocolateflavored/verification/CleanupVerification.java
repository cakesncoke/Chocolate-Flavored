package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.animal.MuddyPig;
import com.akiotsukino.chocolateflavored.content.*;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Live behavior checks for the content cleanup, excluded from release builds. */
public final class CleanupVerification {
    public static void run(ServerLevel level, FakePlayer player) {
        for (String name : new String[]{"dirt_pile", "sand_pile", "red_sand_pile", "gravel_pile"})
            check(!BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("chocolateflavored", name)), "Removed pile registration: " + name);
        var slime = MobContent.TROPICAL_SLIME_BLOCK.get().defaultBlockState();
        check(slime.isStickyBlock(), "Tropical slime remains sticky");
        check(!slime.canStickTo(Blocks.SLIME_BLOCK.defaultBlockState()) && !slime.canStickTo(Blocks.HONEY_BLOCK.defaultBlockState()), "Separate sticky block families");
        check(slime.canStickTo(Blocks.STONE.defaultBlockState()), "Tropical slime moves ordinary blocks");
        var pos = new BlockPos(140, 170, 140);
        var drops = Block.getDrops(slime, level, pos, null, player, ItemStack.EMPTY);
        check(drops.size() == 1 && drops.getFirst().is(MobContent.TROPICAL_SLIME_BLOCK_ITEM.get()), "Tropical slime block drops itself");
        player.setAirSupply(0); player.removeAllEffects();
        var balls = new ItemStack(MobContent.TROPICAL_BALL.get(), 2);
        balls.finishUsingItem(level, player);
        check(balls.getCount() == 1 && player.getAirSupply() == player.getMaxAirSupply() && player.getEffect(MobEffects.WATER_BREATHING).getDuration() == 300, "One ball consumed; air and 15 seconds water breathing restored");
        var fish = EntityType.TROPICAL_FISH.create(level, EntitySpawnReason.COMMAND); fish.setAirSupply(0);
        MobContent.TROPICAL_BALL.get().interactLivingEntity(balls, player, fish, InteractionHand.MAIN_HAND);
        check(balls.isEmpty() && fish.getAirSupply() == fish.getMaxAirSupply(), "Ball rescues stranded aquatic animals");
        var rabbit = MobContent.HUSK_RABBIT.get().create(level, EntitySpawnReason.COMMAND);
        check(rabbit.getMaxHealth() == 8 && !rabbit.canBreed() && rabbit.isInvertedHealAndHarm(), "Hostile undead rabbit attributes and no breeding");
        rabbit.makeRider(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL);
        check(rabbit.getFirstPassenger() instanceof net.minecraft.world.entity.monster.zombie.Husk husk && husk.isBaby() && husk.getMainHandItem().is(Items.WOODEN_SWORD), "Baby Husk jockey with allowed equipment");
        var desert = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.DESERT).value();
        var desertSpawns = desert.getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
        long rabbitWeight = desertSpawns.stream().filter(e -> e.value().type() == MobContent.HUSK_RABBIT.get()).mapToLong(net.minecraft.util.random.Weighted::weight).sum();
        long creeperWeight = desertSpawns.stream().filter(e -> e.value().type() == EntityType.CREEPER).mapToLong(net.minecraft.util.random.Weighted::weight).sum();
        // The zombie-villager rule rescales all integer weights while preserving their ratios.
        check(rabbitWeight > 0 && rabbitWeight * 100 == creeperWeight * 15, "Reference desert spawn weight relative to vanilla creepers");
        check(SpawnEggContent.EGGS.size() == 21, "All prior eggs plus Tuff Golem and Sunken have eggs");
        for (var egg : SpawnEggContent.EGGS) check(SpawnEggItem.getType(egg.get().getDefaultInstance()) != null, "Valid spawn egg entity data");
        var muddyEgg = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("chocolateflavored", "muddy_pig_spawn_egg")).getDefaultInstance();
        level.getChunkAt(pos);
        var pig = EntityType.PIG.spawn(level, muddyEgg, player, pos, EntitySpawnReason.SPAWN_ITEM_USE, false, false);
        check(pig != null && ((MuddyPig)pig).chocolateflavored$isMuddy(), "Native egg entity data restores muddy pig state");
        pig.discard();
        // Vanilla Silk Touch and Fortune III branches must still take precedence over the 50% baseline.
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (var enchantment : java.util.List.of(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, net.minecraft.world.item.enchantment.Enchantments.FORTUNE)) {
            var tool = new ItemStack(Items.IRON_SHOVEL); tool.enchant(registry.getOrThrow(enchantment), enchantment == net.minecraft.world.item.enchantment.Enchantments.FORTUNE ? 3 : 1);
            for (int i = 0; i < 20; i++) {
                var gravel = Block.getDrops(Blocks.GRAVEL.defaultBlockState(), level, pos, null, player, tool);
                check(gravel.size() == 1 && gravel.getFirst().is(enchantment == net.minecraft.world.item.enchantment.Enchantments.FORTUNE ? Items.FLINT : Items.GRAVEL), "Gravel enchantment behavior");
            }
        }
        ChocolateFlavored.LOGGER.info("CLEANUP_VERIFICATION_PASSED: removed piles, tropical ball/block, Husk Rabbit jockey/desert spawns, all eggs and gravel enchantments");
    }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    private CleanupVerification() {}
}
