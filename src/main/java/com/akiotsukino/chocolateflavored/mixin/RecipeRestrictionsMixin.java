package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.gameplay.ItemRestrictions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep registry references valid while excluding restricted recipes from crafting and displays. */
@Mixin(RecipeManager.class)
abstract class RecipeRestrictionsMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"))
    private RecipeMap chocolateflavored$filterRecipes(HolderLookup<Recipe<?>> recipes,
            Operation<RecipeMap> original, HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        return original.call(((HolderLookup.RegistryLookup<Recipe<?>>) recipes).filterElements(recipe -> !ItemRestrictions.forbiddenRecipe(
                Recipe.DIRECT_CODEC.encodeStart(ops, recipe).getOrThrow())));
    }
}
