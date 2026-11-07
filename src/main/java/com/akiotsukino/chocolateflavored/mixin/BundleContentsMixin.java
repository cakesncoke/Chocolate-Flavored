package com.akiotsukino.chocolateflavored.mixin;

import java.util.List;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

@Mixin(BundleContents.Mutable.class)
abstract class BundleContentsMixin {
    @Inject(method = "tryInsert", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$restrictMixedStackability(ItemStack incoming, CallbackInfoReturnable<Integer> cir) {
        List<ItemStack> contents = ((BundleContents.Mutable)(Object)this).itemCopies().toList();
        if (!contents.isEmpty()) {
            boolean containsUnstackable = contents.stream().anyMatch(stack -> stack.getMaxStackSize() == 1);
            boolean incomingUnstackable = incoming.getMaxStackSize() == 1;
            if (containsUnstackable != incomingUnstackable) cir.setReturnValue(0);
        }
    }

    @Inject(method = "getStackedWeight(Lnet/minecraft/world/item/ItemStack;)Lorg/apache/commons/lang3/math/Fraction;",
            at = @At("HEAD"), cancellable = true)
    private static void chocolateflavored$quarterWeightForUnstackables(ItemStack stack,
            CallbackInfoReturnable<Fraction> cir) {
        if (stack.getMaxStackSize() == 1) cir.setReturnValue(Fraction.getFraction(1, 4));
    }
}
