package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.block.OvenBlock;
import com.akiotsukino.chocolateflavored.block.entity.OvenBlockEntity;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Enabled only in NeoForge's GameTest runs. Tests use actual registered blocks, fuels and recipes. */
@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OvenGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    @GameTest(template = "oven_test_empty")
    public static void fuelRequiresManualIgnitionAndRelighting(GameTestHelper helper) {
        OvenBlockEntity oven = place(helper);
        helper.assertTrue(!oven.getBlockState().getValue(OvenBlock.LIT), "New Oven must start unlit");
        helper.assertTrue(!oven.ignite(), "Empty Oven must refuse ignition");
        ItemStack sticks = new ItemStack(Items.STICK, 2);
        helper.assertTrue(oven.addFuel(sticks, false, false) == 1 && sticks.getCount() == 1, "One click queues exactly one fuel");
        tick(helper, oven, 10);
        helper.assertTrue(oven.getBurnTime() == 0 && !oven.getBlockState().getValue(OvenBlock.LIT), "Fuel insertion must not ignite");
        helper.assertTrue(oven.ignite(), "Fuelled Oven should ignite");
        int duration = OvenBlockEntity.fuelTicks(new ItemStack(Items.STICK));
        tick(helper, oven, duration);
        helper.assertTrue(!oven.getBlockState().getValue(OvenBlock.LIT), "Last fuel tick must extinguish");
        oven.addFuel(sticks, false, false);
        tick(helper, oven, 10);
        helper.assertTrue(!oven.getBlockState().getValue(OvenBlock.LIT), "Refuelling must require another ignition");
        helper.assertTrue(oven.ignite(), "Refuelled Oven can be manually relit");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void sixSlotsCookCustomAndCampfireRecipes(GameTestHelper helper) {
        OvenBlockEntity oven = place(helper);
        ItemStack wheat = new ItemStack(Items.WHEAT, 4);
        ItemStack beef = new ItemStack(Items.BEEF, 3);
        for (int i = 0; i < 3; i++) {
            helper.assertTrue(oven.placeFood(wheat, false), "Oven-only recipe must be accepted");
            helper.assertTrue(oven.placeFood(beef, false), "Campfire fallback must be accepted");
        }
        helper.assertTrue(!oven.placeFood(wheat, false) && wheat.getCount() == 1, "Seventh food must remain in hand");
        helper.assertTrue(oven.findRecipe(new ItemStack(Items.IRON_ORE)).isEmpty(), "Furnace recipes must not leak into Oven");
        oven.addFuel(new ItemStack(Items.COAL), false, false);
        oven.ignite();
        tick(helper, oven, 600);
        helper.assertTrue(outputCount(helper, Items.BREAD) == 3, "Three wheat must yield three bread");
        helper.assertTrue(outputCount(helper, Items.COOKED_BEEF) == 3, "Three campfire inputs must yield three cooked beef");
        for (int i = 0; i < 6; i++) helper.assertTrue(oven.getItems().getStackInSlot(i).isEmpty(), "Cooked slots must clear");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void fuelQueueAndBucketRemainders(GameTestHelper helper) {
        OvenBlockEntity oven = place(helper);
        ItemStack coal = new ItemStack(Items.COAL, 2);
        oven.addFuel(coal, true, false);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        helper.assertTrue(oven.addFuel(planks, false, false) == 0 && planks.getCount() == 1, "Mixed queued fuel must not be lost");
        oven.ignite();
        helper.assertTrue(oven.getFuel().getCount() == 1, "Ignition consumes one unit, not the stack");
        tick(helper, oven, OvenBlockEntity.fuelTicks(new ItemStack(Items.COAL)) + 1);
        helper.assertTrue(oven.getBlockState().getValue(OvenBlock.LIT) && oven.getFuel().isEmpty(), "Next queued unit continues the fire");
        oven.extinguish();
        ItemStack lava = new ItemStack(Items.LAVA_BUCKET);
        oven.addFuel(lava, false, false);
        helper.assertTrue(lava.isEmpty() && oven.ignite(), "Lava bucket can fuel Oven");
        helper.assertTrue(outputCount(helper, Items.BUCKET) == 1, "Lava bucket must return exactly one bucket");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void saveReloadPreservesFuelAndProgress(GameTestHelper helper) {
        OvenBlockEntity oven = place(helper);
        oven.addFuel(new ItemStack(Items.COAL, 2), true, false);
        oven.placeFood(new ItemStack(Items.WHEAT), false);
        oven.ignite();
        tick(helper, oven, 50);
        var saved = oven.saveWithoutMetadata(helper.getLevel().registryAccess());
        OvenBlockEntity loaded = new OvenBlockEntity(oven.getBlockPos(), oven.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded.getBurnTime() == oven.getBurnTime() && loaded.getFuel().getCount() == 1, "Fuel must survive reload");
        helper.assertTrue(loaded.getItems().getStackInSlot(0).is(Items.WHEAT), "Food must survive reload");
        tick(helper, loaded, 149);
        helper.assertTrue(outputCount(helper, Items.BREAD) == 0, "Reload must not finish food early");
        tick(helper, loaded, 1);
        helper.assertTrue(outputCount(helper, Items.BREAD) == 1, "Reload must preserve the first 50 cooking ticks");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void extinguishingAndCoveringPreserveQueuedItems(GameTestHelper helper) {
        OvenBlockEntity oven = place(helper);
        oven.addFuel(new ItemStack(Items.COAL, 2), true, false);
        oven.placeFood(new ItemStack(Items.WHEAT), false);
        oven.ignite();
        tick(helper, oven, 50);
        oven.extinguish();
        helper.assertTrue(oven.getBurnTime() == 0 && oven.getFuel().getCount() == 1, "Extinguish keeps queued fuel only");
        tick(helper, oven, 25);
        helper.assertTrue(oven.saveWithoutMetadata(helper.getLevel().registryAccess()).getIntArray("Progress")[0] == 0, "Unlit food cools down");
        helper.setBlock(POS.above(), Blocks.STONE);
        tick(helper, oven, 1);
        helper.assertTrue(outputCount(helper, Items.WHEAT) == 1 && oven.getItems().getStackInSlot(0).isEmpty(), "Cover ejects food exactly once");
        helper.assertTrue(oven.takeFuel().getCount() == 1 && oven.takeFuel().isEmpty(), "Fuel retrieval cannot duplicate items");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void breakingDropsFoodAndUnburnedFuelOnce(GameTestHelper helper) {
        OvenBlockEntity oven = place(helper);
        oven.placeFood(new ItemStack(Items.WHEAT, 2), false);
        oven.addFuel(new ItemStack(Items.COAL, 2), true, false);
        oven.ignite();
        helper.setBlock(POS, Blocks.AIR);
        helper.assertTrue(outputCount(helper, Items.WHEAT) == 1, "Breaking drops stored food");
        helper.assertTrue(outputCount(helper, Items.COAL) == 1, "Breaking drops only queued, not already consumed fuel");
        helper.succeed();
    }

    private static OvenBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.OVEN.get());
        return (OvenBlockEntity) helper.getBlockEntity(POS);
    }

    private static void tick(GameTestHelper helper, OvenBlockEntity oven, int count) {
        for (int i = 0; i < count; i++) OvenBlockEntity.serverTick(helper.getLevel(), oven.getBlockPos(),
                helper.getLevel().getBlockState(oven.getBlockPos()), oven);
    }

    private static int outputCount(GameTestHelper helper, Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(POS)).inflate(1))
                .stream().filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
    }
}
