package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.recipe.OvenRecipe;
import com.akiotsukino.chocolateflavored.recipe.CookingPotRecipe;
import com.akiotsukino.chocolateflavored.recipe.FoodServingRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ChocolateFlavored.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ChocolateFlavored.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<OvenRecipe>> OVEN_TYPE = TYPES.register("oven_cooking", () -> new RecipeType<>() {
        @Override
        public String toString() { return ChocolateFlavored.MOD_ID + ":oven_cooking"; }
    });
    public static final DeferredHolder<RecipeSerializer<?>, OvenRecipe.Serializer> OVEN_SERIALIZER = SERIALIZERS.register("oven_cooking", OvenRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CookingPotRecipe>> COOKING_TYPE = TYPES.register("cooking", () -> new RecipeType<>() {
        @Override
        public String toString() { return ChocolateFlavored.MOD_ID + ":cooking"; }
    });
    public static final DeferredHolder<RecipeSerializer<?>, CookingPotRecipe.Serializer> COOKING_SERIALIZER = SERIALIZERS.register("cooking", CookingPotRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FoodServingRecipe>> FOOD_SERVING_SERIALIZER =
            SERIALIZERS.register("food_serving", () -> new SimpleCraftingRecipeSerializer<>(FoodServingRecipe::new));

    private ModRecipes() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
