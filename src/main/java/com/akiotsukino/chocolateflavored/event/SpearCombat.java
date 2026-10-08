package com.akiotsukino.chocolateflavored.event;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.item.SpearItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

/** All targeting, velocity thresholds, cooldowns, damage and durability are decided by the server. */
public final class SpearCombat {
    public static final ResourceKey<Enchantment> LUNGE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "lunge"));
    private static final ResourceKey<net.minecraft.world.damagesource.DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "spear"));
    private static final Map<Player, Motion> MOTION = new WeakHashMap<>();
    private static final Map<Player, Map<UUID, Long>> CONTACTS = new WeakHashMap<>();
    private record Motion(Vec3 position, Vec3 velocity) {}
    private SpearCombat() {}

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        Motion previous = MOTION.get(player);
        Vec3 delta = previous == null ? Vec3.ZERO : player.position().subtract(previous.position);
        // Teleports are not charge velocity. Walking, riding and elytra motion share the same position sampling.
        if (delta.lengthSqr() > 64) delta = Vec3.ZERO;
        MOTION.put(player, new Motion(player.position(), delta));
        if (!player.isUsingItem()) CONTACTS.remove(player);
    }
    private static Vec3 movement(Entity entity) {
        if (entity instanceof Player player && MOTION.containsKey(player)) return MOTION.get(player).velocity.scale(20);
        if (entity.isPassenger()) entity = entity.getRootVehicle();
        return entity.getDeltaMovement().scale(20);
    }
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.isCanceled() || !(event.getEntity().getMainHandItem().getItem() instanceof SpearItem)) return;
        event.setCanceled(true);
        if (!event.getEntity().level().isClientSide) jab(event.getEntity());
    }
    public static void jab(Player player) {
        if (!(player.level() instanceof ServerLevel level) || player.isSpectator() || player.isUsingItem()) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpearItem spear)) return;
        long time = level.getGameTime();
        String key = "chocolateflavored:next_spear_jab";
        if (player.getPersistentData().contains(key) && time < player.getPersistentData().getLong(key)) return;
        player.getPersistentData().putLong(key, time + spear.material().jabTicks());
        // Attack the whole unobstructed ray, not a client-provided entity ID.
        boolean hit = false;
        float damage = jabDamage(player, stack);
        for (Entity target : targets(player)) {
            if (stack.isEmpty()) break;
            hit |= impact(player, target, stack, damage, true, true, false, EquipmentSlot.MAINHAND);
        }
        lunge(player, stack);
        player.resetAttackStrengthTicker();
        player.swing(InteractionHand.MAIN_HAND, true);
        sound(player, hit ? "item.spear.hit" : "item.spear.attack");
        player.awardStat(Stats.ITEM_USED.get(spear));
    }
    private static float jabDamage(Player player, ItemStack stack) {
        // Use a fresh snapshot so potions/modifiers work and a just-swapped sword cannot lend its damage.
        var attack = new net.minecraft.world.entity.ai.attributes.AttributeInstance(Attributes.ATTACK_DAMAGE, unused -> {});
        attack.replaceFrom(player.getAttribute(Attributes.ATTACK_DAMAGE));
        attack.removeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID);
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
                attack.removeModifier(modifier.id());
                attack.addTransientModifier(modifier);
            }
        });
        return (float) attack.getValue();
    }
    public static void beginCharge(Player player) { CONTACTS.put(player, new HashMap<>()); sound(player, "item.spear.use"); }
    public static void endCharge(Player player) { if (!player.level().isClientSide) CONTACTS.remove(player); }
    public static void charge(Player player, ItemStack stack, int useTicks) {
        if (!(player.level() instanceof ServerLevel level) || player.isSpectator() || !(stack.getItem() instanceof SpearItem spear)) return;
        SpearItem.Material material = spear.material();
        int age = useTicks - material.delay;
        if (age < 0 || age > material.damageDuration) return;
        Vec3 look = player.getLookAngle();
        double forwardSpeed = look.dot(movement(player));
        var contacts = CONTACTS.computeIfAbsent(player, unused -> new HashMap<>());
        for (Entity target : targets(player)) {
            if (stack.isEmpty()) break;
            long now = level.getGameTime();
            Long last = contacts.get(target.getUUID());
            if (last != null && now - last < 10) continue;
            contacts.put(target.getUUID(), now);
            double relativeSpeed = Math.max(0, forwardSpeed - look.dot(movement(target)));
            boolean dismount = age <= material.dismountDuration && forwardSpeed >= material.dismountSpeed;
            boolean knockback = age <= material.knockbackDuration && forwardSpeed >= 5.1;
            boolean damage = relativeSpeed >= 4.6;
            if (!damage && !knockback && !dismount) continue;
            float amount = (float) player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE)
                    + (float) Math.floor(relativeSpeed * material.damageMultiplier);
            EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            if (impact(player, target, stack, amount, damage, knockback, dismount, slot)) sound(player, "item.spear.hit");
        }
    }
    private static List<Entity> targets(Player player) {
        Vec3 eye = player.getEyePosition(), look = player.getLookAngle();
        Vec3 start = eye.add(look.scale(2)), end = eye.add(look.scale(player.isCreative() ? 6.5 : 4.5));
        var wall = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (eye.distanceToSqr(wall.getLocation()) < eye.distanceToSqr(start)) return List.of();
        end = wall.getLocation();
        final Vec3 rayEnd = end;
        List<Entity> entities = player.level().getEntities(player,
                new AABB(start, end).inflate(1), target -> target != player && target.isAlive() && !target.isSpectator()
                        && target.isPickable() && target.isAttackable() && !player.isPassengerOfSameVehicle(target)
                        && (!(target instanceof Player other) || player.canHarmPlayer(other)));
        entities.removeIf(target -> target.getBoundingBox().inflate(.125).clip(start, rayEnd).isEmpty());
        entities.sort(Comparator.comparingDouble(target -> target.distanceToSqr(player)));
        return entities;
    }
    private static boolean impact(Player player, Entity target, ItemStack stack, float amount,
                                  boolean dealDamage, boolean knockback, boolean dismount, EquipmentSlot slot) {
        ServerLevel level = (ServerLevel) player.level();
        var source = new net.minecraft.world.damagesource.DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE), player);
        float before = target instanceof LivingEntity living ? living.getHealth() : 0;
        boolean hurt = dealDamage && target.hurt(source, EnchantmentHelper.modifyDamage(level, stack, target, source, amount));
        boolean pushed = knockback && target instanceof LivingEntity && !target.isInvulnerableTo(source);
        if (pushed) {
            float strength = EnchantmentHelper.modifyKnockback(level, stack, target, source, .4F);
            ((LivingEntity) target).knockback(strength, -player.getLookAngle().x, -player.getLookAngle().z);
        }
        boolean unmounted = dismount && target.isPassenger();
        if (unmounted) target.stopRiding();
        if (!hurt && !pushed && !unmounted) return false;
        if (hurt) {
            EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, stack);
            player.setLastHurtMob(target);
            player.awardStat(Stats.DAMAGE_DEALT, Math.round(Math.max(0, before - (target instanceof LivingEntity living ? living.getHealth() : 0)) * 10));
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new com.akiotsukino.chocolateflavored.network.SpearNetwork.Hit(player.getId()));
        stack.hurtAndBreak(1, player, slot);
        player.causeFoodExhaustion(.1F);
        return true;
    }
    private static void lunge(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.isInWater() || player.isFallFlying() || player.isPassenger()
                || (!player.isCreative() && player.getFoodData().getFoodLevel() < 6)) return;
        var enchantment = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(LUNGE);
        int level = enchantment.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
        if (level == 0) return;
        Vec3 look = player.getLookAngle();
        player.push(look.x * .458 * level, 0, look.z * .458 * level);
        player.hurtMarked = true;
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.causeFoodExhaustion(4F * level);
        sound(player, "item.spear.lunge_" + level);
    }
    private static void sound(Player player, String name) {
        var id = ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, name);
        var event = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(id);
        if (event != null) player.level().playSound(null, player.blockPosition(), event, player.getSoundSource(), 1F, 1F);
    }
}
