package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.echo.EchoAnchors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class EchoLodestoneRemovalMixin {
    @Inject(method="setBlockState",at=@At("RETURN"))
    private void chocolateflavored$invalidate(BlockPos pos,BlockState state,int flags,CallbackInfoReturnable<BlockState> cir){
        var old=cir.getReturnValue();var chunk=(LevelChunk)(Object)this;
        if(old!=null&&old.is(Blocks.LODESTONE)&&chunk.getLevel() instanceof ServerLevel server
                &&!chunk.getBlockState(pos).is(Blocks.LODESTONE))EchoAnchors.removed(server,pos);
    }
}
