package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CopperGameTests {
    @GameTest(template = "oven_test_empty")
    public static void copperKeepsStoneCombatAndMiningRestrictionsWithIronDurability(GameTestHelper helper) {
        Item[] copper = tools();
        Item[] stone = {Items.STONE_SWORD, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE};
        for (int i = 0; i < copper.length; i++) {
            ItemStack stack = new ItemStack(copper[i]);
            helper.assertTrue(stack.getMaxDamage() == Tiers.IRON.getUses() && stack.getMaxStackSize() == 1,
                    "Every copper tool must have iron durability and remain nonstackable");
            helper.assertTrue(stack.get(DataComponents.ATTRIBUTE_MODIFIERS).equals(new ItemStack(stone[i]).get(DataComponents.ATTRIBUTE_MODIFIERS)),
                    "Copper combat damage and attack speed must match the stone counterpart");
        }
        ItemStack pick = new ItemStack(ModItems.COPPER_PICKAXE.get());
        helper.assertTrue(pick.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 5.0F, "Copper mining speed must be between stone and iron");
        helper.assertTrue(pick.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()), "Copper pickaxe must harvest iron ore");
        helper.assertTrue(!pick.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState())
                && !pick.isCorrectToolForDrops(Blocks.GOLD_ORE.defaultBlockState())
                && !pick.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "Iron durability must not grant iron mining level");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void copperToolsRepairAndAcceptTheirVanillaEnchantments(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (Item tool : tools()) {
            ItemStack stack = new ItemStack(tool);
            helper.assertTrue(tool.isValidRepairItem(stack, new ItemStack(Items.COPPER_INGOT)), "Copper ingots must repair each tool");
            helper.assertTrue(!tool.isValidRepairItem(stack, new ItemStack(Items.IRON_INGOT)), "Iron must not become the repair material");
            helper.assertTrue(stack.getEnchantmentValue() == 13, "Copper must retain vanilla copper enchantability");
            helper.assertTrue(enchantments.getOrThrow(Enchantments.UNBREAKING).value().canEnchant(stack)
                    && enchantments.getOrThrow(Enchantments.MENDING).value().canEnchant(stack), "Item tags must enable durability enchantments");
        }
        helper.assertTrue(enchantments.getOrThrow(Enchantments.FORTUNE).value().canEnchant(new ItemStack(ModItems.COPPER_PICKAXE.get())), "Pickaxe tags must enable Fortune");
        helper.assertTrue(enchantments.getOrThrow(Enchantments.SHARPNESS).value().canEnchant(new ItemStack(ModItems.COPPER_SWORD.get())), "Sword tags must enable Sharpness");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void craftingToolsAndNuggetConversionUseTheRegisteredRecipes(GameTestHelper helper) {
        String[] names = {"sword", "pickaxe", "axe", "shovel", "hoe"};
        String[][] patterns = {{"X", "X", "#"}, {"XXX", " # ", " # "}, {"XX", "X#", " #"}, {"X", "#", "#"}, {"XX", " #", " #"}};
        for (int i = 0; i < names.length; i++) {
            var input = pattern(patterns[i], Items.COPPER_INGOT, Items.STICK);
            var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
            helper.assertTrue(recipe.isPresent(), "Copper tool crafting recipe must match its grid");
            helper.assertTrue(recipe.get().value().assemble(input, helper.getLevel().registryAccess()).is(tools()[i]), "Copper crafting must produce the correct tool");
        }
        var mirrored = pattern(new String[]{"XX", "#X", "# "}, Items.COPPER_INGOT, Items.STICK);
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, mirrored, helper.getLevel()).isPresent(), "Axe pattern must allow mirroring");
        var unpack = CraftingInput.of(1, 1, List.of(new ItemStack(Items.COPPER_INGOT)));
        var unpackRecipe = helper.getLevel().getRecipeManager().byKey(id("copper_nugget")).orElseThrow();
        // Look up by type and input too: this verifies the recipe's decoded ingredient format.
        var nuggetRecipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, unpack, helper.getLevel()).orElseThrow();
        helper.assertTrue(nuggetRecipe.id().equals(unpackRecipe.id()), "Ingot unpacking must use copper nugget recipe");
        ItemStack nuggets = nuggetRecipe.value().assemble(unpack, helper.getLevel().registryAccess());
        helper.assertTrue(nuggets.is(ModItems.COPPER_NUGGET.get()) && nuggets.getCount() == 9, "One ingot must produce nine nuggets");
        var pack = CraftingInput.of(3, 3, Collections.nCopies(9, new ItemStack(ModItems.COPPER_NUGGET.get())));
        ItemStack ingot = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pack, helper.getLevel()).orElseThrow()
                .value().assemble(pack, helper.getLevel().registryAccess());
        helper.assertTrue(ingot.is(Items.COPPER_INGOT) && ingot.getCount() == 1, "Nine nuggets must return exactly one ingot");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void damagedCopperToolsRecycleInFurnaceAndBlastFurnace(GameTestHelper helper) {
        for (Item tool : tools()) {
            ItemStack damaged = new ItemStack(tool);
            damaged.setDamageValue(100);
            var input = new SingleRecipeInput(damaged);
            var smelting = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, helper.getLevel()).orElseThrow();
            var blasting = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.BLASTING, input, helper.getLevel()).orElseThrow();
            ItemStack result = smelting.value().assemble(input, helper.getLevel().registryAccess());
            helper.assertTrue(result.is(ModItems.COPPER_NUGGET.get()) && result.getCount() == 1, "Damaged tools must smelt into one nugget");
            helper.assertTrue(blasting.value().assemble(input, helper.getLevel().registryAccess()).is(ModItems.COPPER_NUGGET.get()), "Damaged tools must also be recyclable in a blast furnace");
            helper.assertTrue(smelting.value().getCookingTime() == 200 && blasting.value().getCookingTime() == 100
                    && smelting.value().getExperience() == 0.1F && blasting.value().getExperience() == 0.1F, "Recycling time and XP must match vanilla");
        }
        helper.succeed();
    }

    private static Item[] tools() {
        return new Item[]{ModItems.COPPER_SWORD.get(), ModItems.COPPER_PICKAXE.get(), ModItems.COPPER_AXE.get(), ModItems.COPPER_SHOVEL.get(), ModItems.COPPER_HOE.get()};
    }
    private static CraftingInput pattern(String[] rows, Item material, Item handle) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String row : rows) for (char symbol : row.toCharArray()) stacks.add(symbol == 'X' ? new ItemStack(material) : symbol == '#' ? new ItemStack(handle) : ItemStack.EMPTY);
        return CraftingInput.of(rows[0].length(), rows.length, stacks);
    }
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, name); }
}
