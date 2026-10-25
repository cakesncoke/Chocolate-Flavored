package com.akiotsukino.chocolateflavored.trim;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;

@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class TrimCombat {
    private static final ThreadLocal<Boolean> DISCHARGING = ThreadLocal.withInitial(() -> false);
    public static boolean hostile(Entity e) { return e instanceof Enemy; }
    public static ItemStack weapon(DamageSource source) {
        if (!source.is(DamageTypes.PLAYER_ATTACK) && !source.is(DamageTypes.MOB_ATTACK) && !source.is(DamageTypeTags.IS_PROJECTILE)) return ItemStack.EMPTY;
        var direct=source.getDirectEntity();var stack=direct==null?null:direct.getWeaponItem();return stack==null?ItemStack.EMPTY:stack;
    }
    public static float protection(LivingEntity p, DamageSource s) {
        float points = 0;
        if (has(p, FEET, "wild") && s.is(DamageTypeTags.IS_FALL)) points += 6;
        if ((has(p, CHEST, "sentry") || has(p, CHEST, "dune")) && s.is(DamageTypeTags.IS_PROJECTILE)) points += 4;
        if (has(p, CHEST, "flow") && s.is(DamageTypeTags.IS_EXPLOSION)) points += 4;
        if (full(p, "rib") && s.is(DamageTypeTags.IS_FIRE)) points += 8;
        return points;
    }
    @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e) {
        var victim = e.getEntity(); var source = e.getSource(); float amount = e.getAmount();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (victim instanceof Player p) {
            if (source.is(DamageTypes.LIGHTNING_BOLT) && has(p, HEAD, "bolt")) amount = 0;
            if (source.is(DamageTypes.FLY_INTO_WALL) && has(p, HEAD, "spire")) amount *= .25f;
            if (source.is(DamageTypes.HOT_FLOOR) && has(p, FEET, "rib")) amount = 0;
            if(source.is(DamageTypes.LAVA) && has(p,CHEST,"rib")) {
                if(TrimTriggers.ready(p,"rib") && !p.getPersistentData().getBooleanOr(TrimTriggers.key("in_lava"),false)){TrimTriggers.start(p,"rib",600);TrimTriggers.duration(p,"lava",100);}
                if(TrimTriggers.active(p,"lava"))amount=0;
            }
            if (source.is(DamageTypeTags.IS_FALL)) {
                if (trail(p, FEET) && p.fallDistance <= 5) amount = 0;
                if (has(p, FEET, "spire")) amount *= .5f;
                if (has(p, FEET, "flow")) amount *= .75f;
                if (has(p, FEET, "eye") && p.level().getBlockState(p.getBlockPosBelowThatAffectsMyMovement()).is(STONE_LANDINGS)) amount *= .5f;
            }
            if (has(p, HEAD, "flow") && source.getDirectEntity() != null && source.getDirectEntity().getType() == EntityType.WIND_CHARGE) amount = 0;
            if (source.getEntity() instanceof AbstractPiglin && has(p, CHEST, "snout")) amount *= .7f;
            if (weapon(source).is(BOLT_WEAPONS) && has(p, LEGS, "bolt")) amount *= .5f;
        }
        if (!DISCHARGING.get() && source.getEntity() instanceof Player p) {
            double bonus = 0;
            if (source.is(DamageTypeTags.IS_PROJECTILE) && has(p, HEAD, "sentry")) bonus += full(p, "sentry") ? .3 : .15;
            if (source.getDirectEntity() == p && source.is(DamageTypes.PLAYER_ATTACK)) {
                if (victim.is(EntityTypeTags.ILLAGER) && has(p, CHEST, "vex")) bonus += .25;
                if (victim instanceof Mob && has(p, CHEST, "silence")) bonus += full(p, "silence") ? .2 : .1;
                if (p.isUnderWater() && full(p, "tide")) bonus += .5;

            }
            if(has(p,CHEST,"bolt") && weapon(source).is(BOLT_WEAPONS))bonus+=.2;
            amount *= (float)(1 + bonus);
        }
        e.setAmount(amount);
    }
    @SubscribeEvent public static void damaged(LivingDamageEvent.Post e) {
        if (e.getHealthDamage() <= 0) return;
        if (e.getEntity() instanceof Player p && has(p, FEET, "vex") && p.isAlive() && p.getHealth() < 10 && TrimTriggers.ready(p, "vex")) {
            TrimTriggers.start(p, "vex", 600); TrimTriggers.duration(p, "vex_speed", 100);
        }
        var s = e.getSource();
        if (DISCHARGING.get() || !(s.getEntity() instanceof Player p) || s.getDirectEntity() != p || !s.is(DamageTypes.PLAYER_ATTACK)) return;
        if (full(p, "rib") && p.getRandom().nextFloat() < .3f) e.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
        if (!full(p, "bolt")) return;
        var data = p.getPersistentData();
        // Multiple damage callbacks from a single native attack cannot count more than once.
        long now = p.level().getGameTime();
        if (data.getLongOr("chocolateflavored:trim/bolt_last_hit", -1) == now) return;
        data.putLong("chocolateflavored:trim/bolt_last_hit", now);
        int hits = data.getIntOr("chocolateflavored:trim/bolt_hits", 0) + 1;
        data.putInt("chocolateflavored:trim/bolt_hits", hits % 5);
        if (hits < 5 || !(p.level() instanceof ServerLevel level)) return;
        if (storm(p)) {
            var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) { bolt.setPos(e.getEntity().position()); if (p instanceof net.minecraft.server.level.ServerPlayer sp) bolt.setCause(sp); level.addFreshEntity(bolt); }
        } else if (e.getEntity().isAlive()) {
            DISCHARGING.set(true);
            try { e.getEntity().hurtServer(level, new DamageSource(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,id("static_discharge"))),p), 2); } finally { DISCHARGING.remove(); }
        }
    }
    @SubscribeEvent public static void drops(LivingDropsEvent e) {
        if (e.isCanceled() || !(e.getSource().getEntity() instanceof Player p)) return;
        var victim = e.getEntity();
        if (victim.getPersistentData().getBooleanOr("chocolateflavored:trim/kill_processed", false)) return;
        victim.getPersistentData().putBoolean("chocolateflavored:trim/kill_processed", true);
        if (victim instanceof Mob && has(p, LEGS, "snout") && p.getRandom().nextFloat() < .1f) e.getDrops().add(new ItemEntity(p.level(), victim.getX(), victim.getY(), victim.getZ(), new ItemStack(Items.GOLD_NUGGET)));
        if (hostile(victim) && has(p, LEGS, "silence") && p.getRandom().nextFloat() < (full(p, "silence") ? .2f : .1f)) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false, false));
    }
    private TrimCombat() {}
}
