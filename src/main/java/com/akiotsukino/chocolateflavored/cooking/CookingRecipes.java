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
    public static final DeferredHolder<RecipeType<?>, RecipeType<MudKilnRecipe>> MUD_KILN = TYPES.register("mud_kiln", () -> new RecipeType<>() {
        @Override public String toString() { return "chocolateflavored:mud_kiln"; }
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<PotRecipe>> COOKING_POT = TYPES.register("cooking_pot", () -> new RecipeType<>() {
        @Override public String toString() { return "chocolateflavored:cooking_pot"; }
    });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MudKilnRecipe>> MUD_KILN_SERIALIZER = SERIALIZERS.register("mud_kiln", () -> new RecipeSerializer<>(MudKilnRecipe.CODEC, ByteBufCodecs.fromCodecWithRegistries(MudKilnRecipe.CODEC.codec())));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PotRecipe>> POT_SERIALIZER = SERIALIZERS.register("cooking_pot", () -> new RecipeSerializer<>(PotRecipe.CODEC, ByteBufCodecs.fromCodecWithRegistries(PotRecipe.CODEC.codec())));
    public static void register(IEventBus bus) { TYPES.addAlias(net.minecraft.resources.Identifier.parse("chocolateflavored:oven"), MUD_KILN.getId()); SERIALIZERS.addAlias(net.minecraft.resources.Identifier.parse("chocolateflavored:oven"), MUD_KILN_SERIALIZER.getId()); TYPES.register(bus); SERIALIZERS.register(bus); }

    private interface ProcessingRecipe<I extends RecipeInput> extends Recipe<I> {
        @Override default boolean isSpecial() { return true; }
        @Override default boolean showNotification() { return false; }
        @Override default String group() { return ""; }
        @Override default PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
        @Override default RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
    }
    public record MudKilnRecipe(Ingredient ingredient, int inputCount, ItemStackTemplate result, int cookingTime, float experience) implements ProcessingRecipe<SingleRecipeInput> {
        public static final MapCodec<MudKilnRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(MudKilnRecipe::ingredient),
                Codec.intRange(1, 64).optionalFieldOf("input_count", 1).forGetter(MudKilnRecipe::inputCount),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(MudKilnRecipe::result),
                Codec.intRange(1, 32767).optionalFieldOf("cookingtime", 200).forGetter(MudKilnRecipe::cookingTime),
                Codec.floatRange(0, 1000).optionalFieldOf("experience", 0.0F).forGetter(MudKilnRecipe::experience)
        ).apply(i, MudKilnRecipe::new));
        @Override public boolean matches(SingleRecipeInput input, Level level) { return ingredient.test(input.item()) && input.item().getCount() >= inputCount; }
        @Override public ItemStack assemble(SingleRecipeInput input) { return result.create(); }
        @Override public RecipeSerializer<MudKilnRecipe> getSerializer() { return MUD_KILN_SERIALIZER.get(); }
        @Override public RecipeType<MudKilnRecipe> getType() { return MUD_KILN.get(); }
    }
    public record PotInput(List<ItemStack> items) implements RecipeInput {
        public PotInput { items = List.copyOf(items); }
        @Override public int size() { return items.size(); }
        @Override public ItemStack getItem(int slot) { return items.get(slot); }
    }
    public record PotIngredient(Ingredient ingredient, int count) {
        public static final Codec<PotIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(PotIngredient::ingredient),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(PotIngredient::count)
        ).apply(i, PotIngredient::new));
        public boolean test(ItemStack stack) { return ingredient.test(stack) && stack.getCount() >= count; }
    }
    public record PotRecipe(List<PotIngredient> ingredients, List<ItemStackTemplate> results, float experience) implements ProcessingRecipe<PotInput> {
        public static final int COOKING_TIME = 200;
        public PotRecipe { ingredients = List.copyOf(ingredients); results = List.copyOf(results); }
        private static final Codec<List<ItemStackTemplate>> RESULTS = ItemStackTemplate.CODEC.listOf(1, 6).validate(values ->
                values.stream().map(ItemStackTemplate::create).anyMatch(stack -> stack.isEmpty() || stack.getCount() > Math.min(64, stack.getMaxStackSize()))
                        ? com.mojang.serialization.DataResult.error(() -> "Each pot result must fit a normal output stack")
                        : com.mojang.serialization.DataResult.success(values));
        public static final MapCodec<PotRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                PotIngredient.CODEC.listOf(1, 6).fieldOf("ingredients").forGetter(PotRecipe::ingredients),
                RESULTS.fieldOf("results").forGetter(PotRecipe::results),
                Codec.floatRange(0, 1000).optionalFieldOf("experience", 0.0F).forGetter(PotRecipe::experience)
        ).apply(i, PotRecipe::new));
        @Override public boolean matches(PotInput input, Level level) { return assignment(input) != null; }
        /** Every ingredient occupies one input slot. Backtracking handles overlapping tags and counts. */
        public int[] assignment(PotInput input) {
            if (input.items().stream().filter(stack -> !stack.isEmpty()).count() != ingredients.size()) return null;
            int[] slots = new int[ingredients.size()];
            return match(input, slots, 0, 0) ? slots : null;
        }
        private boolean match(PotInput input, int[] slots, int index, int used) {
            if (index == ingredients.size()) return true;
            for (int slot = 0; slot < input.size(); slot++) {
                int bit = 1 << slot;
                if ((used & bit) == 0 && ingredients.get(index).test(input.getItem(slot))) {
                    slots[index] = slot;
                    if (match(input, slots, index + 1, used | bit)) return true;
                }
            }
            return false;
        }
        public int cookingTime() { return COOKING_TIME; }
        public List<ItemStack> outputs() { return results.stream().map(ItemStackTemplate::create).toList(); }
        @Override public ItemStack assemble(PotInput input) { return results.getFirst().create(); }
        @Override public RecipeSerializer<PotRecipe> getSerializer() { return POT_SERIALIZER.get(); }
        @Override public RecipeType<PotRecipe> getType() { return COOKING_POT.get(); }
    }
    private CookingRecipes() {}
}
