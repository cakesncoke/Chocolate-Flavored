package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RoseGoldCakeGameTests {
    @GameTest(template = "oven_test_empty")
    public static void roseGoldSmithingPreservesDamageAndImprovesIronTools(GameTestHelper helper) {
        Item[] iron = {Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE};
        Item[] rose = {ModItems.ROSE_GOLD_SWORD.get(), ModItems.ROSE_GOLD_PICKAXE.get(), ModItems.ROSE_GOLD_AXE.get(), ModItems.ROSE_GOLD_SHOVEL.get(), ModItems.ROSE_GOLD_HOE.get()};
        for (int i = 0; i < iron.length; i++) {
            var base = new ItemStack(iron[i]);
            base.setDamageValue(100);
            var input = new SmithingRecipeInput(new ItemStack(ModItems.ROSE_GOLD_UPGRADE_SMITHING_TEMPLATE.get()), base, new ItemStack(Items.GOLD_INGOT));
            var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel()).orElseThrow();
            var result = recipe.value().assemble(input, helper.getLevel().registryAccess());
            helper.assertTrue(result.is(rose[i]) && result.getDamageValue() == 100 && result.getMaxDamage() == new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage(),
                    "Each iron tool must smith to the matching rose gold tool, retain damage and gain diamond durability");
            helper.assertTrue(result.getItem().isValidRepairItem(result, new ItemStack(Items.GOLD_INGOT)), "Gold ingots must repair rose gold tools");
        }
        var pick = new ItemStack(ModItems.ROSE_GOLD_PICKAXE.get());
        helper.assertTrue(pick.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 7F && pick.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState())
                && !pick.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "Rose gold must be faster than iron without gaining diamond mining level");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void netheriteSpearSmithingPreservesComponentsAndBoostsDurability(GameTestHelper helper) {
        var base = new ItemStack(ModItems.DIAMOND_SPEAR.get());
        base.setDamageValue(125);
        var input = new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), base, new ItemStack(Items.NETHERITE_INGOT));
        var result = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel()).orElseThrow()
                .value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(ModItems.NETHERITE_SPEAR.get()) && result.getDamageValue() == 125 && result.getMaxDamage() == 6093,
                "Diamond spear must upgrade to the matching netherite spear, retaining damage");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void chocolateCakeHasSevenVanillaBitesAndComparatorValues(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "cake-test"));
        player.setGameMode(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 1, 2), ModBlocks.CHOCOLATE_CAKE.get());
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (int bite = 0; bite < 7; bite++) {
            var state = helper.getLevel().getBlockState(pos);
            helper.assertTrue(state.getValue(CakeBlock.BITES) == bite && state.getAnalogOutputSignal(helper.getLevel(), pos) == 14 - bite * 2,
                    "Chocolate cake must have vanilla bites and comparator output");
            player.getFoodData().setFoodLevel(10);
            player.getFoodData().setSaturation(0);
            state.useWithoutItem(helper.getLevel(), player, hit);
            helper.assertTrue(player.getFoodData().getFoodLevel() == 12 && Math.abs(player.getFoodData().getSaturationLevel() - .4F) < .0001,
                    "Every slice must restore two food points and 0.4 saturation");
        }
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 1, 2));
        helper.assertTrue(new ItemStack(ModItems.CHOCOLATE_CAKE.get()).getMaxStackSize() == new ItemStack(Items.CAKE).getMaxStackSize(),
                "Chocolate cake item must retain vanilla stack size");
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void everyCandleKeepsChocolateCakeAndVanillaCandleMappingIntact(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "candle-cake-test"));
        player.setGameMode(GameType.SURVIVAL);
        var local = new BlockPos(2, 1, 2);
        var pos = helper.absolutePos(local);
        helper.setBlock(local.below(), Blocks.STONE);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (var entry : ModBlocks.CHOCOLATE_CANDLES.entrySet()) {
            var candle = entry.getKey();
            var stack = new ItemStack(candle.asItem());
            helper.setBlock(local, ModBlocks.CHOCOLATE_CAKE.get());
            helper.getLevel().getBlockState(pos).useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertBlockPresent(entry.getValue().get(), local);
            helper.assertTrue(CandleCakeBlock.byCandle(candle).getBlock() != entry.getValue().get(),
                    "Chocolate candle registration must never change vanilla cake behavior");
            player.getFoodData().setFoodLevel(10);
            helper.getLevel().getBlockState(pos).useWithoutItem(helper.getLevel(), player, hit);
            helper.assertBlockPresent(ModBlocks.CHOCOLATE_CAKE.get(), local);
            helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(CakeBlock.BITES) == 1, "Eating candle cake must leave six chocolate bites");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).stream().anyMatch(e -> e.getItem().is(candle.asItem())),
                    "Eating candle cake must return its candle");
        }
        helper.succeed();
    }
    @GameTest(template = "oven_test_empty")
    public static void chocolateCakeCraftingReturnsTheThreeMilkBuckets(GameTestHelper helper) {
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, java.util.List.of(
                new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET),
                new ItemStack(Items.SUGAR), new ItemStack(Items.EGG), new ItemStack(Items.COCOA_BEANS),
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT)));
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow().value();
        helper.assertTrue(recipe.assemble(input, helper.getLevel().registryAccess()).is(ModItems.CHOCOLATE_CAKE.get()),
                "Chocolate cake crafting must produce the registered cake item");
        var remaining = recipe.getRemainingItems(input);
        helper.assertTrue(remaining.stream().filter(stack -> stack.is(Items.BUCKET)).mapToInt(ItemStack::getCount).sum() == 3,
                "Chocolate cake must return the three empty milk buckets exactly as vanilla does");
        helper.succeed();
    }
}
