package com.akiotsukino.chocolateflavored.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.Bees;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bundles hold up to four unstackable items, and never mix stackable with unstackable items.
 * Nested bundles and occupied beehives keep their vanilla weights.
 */
@Mixin(BundleContents.class)
abstract class BundleContentsMixin {
    @Unique
    private static final DataResult<Fraction> CHOCOLATEFLAVORED$UNSTACKABLE_WEIGHT = DataResult.success(Fraction.getFraction(1, 4));

    // getWeight is the single source for insertion, transfer, removal and total bundle weight.
    @Inject(method = "getWeight", at = @At("HEAD"), cancellable = true)
    private static void chocolateflavored$quarterWeightForUnstackables(ItemInstance item, CallbackInfoReturnable<DataResult<Fraction>> cir) {
        if (item.getMaxStackSize() == 1 && !item.has(DataComponents.BUNDLE_CONTENTS)
                && item.getOrDefault(DataComponents.BEES, Bees.EMPTY).bees().isEmpty()) {
            cir.setReturnValue(CHOCOLATEFLAVORED$UNSTACKABLE_WEIGHT);
        }
    }
}
