package com.akiotsukino.chocolateflavored.cooking;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

/** Server recipes use current vanilla codecs, so datapacks and KubeJS share one format. */
public final class CookingRecipes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ChocolateFlavored.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ChocolateFlavored.MODID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<OvenRecipe>> OVEN = TYPES.register("oven", () -> new RecipeType<>() {
        @Override public String toString() { return "chocolateflavored:oven"; }
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<PotRecipe>> COOKING_POT = TYPES.register("cooking_pot", () -> new RecipeType<>() {
        @Override public String toString() { return "chocolateflavored:cooking_pot"; }
    });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OvenRecipe>> OVEN_SERIALIZER = SERIALIZERS.register("oven", () -> new RecipeSerializer<>(OvenRecipe.CODEC, ByteBufCodecs.fromCodecWithRegistries(OvenRecipe.CODEC.codec())));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PotRecipe>> POT_SERIALIZER = SERIALIZERS.register("cooking_pot", () -> new RecipeSerializer<>(PotRecipe.CODEC, ByteBufCodecs.fromCodecWithRegistries(PotRecipe.CODEC.codec())));
    public static void register(IEventBus bus) { TYPES.register(bus); SERIALIZERS.register(bus); }

    private interface ProcessingRecipe<I extends RecipeInput> extends Recipe<I> {
        @Override default boolean isSpecial() { return true; }
        @Override default boolean showNotification() { return false; }
        @Override default String group() { return ""; }
        @Override default PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
        @Override default RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
    }
    public record OvenRecipe(Ingredient ingredient, int inputCount, ItemStackTemplate result, int cookingTime, float experience) implements ProcessingRecipe<SingleRecipeInput> {
        public static final MapCodec<OvenRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(OvenRecipe::ingredient),
                Codec.intRange(1, 64).optionalFieldOf("input_count", 1).forGetter(OvenRecipe::inputCount),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(OvenRecipe::result),
                Codec.intRange(1, 32767).optionalFieldOf("cookingtime", 200).forGetter(OvenRecipe::cookingTime),
                Codec.floatRange(0, 1000).optionalFieldOf("experience", 0.0F).forGetter(OvenRecipe::experience)
        ).apply(i, OvenRecipe::new));
        @Override public boolean matches(SingleRecipeInput input, Level level) { return ingredient.test(input.item()) && input.item().getCount() >= inputCount; }
        @Override public ItemStack assemble(SingleRecipeInput input) { return result.create(); }
        @Override public RecipeSerializer<OvenRecipe> getSerializer() { return OVEN_SERIALIZER.get(); }
        @Override public RecipeType<OvenRecipe> getType() { return OVEN.get(); }
    }
    public record PotInput(List<ItemStack> items) implements RecipeInput {
        public PotInput { items = List.copyOf(items); }
        @Override public int size() { return items.size(); }
        @Override public ItemStack getItem(int slot) { return items.get(slot); }
    }
    public record PotRecipe(List<Ingredient> ingredients, ItemStackTemplate result, Optional<ItemStackTemplate> container, int cookingTime, float experience) implements ProcessingRecipe<PotInput> {
        public PotRecipe { ingredients = List.copyOf(ingredients); }
        public static final MapCodec<PotRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.listOf(1, 6).fieldOf("ingredients").forGetter(PotRecipe::ingredients),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(PotRecipe::result),
                ItemStackTemplate.CODEC.optionalFieldOf("container").forGetter(PotRecipe::container),
                Codec.intRange(1, 32767).optionalFieldOf("cookingtime", 200).forGetter(PotRecipe::cookingTime),
                Codec.floatRange(0, 1000).optionalFieldOf("experience", 0.0F).forGetter(PotRecipe::experience)
        ).apply(i, PotRecipe::new));
        @Override public boolean matches(PotInput input, Level level) {
            var stacks = input.items().stream().filter(s -> !s.isEmpty()).toList();
            return stacks.size() == ingredients.size() && match(stacks, 0, 0);
        }
        // Backtracking avoids greedily misassigning overlapping tag/item ingredients (at most six).
        private boolean match(List<ItemStack> stacks, int index, int used) {
            if (index == ingredients.size()) return true;
            for (int slot = 0; slot < stacks.size(); slot++) {
                int bit = 1 << slot;
                if ((used & bit) == 0 && ingredients.get(index).test(stacks.get(slot)) && match(stacks, index + 1, used | bit)) return true;
            }
            return false;
        }
        @Override public ItemStack assemble(PotInput input) { return result.create(); }
        public ItemStack servingContainer() {
            if (container.isPresent()) return container.get().withCount(1).create();
            var stack = result.create();
            var remainder = stack.getCraftingRemainder();
            if (remainder != null) return remainder.withCount(1).create();
            var use = stack.get(net.minecraft.core.component.DataComponents.USE_REMAINDER);
            return use == null ? ItemStack.EMPTY : use.convertInto().withCount(1).create();
        }
        @Override public RecipeSerializer<PotRecipe> getSerializer() { return POT_SERIALIZER.get(); }
        @Override public RecipeType<PotRecipe> getType() { return COOKING_POT.get(); }
    }
    private CookingRecipes() {}
}
