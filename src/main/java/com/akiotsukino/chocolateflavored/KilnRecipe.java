package com.akiotsukino.chocolateflavored;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * An Earthen Kiln recipe that needs a stack of {@code input_count} items (for example 3 wheat for bread).
 * The kiln also cooks anything a vanilla campfire can, so only batch recipes need this type.
 */
public record KilnRecipe(Ingredient ingredient, int inputCount, ItemStackTemplate result, int cookingTime, float experience)
        implements Recipe<SingleRecipeInput> {
    public static final MapCodec<KilnRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(KilnRecipe::ingredient),
            Codec.intRange(1, 64).optionalFieldOf("input_count", 1).forGetter(KilnRecipe::inputCount),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(KilnRecipe::result),
            Codec.intRange(1, 32767).optionalFieldOf("cookingtime", 200).forGetter(KilnRecipe::cookingTime),
            Codec.floatRange(0, 1000).optionalFieldOf("experience", 0.0F).forGetter(KilnRecipe::experience)
    ).apply(i, KilnRecipe::new));
    public static final RecipeSerializer<KilnRecipe> SERIALIZER =
            new RecipeSerializer<>(CODEC, ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item()) && input.item().getCount() >= inputCount;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<KilnRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<KilnRecipe> getType() {
        return ChocolateFlavored.KILN_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
