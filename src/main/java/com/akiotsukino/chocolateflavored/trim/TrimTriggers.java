package com.akiotsukino.chocolateflavored.trim;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;

@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class TrimTriggers {
    public static final String[] COOLDOWNS = {"vex", "rib", "sentry", "eye"};
    public static boolean cooldownApplies(ItemStack stack,String ability) {
        if(!pattern(stack).equals("minecraft:"+ability))return false;
        var equipped=stack.get(DataComponents.EQUIPPABLE);if(equipped==null)return false;
        return switch(ability){case "vex"->equipped.slot()==FEET;case "rib"->equipped.slot()==CHEST;default->true;};
    }
    public static String key(String ability) { return "chocolateflavored:trim/" + ability; }
    public static long clock(Player p) { return p.level() instanceof ServerLevel l ? l.getServer().overworld().getGameTime() : p.level().getGameTime(); }
    public static boolean ready(Player p, String ability) { return p.getPersistentData().getLongOr(key(ability), 0) <= clock(p); }
    public static boolean active(Player p, String ability) {
        if (ability.equals("vex_speed") && !has(p, FEET, "vex")) return false;
        return !ready(p, ability);
    }
    public static void duration(Player p, String ability, int ticks) { p.getPersistentData().putLong(key(ability), clock(p) + ticks); }
    public static void start(Player p, String ability, int ticks) {
        duration(p, ability, ticks);
        p.getCooldowns().addCooldown(id("trim_cooldown/"+ability), ticks);
    }
    public static boolean resurrect(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer p) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !full(p, "vex")) return false;
        int[] costs = new int[4];
        for (int i = 0; i < ARMOR.length; i++) {
            var stack = p.getItemBySlot(ARMOR[i]); costs[i] = (stack.getMaxDamage() + 2) / 3;
            if (!stack.isDamageableItem() || stack.has(DataComponents.UNBREAKABLE) || stack.getMaxDamage() - stack.getDamageValue() < costs[i]) return false;
        }
        for (int i = 0; i < ARMOR.length; i++) {
            var stack = p.getItemBySlot(ARMOR[i]);
            stack.setDamageValue(stack.getDamageValue() + costs[i]);
            if (stack.getDamageValue() >= stack.getMaxDamage()) { stack.shrink(1); }
        }
        p.setHealth(1);
        DeathProtection.TOTEM_OF_UNDYING.applyEffects(new ItemStack(Items.TOTEM_OF_UNDYING), p);
        p.level().broadcastEntityEvent(p, (byte)35);
        TrimAttributes.update(p);
        return true;
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        TrimHighlights.expire(p);
        if(!p.isAlive() || p.isSpectator())return;
        var data = p.getPersistentData();
        boolean lava = p.isInLava();
        if (lava && !data.getBooleanOr(key("in_lava"), false) && has(p, CHEST, "rib") && ready(p, "rib")) { start(p, "rib", 600); duration(p, "lava", 100); }
        data.putBoolean(key("in_lava"), lava);
        if (p.onGround() && !p.isInWater() && !p.isInLava()) data.putBoolean(key("flow_used"), false);
        boolean crouch = p.isShiftKeyDown();
        boolean edge = crouch && !data.getBooleanOr(key("crouching"), false);
        data.putBoolean(key("crouching"), crouch);
        if (edge && !p.onGround() && !p.isPassenger() && full(p, "flow") && !data.getBooleanOr(key("flow_used"), false)) {
            data.putBoolean(key("flow_used"), true);
            WindCharge charge = new WindCharge(p, p.level(), p.getX(), p.getY() - .15, p.getZ());
            ((com.akiotsukino.chocolateflavored.mixin.TrimWindInvoker)charge).chocolateflavored$explode(charge.position());
        }
        if (edge && full(p, "sentry") && ranged(p.getMainHandItem()) && ready(p, "sentry")) {
            start(p, "sentry", 1200);
            for (var mob : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(24), m -> TrimCombat.hostile(m) && m.distanceToSqr(p) <= 576))
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false, false));
        }
        if (edge && full(p, "eye") && p.getMainHandItem().is(net.minecraft.tags.ItemTags.PICKAXES) && ready(p, "eye")) {
            start(p, "eye", 12000); TrimHighlights.scan(p, false);
        }
        if (p.tickCount % 20 == 0 && full(p, "dune") && (p.getMainHandItem().is(Items.BRUSH) || p.getOffhandItem().is(Items.BRUSH))) TrimHighlights.scan(p, true);
        if (has(p, FEET, "rib")) TrimBasalt.walk(p);
        // Native cooldown packets are restored after reconnects/equipment changes without item mutation.
        if (p.tickCount % 20 == 0) for (String ability : COOLDOWNS) if (!ready(p, ability)) for (var slot : ARMOR)
            if (cooldownApplies(p.getItemBySlot(slot), ability) && p.getCooldowns().getCooldownPercent(p.getItemBySlot(slot),0)==0) p.getCooldowns().addCooldown(id("trim_cooldown/"+ability), (int)Math.min(Integer.MAX_VALUE, data.getLongOr(key(ability), 0) - clock(p)));
        if (has(p, LEGS, "eye")) for (var item : p.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, p.getBoundingBox().inflate(3, 2.5, 3))) item.playerTouch(p);
    }
    public static boolean ranged(ItemStack stack) { return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem; }
    private TrimTriggers() {}
}
