package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.block.entity.CookingPotBlockEntity;
import com.akiotsukino.chocolateflavored.block.entity.OvenBlockEntity;
import com.akiotsukino.chocolateflavored.recipe.FoodServingRecipe;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CookingPotGameTests {
    private static final BlockPos POS = new BlockPos(2, 2, 2);

    @GameTest(template = "oven_test_empty")
    public static void onlyLitOvenHeatsAndCooksMeals(GameTestHelper helper) {
        helper.setBlock(POS.below(), ModBlocks.OVEN.get());
        CookingPotBlockEntity pot = place(helper);
        mushrooms(pot, 1);
        helper.assertTrue(!pot.isHeated(), "Unlit Oven must not heat the pot");
        tick(helper, pot, 250);
        helper.assertTrue(pot.getMeal().isEmpty(), "Unheated ingredients must not cook");
        OvenBlockEntity oven = (OvenBlockEntity) helper.getBlockEntity(POS.below());
        oven.addFuel(new ItemStack(Items.COAL), false, false);
        oven.ignite();
        helper.assertTrue(pot.isHeated(), "Lit Oven must be a heat source");
        tick(helper, pot, 200);
        helper.assertTrue(pot.getMeal().is(Items.MUSHROOM_STEW), "Registered recipe must cook on a lit Oven");
        helper.assertTrue(pot.getContainer().is(Items.BOWL), "Meal must require its serving container");
        oven.extinguish();
        helper.assertTrue(!pot.isHeated(), "Extinguished Oven must stop supplying heat");
        helper.assertTrue(RecipeBookType.valueOf("CHOCOLATEFLAVORED_COOKING") != null, "Recipe book enum must exist on server");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void heatConductorsAndSidedAutomationWork(GameTestHelper helper) {
        helper.setBlock(POS.below(2), Blocks.MAGMA_BLOCK);
        helper.setBlock(POS.below(), Blocks.HOPPER);
        CookingPotBlockEntity pot = place(helper);
        helper.assertTrue(pot.isHeated(), "Hopper must conduct heat from magma");
        var top = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pot.getBlockPos(), Direction.UP);
        var side = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pot.getBlockPos(), Direction.NORTH);
        helper.assertTrue(top != null && side != null, "Item capabilities must be registered");
        helper.assertTrue(top.insertItem(0, new ItemStack(Items.BROWN_MUSHROOM), false).isEmpty(), "Top must accept ingredients");
        helper.assertTrue(top.insertItem(1, new ItemStack(Items.RED_MUSHROOM), false).isEmpty(), "Top must accept second ingredient");
        helper.assertTrue(!side.insertItem(0, new ItemStack(Items.CARROT), false).isEmpty(), "Sides must reject ingredient insertion");
        helper.assertTrue(side.insertItem(CookingPotBlockEntity.CONTAINER_SLOT, new ItemStack(Items.BOWL), false).isEmpty(), "Side must accept bowls");
        tick(helper, pot, 200);
        helper.assertTrue(side.extractItem(CookingPotBlockEntity.MEAL_DISPLAY_SLOT, 1, false).isEmpty(), "Automation cannot extract unserved meal");
        helper.assertTrue(side.extractItem(CookingPotBlockEntity.OUTPUT_SLOT, 1, false).is(Items.MUSHROOM_STEW), "Side must extract served food");
        helper.assertTrue(pot.getMeal().isEmpty() && pot.getInventory().getStackInSlot(CookingPotBlockEntity.CONTAINER_SLOT).isEmpty(), "Serving consumes one meal and one bowl");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void saveReloadAndBreakingPreserveStoredServings(GameTestHelper helper) {
        CookingPotBlockEntity pot = cooked(helper, 2);
        var saved = pot.saveWithoutMetadata(helper.getLevel().registryAccess());
        CookingPotBlockEntity loaded = new CookingPotBlockEntity(pot.getBlockPos(), pot.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded.getMeal().getCount() == 2 && loaded.getContainer().is(Items.BOWL), "Save/reload must preserve meals and container");
        pot.getInventory().setStackInSlot(0, new ItemStack(Items.CARROT));
        helper.getLevel().destroyBlock(pot.getBlockPos(), true);
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pot.getBlockPos()).inflate(1));
        var potDrops = drops.stream().filter(entity -> entity.getItem().is(ModItems.COOKING_POT.get())).toList();
        helper.assertTrue(potDrops.size() == 1, "Breaking must drop one Cooking Pot");
        ItemStack drop = potDrops.getFirst().getItem();
        helper.assertTrue(CookingPotBlockEntity.getMealFromItem(drop).getCount() == 2, "Loot must copy meal component, including servings beyond normal food stack size");
        helper.assertTrue(CookingPotBlockEntity.getContainerFromItem(drop).is(Items.BOWL), "Loot must copy serving container");
        helper.assertTrue(drops.stream().filter(entity -> entity.getItem().is(Items.CARROT)).mapToInt(entity -> entity.getItem().getCount()).sum() == 1, "Input inventory drops once");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void carriedPotCanServeInCraftingGrid(GameTestHelper helper) {
        ItemStack pot = cooked(helper, 2).getAsItem();
        var input = CraftingInput.of(2, 2, List.of(pot, new ItemStack(Items.BOWL), ItemStack.EMPTY, ItemStack.EMPTY));
        var recipe = new FoodServingRecipe(CraftingBookCategory.MISC);
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Carried pot and bowl must match serving recipe");
        helper.assertTrue(recipe.assemble(input, helper.getLevel().registryAccess()).is(Items.MUSHROOM_STEW), "Serving must yield one meal");
        ItemStack remainder = recipe.getRemainingItems(input).getFirst();
        helper.assertTrue(remainder.is(ModItems.COOKING_POT.get()) && CookingPotBlockEntity.getMealFromItem(remainder).getCount() == 1, "Pot remainder retains exactly one remaining serving");
        helper.succeed();
    }

    private static CookingPotBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.COOKING_POT.get());
        return (CookingPotBlockEntity) helper.getBlockEntity(POS);
    }
    private static void mushrooms(CookingPotBlockEntity pot, int count) {
        pot.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_MUSHROOM, count));
        pot.getInventory().setStackInSlot(1, new ItemStack(Items.RED_MUSHROOM, count));
    }
    private static CookingPotBlockEntity cooked(GameTestHelper helper, int count) {
        helper.setBlock(POS.below(), Blocks.MAGMA_BLOCK);
        CookingPotBlockEntity pot = place(helper);
        mushrooms(pot, count);
        tick(helper, pot, 200 * count);
        return pot;
    }
    private static void tick(GameTestHelper helper, CookingPotBlockEntity pot, int count) {
        for (int i = 0; i < count; i++) CookingPotBlockEntity.cookingTick(helper.getLevel(), pot.getBlockPos(), pot.getBlockState(), pot);
    }
}
