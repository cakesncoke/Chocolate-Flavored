package com.akiotsukino.chocolateflavored.recipe;

import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Single-input cooking recipes used only by the Oven. Campfire fallback is handled by its block entity. */
public record OvenRecipe(Ingredient ingredient, ItemStack result, int cookingTime) implements Recipe<SingleRecipeInput> {
    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }

    @Override
    public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, ingredient); }

    @Override
    public boolean isSpecial() { return true; }

    @Override
    public ItemStack getToastSymbol() { return new ItemStack(ModBlocks.OVEN.get()); }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.OVEN_SERIALIZER.get(); }

    @Override
    public RecipeType<?> getType() { return ModRecipes.OVEN_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<OvenRecipe> {
        private static final MapCodec<OvenRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(OvenRecipe::ingredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(OvenRecipe::result),
                Codec.intRange(1, 72000).optionalFieldOf("cookingtime", 200).forGetter(OvenRecipe::cookingTime)
        ).apply(instance, OvenRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, OvenRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
                    buffer.writeVarInt(recipe.cookingTime());
                },
                buffer -> new OvenRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                        ItemStack.STREAM_CODEC.decode(buffer), buffer.readVarInt()));

        @Override
        public MapCodec<OvenRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OvenRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
