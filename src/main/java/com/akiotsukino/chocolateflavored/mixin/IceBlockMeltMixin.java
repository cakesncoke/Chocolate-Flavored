package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** In the Nether, ice touching crying obsidian melts into water instead of evaporating. */
@Mixin(IceBlock.class)
abstract class IceBlockMeltMixin {
    @Inject(method = "melt", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$preserveWaterNearCryingObsidian(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (level.dimension() != Level.NETHER) return;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(Blocks.CRYING_OBSIDIAN)) {
                // Same updates as vanilla's non-evaporating branch of IceBlock.melt.
                level.setBlockAndUpdate(pos, IceBlock.meltsInto());
                level.neighborChanged(pos, IceBlock.meltsInto().getBlock(), null);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
                ci.cancel();
                return;
            }
        }
    }
}
