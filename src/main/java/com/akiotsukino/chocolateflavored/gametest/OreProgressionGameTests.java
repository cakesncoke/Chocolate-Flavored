package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OreProgressionGameTests {
    @GameTest(template = "oven_test_empty")
    public static void flintOresDropOnePileConvertAndBreakOnLastUse(GameTestHelper helper) {
        var player = player(helper);
        var tool = new ItemStack(Items.FLINT);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        tool.setDamageValue(2);
        var pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, Blocks.COPPER_ORE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.COBBLESTONE, pos);
        helper.assertTrue(tool.getDamageValue() == 3 && count(helper, ModItems.COPPER_ORE_PILE.get()) == 1,
                "Flint copper extraction must produce one pile and spend one charge");
        helper.setBlock(pos, Blocks.COAL_ORE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.COBBLESTONE, pos);
        helper.assertTrue(player.getMainHandItem().isEmpty() && count(helper, ModItems.COAL_ORE_PILE.get()) == 1,
                "Final flint use must still extract exactly one coal pile");
        helper.assertTrue(count(helper, Items.RAW_COPPER) == 0 && count(helper, Items.COAL) == 0 && count(helper, Items.COBBLESTONE) == 0,
                "Conversion must replace all ordinary ore and block drops");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void copperExtractsIronAndDeepslateAndUsesOneDurability(GameTestHelper helper) {
        var player = player(helper);
        var tool = new ItemStack(ModItems.COPPER_PICKAXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        var pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, Blocks.IRON_ORE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.COBBLESTONE, pos);
        helper.assertTrue(tool.getDamageValue() == 1 && count(helper, ModItems.IRON_ORE_PILE.get()) == 1,
                "Copper iron extraction must yield one pile and use one durability");
        tool.setDamageValue(tool.getMaxDamage() - 1);
        helper.setBlock(pos, Blocks.DEEPSLATE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.COBBLED_DEEPSLATE, pos);
        helper.assertTrue(player.getMainHandItem().isEmpty() && count(helper, ModItems.DEEPSLATE_PEBBLE.get()) == 1,
                "Final copper use must convert deepslate and yield one pebble");
        helper.assertTrue(count(helper, Items.RAW_IRON) == 0 && count(helper, Items.COBBLED_DEEPSLATE) == 0,
                "Custom extraction must never also drop raw iron or a deepslate block");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void copperCannotProgressHarvestOrRemoveAnyDeepslateOre(GameTestHelper helper) {
        var player = player(helper);
        var tool = new ItemStack(ModItems.COPPER_PICKAXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        Block[] ores = {Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_IRON_ORE,
                Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
                Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE};
        var pos = new BlockPos(2, 2, 2);
        for (Block ore : ores) {
            helper.setBlock(pos, ore);
            var state = ore.defaultBlockState();
            helper.assertTrue(tool.getDestroySpeed(state) == 0.0F && !tool.isCorrectToolForDrops(state),
                    "Copper must have zero progress and no harvesting permission for every deepslate ore");
            helper.assertTrue(!player.gameMode.destroyBlock(helper.absolutePos(pos)), "Server must refuse a forced copper break too");
            helper.assertBlockPresent(ore, pos);
        }
        helper.assertTrue(tool.getDamageValue() == 0 && helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).isEmpty(),
                "Blocked mining must not wear the tool or create loot");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        helper.setBlock(pos, Blocks.DEEPSLATE_IRON_ORE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertTrue(count(helper, Items.RAW_IRON) == 1, "Iron must still harvest deepslate iron normally");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void shovelIsRequiredForNormalSoilDropsWhileOtherHeldItemsYieldPiles(GameTestHelper helper) {
        var player = player(helper);
        var pos = new BlockPos(2, 2, 2);
        for (Item item : new Item[]{Items.AIR, Items.STICK, Items.FLINT, Items.IRON_SWORD, ModItems.FLINT_KNIFE.get(), ModItems.COPPER_PICKAXE.get()}) {
            clearDrops(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            helper.setBlock(pos, Blocks.DIRT);
            player.gameMode.destroyBlock(helper.absolutePos(pos));
            helper.assertTrue(count(helper, ModItems.DIRT_PILE.get()) == 3 && count(helper, Items.DIRT) == 0,
                    "Every non-shovel, including empty hand, must yield three dirt piles");
        }
        for (Item shovel : new Item[]{Items.WOODEN_SHOVEL, Items.IRON_SHOVEL, ModItems.COPPER_SHOVEL.get()}) {
            clearDrops(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shovel));
            helper.setBlock(pos, Blocks.DIRT);
            player.gameMode.destroyBlock(helper.absolutePos(pos));
            helper.assertTrue(count(helper, Items.DIRT) == 1 && count(helper, ModItems.DIRT_PILE.get()) == 0,
                    "Vanilla and modded shovels must retain their original loot");
        }
        for (Block block : new Block[]{Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL}) {
            clearDrops(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            helper.setBlock(pos, block);
            player.gameMode.destroyBlock(helper.absolutePos(pos));
            Item pile = block == Blocks.SAND ? ModItems.SAND_PILE.get() : block == Blocks.RED_SAND ? ModItems.RED_SAND_PILE.get() : ModItems.GRAVEL_PILE.get();
            helper.assertTrue(count(helper, pile) == 3 && count(helper, block.asItem()) == 0,
                    "Non-shovel sand and gravel mining must use the matching piles too");
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ore-test"));
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }
    private static int count(GameTestHelper helper, Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).stream()
                .filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }
    private static void clearDrops(GameTestHelper helper) {
        for (var entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds())) entity.discard();
    }
}
