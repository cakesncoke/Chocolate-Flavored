package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A nonempty bundle accepts only items matching its contents' stackability. Weights live in {@link BundleContentsMixin}. */
@Mixin(BundleContents.Mutable.class)
abstract class BundleMutableMixin {
    @Inject(method = "tryInsert", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$restrictMixedStackability(ItemStack incoming, CallbackInfoReturnable<Integer> cir) {
        var contents = ((BundleContents.Mutable)(Object)this).itemCopies().toList();
        if (contents.isEmpty()) return;
        boolean containsUnstackable = contents.stream().anyMatch(stack -> stack.getMaxStackSize() == 1);
        if (containsUnstackable != (incoming.getMaxStackSize() == 1)) cir.setReturnValue(0);
    }
}
