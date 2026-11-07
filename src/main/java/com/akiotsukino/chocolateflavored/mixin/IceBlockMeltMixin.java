package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
abstract class IceBlockMeltMixin {
    @Inject(method = "melt", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$preserveWaterNearCryingObsidian(BlockState state, Level level,
            BlockPos pos, CallbackInfo ci) {
        if (level.dimension() != Level.NETHER) return;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(Blocks.CRYING_OBSIDIAN)) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                ci.cancel();
                return;
            }
        }
    }
}
