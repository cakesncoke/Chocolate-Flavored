// Adapted from Farmer's Delight, Copyright (c) 2020 vectorwing (MIT).
// See THIRD_PARTY_NOTICES.md and licenses/FarmersDelight-MIT.txt.
package com.akiotsukino.chocolateflavored.client.recipebook;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import com.akiotsukino.chocolateflavored.recipe.CookingPotRecipe;
import com.akiotsukino.chocolateflavored.registry.ModRecipes;

public class RecipeCategories
{
	public static RecipeBookCategories COOKING_SEARCH = RecipeBookCategories.valueOf("CHOCOLATEFLAVORED_COOKING_SEARCH");
	public static RecipeBookCategories COOKING_MEALS = RecipeBookCategories.valueOf("CHOCOLATEFLAVORED_COOKING_MEALS");
	public static RecipeBookCategories COOKING_DRINKS = RecipeBookCategories.valueOf("CHOCOLATEFLAVORED_COOKING_DRINKS");
	public static RecipeBookCategories COOKING_MISC = RecipeBookCategories.valueOf("CHOCOLATEFLAVORED_COOKING_MISC");

	public static void init(RegisterRecipeBookCategoriesEvent event) {
		event.registerBookCategories(RecipeBookType.valueOf("CHOCOLATEFLAVORED_COOKING"), ImmutableList.of(COOKING_SEARCH, COOKING_MEALS, COOKING_DRINKS, COOKING_MISC));
		event.registerAggregateCategory(COOKING_SEARCH, ImmutableList.of(COOKING_MEALS, COOKING_DRINKS, COOKING_MISC));
		event.registerRecipeCategoryFinder(ModRecipes.COOKING_TYPE.get(), recipe ->
		{
			if (recipe.value() instanceof CookingPotRecipe cookingRecipe) {
				CookingPotRecipeBookTab tab = cookingRecipe.getRecipeBookTab();
				if (tab != null) {
					return switch (tab) {
						case MEALS -> COOKING_MEALS;
						case DRINKS -> COOKING_DRINKS;
						case MISC -> COOKING_MISC;
					};
				}
			}
			return COOKING_MISC;
		});
	}
}
