package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
abstract class CampfirePlacementMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void chocolateflavored$placeUnlit(BlockPlaceContext context, CallbackInfoReturnable<BlockState> callback) {
        BlockState state = callback.getReturnValue();
        if (state != null) callback.setReturnValue(state.setValue(CampfireBlock.LIT, false));
    }
}
