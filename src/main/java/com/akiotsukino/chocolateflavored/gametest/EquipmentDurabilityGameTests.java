package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EquipmentDurabilityGameTests {
    @GameTest(template = "oven_test_empty")
    public static void everyRequestedToolWeaponAndArmorUsesTheNewMaximum(GameTestHelper helper) {
        check(helper, new Item[]{Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS},
                new int[]{375, 375, 375, 375, 375, 248, 360, 338, 293});
        check(helper, new Item[]{Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE,
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS},
                new int[]{3122, 3122, 3122, 3122, 3122, 726, 1056, 990, 858});
        check(helper, new Item[]{Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS},
                new int[]{6093, 6093, 6093, 6093, 6093, 1221, 1776, 1665, 1443});
        helper.assertTrue(new ItemStack(ModItems.COPPER_PICKAXE.get()).getMaxDamage() == 250
                && new ItemStack(Items.FLINT).getMaxDamage() == 4 && new ItemStack(ModItems.FLINT_KNIFE.get()).getMaxDamage() == 131,
                "Vanilla multipliers must not change copper or the primitive tools");
        helper.assertTrue(new ItemStack(Items.STONE_PICKAXE).getMaxDamage() == 131
                && new ItemStack(Items.GOLDEN_PICKAXE).getMaxDamage() == 32 && new ItemStack(Items.BOW).getMaxDamage() == 384,
                "Unrequested equipment tiers must retain vanilla durability");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void toolsAndArmorSurviveTheirOldLimitAndBreakAtTheNewOne(GameTestHelper helper) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "durability-test"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var sword = new ItemStack(Items.IRON_SWORD);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        sword.setDamageValue(249);
        sword.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        helper.assertTrue(!sword.isEmpty() && sword.getDamageValue() == 250, "Iron sword must survive its old vanilla breaking point");
        sword.setDamageValue(374);
        sword.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        helper.assertTrue(sword.isEmpty(), "Iron sword must break at 375 uses");
        var helmet = new ItemStack(Items.DIAMOND_HELMET);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
        helmet.setDamageValue(362);
        helmet.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.HEAD);
        helper.assertTrue(!helmet.isEmpty() && helmet.getDamageValue() == 363, "Diamond helmet must survive its old durability limit");
        helmet.setDamageValue(725);
        helmet.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.HEAD);
        helper.assertTrue(helmet.isEmpty(), "Diamond helmet must break at the doubled maximum");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, Item[] items, int[] maxima) {
        for (int i = 0; i < items.length; i++) {
            var stack = new ItemStack(items[i]);
            helper.assertTrue(stack.getMaxDamage() == maxima[i] && stack.getMaxStackSize() == 1,
                    "Each requested equipment item must use its multiplied, rounded durability");
        }
    }
}
