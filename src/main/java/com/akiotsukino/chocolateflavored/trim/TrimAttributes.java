package com.akiotsukino.chocolateflavored.trim;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;

@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class TrimAttributes {
    @SubscribeEvent public static void tick(PlayerTickEvent.Pre event) { if (!event.getEntity().level().isClientSide()) update(event.getEntity()); }
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent event) { if (event.getEntity() instanceof Player p && !p.level().isClientSide()) update(p); }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        var old = event.getOriginal().getPersistentData();
        var data = event.getEntity().getPersistentData();
        for (String ability : TrimTriggers.COOLDOWNS) data.putLong(TrimTriggers.key(ability), old.getLongOr(TrimTriggers.key(ability), 0));
        update(event.getEntity());
    }
    public static void update(Player p) {
        if(has(p,HEAD,"dune")){p.removeEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);p.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);}
        int ward = 0; for (var slot : ARMOR) if (has(p, slot, "ward")) ward++;
        modifier(p, Attributes.MAX_HEALTH, "ward", ward * 2 + (ward == 4 ? 2 : 0), Operation.ADD_VALUE);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        modifier(p, Attributes.BLOCK_INTERACTION_RANGE, "reach", (trail(p, HEAD) ? 1 : 0) + (trailFull(p) ? 2 : 0), Operation.ADD_VALUE);
        modifier(p, Attributes.STEP_HEIGHT, "step", has(p, LEGS, "wild") ? .4 : 0, Operation.ADD_VALUE);
        modifier(p, Attributes.SNEAKING_SPEED, "sneak", trail(p, LEGS) ? .15 : 0, Operation.ADD_VALUE);
        modifier(p, Attributes.SUBMERGED_MINING_SPEED, "aqua", has(p, HEAD, "tide") ? existingAqua(p) : 0, Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p, Attributes.BURNING_TIME, "fire_protection", full(p,"rib") ? -.6 : 0, Operation.ADD_MULTIPLIED_BASE);
        modifier(p, Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, "blast_protection", has(p,CHEST,"flow") ? .3 : 0, Operation.ADD_VALUE);
        var below = p.level().getBlockState(p.getBlockPosBelowThatAffectsMyMovement());
        double speed = has(p, HEAD, "wild") ? .2 : 0;
        if (has(p, FEET, "snout") && p.level().dimension() == Level.NETHER) speed += .15;
        if (has(p, LEGS, "rib") && below.is(NETHER_TERRAIN)) speed += .15;
        if (has(p, LEGS, "dune") && below.is(DESERT_TERRAIN)) speed += .2;
        if (has(p, LEGS, "flow") && p.level().dimension() == Level.OVERWORLD && p.level().canSeeSky(p.blockPosition())) speed += .1;
        if (TrimTriggers.active(p, "vex_speed")) speed += .4;
        modifier(p, Attributes.MOVEMENT_SPEED, "movement", speed, Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p, Attributes.ATTACK_SPEED, "storm", full(p, "bolt") && storm(p) && p.getMainHandItem().is(BOLT_WEAPONS) ? .25 : 0, Operation.ADD_MULTIPLIED_TOTAL);
        if (!full(p, "bolt")) p.getPersistentData().putInt("chocolateflavored:trim/bolt_hits", 0);
        if (p.getAirSupply() > p.getMaxAirSupply()) p.setAirSupply(p.getMaxAirSupply());
    }
    private static double existingAqua(Player p) {
        var modifier = p.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getModifier(id("trim/aqua"));
        double ours = modifier == null ? 0 : modifier.amount();
        return p.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED) / (1 + ours) < 1 ? 4 : 0;
    }
    private TrimAttributes() {}
}
