package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.event.SpearCombat;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpearGameTests {
    @GameTest(template = "oven_test_empty")
    public static void jabPiercesTargetsRespectsMinimumReachAndCooldown(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(ModItems.IRON_SPEAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Zombie close = target(helper, 1), first = target(helper, 3), second = target(helper, 4), far = target(helper, 6);
        SpearCombat.jab(player);
        helper.assertTrue(close.getHealth() == 20 && far.getHealth() == 20, "Jab must enforce minimum and maximum reach");
        helper.assertTrue(first.getHealth() == 17 && second.getHealth() == 17 && stack.getDamageValue() == 2,
                "Iron jab must pierce both in-range targets for three damage and one wear per target");
        SpearCombat.jab(player);
        helper.assertTrue(stack.getDamageValue() == 2, "Repeated packets must not bypass the material cooldown");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void wallStopsBothJabAndChargeAndSpearsCannotMineBlocks(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(ModItems.DIAMOND_SPEAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Zombie target = target(helper, 3);
        var wall = new BlockPos(2, 2, 2);
        helper.setBlock(wall, Blocks.STONE);
        SpearCombat.jab(player);
        player.setDeltaMovement(0, 0, 1);
        SpearCombat.beginCharge(player);
        SpearCombat.charge(player, stack, 10);
        helper.assertTrue(target.getHealth() == 20 && stack.getDamageValue() == 0, "Spears must never pierce walls");
        helper.assertTrue(!player.gameMode.destroyBlock(helper.absolutePos(wall)), "Spears must not mine blocks");
        helper.assertBlockPresent(Blocks.STONE, wall);
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void chargeWarmupVelocityDamageAndContactCooldownMatchModernRules(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(ModItems.IRON_SPEAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Zombie target = target(helper, 3);
        player.setDeltaMovement(0, 0, .6); // 12 blocks/second projected along the spear.
        SpearCombat.beginCharge(player);
        SpearCombat.charge(player, stack, 11);
        helper.assertTrue(target.getHealth() == 20, "Iron charge must wait twelve ticks");
        SpearCombat.charge(player, stack, 12);
        helper.assertTrue(target.getHealth() == 8 && stack.getDamageValue() == 1,
                "Charge damage must be one plus floor(relative speed times 0.95)");
        SpearCombat.charge(player, stack, 13);
        helper.assertTrue(stack.getDamageValue() == 1, "Contact cooldown must prevent damage every use tick");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void approachingTargetsAndLateChargeDoDamageWithoutKnockback(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(ModItems.IRON_SPEAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Zombie approaching = target(helper, 3);
        approaching.setDeltaMovement(0, 0, -.3);
        SpearCombat.beginCharge(player);
        SpearCombat.charge(player, stack, 12);
        helper.assertTrue(approaching.getHealth() == 14, "An approaching target must provide relative charge velocity");
        helper.assertTrue(approaching.getDeltaMovement().z == -.3, "Stationary charge must not invent knockback");
        approaching.discard();
        Zombie late = target(helper, 3);
        player.setDeltaMovement(0, 0, .6);
        SpearCombat.beginCharge(player);
        SpearCombat.charge(player, stack, 12 + 136);
        helper.assertTrue(late.getHealth() == 8 && late.getDeltaMovement().lengthSqr() == 0,
                "Disengaged phase must deal velocity damage with no knockback");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void lungeRequiresFoodAndAppliesImpulseWearAndExhaustion(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(ModItems.NETHERITE_SPEAR.get());
        var enchantment = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SpearCombat.LUNGE);
        stack.enchant(enchantment, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.getFoodData().setFoodLevel(5);
        SpearCombat.jab(player);
        helper.assertTrue(stack.getDamageValue() == 0 && player.getDeltaMovement().lengthSqr() == 0,
                "Lunge must be unavailable below six hunger points");
        player.getPersistentData().remove("chocolateflavored:next_spear_jab");
        player.getFoodData().setFoodLevel(20);
        SpearCombat.jab(player);
        helper.assertTrue(stack.getDamageValue() == 1 && Math.abs(player.getDeltaMovement().z - .916) < .0001,
                "Lunge II must add 0.916 horizontal impulse and consume one durability even on a miss");
        helper.assertTrue(player.getFoodData().getExhaustionLevel() >= 8, "Lunge II must apply eight exhaustion");
        var sweeping = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SWEEPING_EDGE);
        helper.assertTrue(!stack.supportsEnchantment(sweeping), "Spear must reject Sweeping Edge");
        helper.assertTrue(stack.getMaxDamage() == 6093 && stack.getItem().isValidRepairItem(stack, new ItemStack(Items.NETHERITE_INGOT)),
                "Netherite spear must use mod durability and netherite repair material");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void engagedChargeDismountsButTiredChargeLeavesRidersMounted(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(ModItems.IRON_SPEAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Zombie target = target(helper, 3);
        var boat = EntityType.BOAT.create(helper.getLevel());
        boat.moveTo(target.getX(), target.getY(), target.getZ(), 0, 0);
        helper.getLevel().addFreshEntity(boat);
        target.startRiding(boat, true);
        player.setDeltaMovement(0, 0, .4);
        SpearCombat.beginCharge(player);
        SpearCombat.charge(player, stack, 12);
        helper.assertTrue(!target.isPassenger(), "Engaged iron charge at eight blocks/second must dismount its target");
        target.invulnerableTime = 0;
        target.setHealth(20);
        target.setDeltaMovement(0, 0, 0);
        target.startRiding(boat, true);
        target.moveTo(boat.getX(), boat.getY() + .5, boat.getZ(), 0, 0);
        SpearCombat.beginCharge(player);
        SpearCombat.charge(player, stack, 12 + 51);
        helper.assertTrue(target.isPassenger() && target.getHealth() < 20,
                "Tired charge must damage without dismounting; health=" + target.getHealth() + ", riding=" + target.isPassenger());
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void jabUsesStrengthAndCurrentWeaponRatherThanStaleSwordAttributes(GameTestHelper helper) {
        var player = player(helper);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 200));
        // Simulate the stale mainhand modifier from a just-swapped netherite sword.
        player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).addTransientModifier(
                new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, 7,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        var stack = new ItemStack(ModItems.IRON_SPEAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var target = target(helper, 3);
        SpearCombat.jab(player);
        helper.assertTrue(target.getHealth() == 14, "Iron jab must deal three plus Strength I's three damage, without borrowing sword damage");
        helper.succeed();
    }
    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "spear-test"));
        player.setGameMode(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(2, 1, 0));
        player.moveTo(pos.getX() + .5, pos.getY(), pos.getZ(), 0, 0);
        return player;
    }
    private static Zombie target(GameTestHelper helper, double z) {
        var entity = EntityType.ZOMBIE.create(helper.getLevel());
        var pos = helper.absolutePos(new BlockPos(2, 1, 0));
        entity.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + z, 0, 0);
        entity.setNoAi(true);
        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }
}
