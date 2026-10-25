package com.akiotsukino.chocolateflavored.mixin;
import com.akiotsukino.chocolateflavored.trim.TrimMining;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayDeque;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ServerPlayerGameMode.class)
abstract class TrimMiningMixin {
    @Shadow protected ServerPlayer player;
    @Shadow protected ServerLevel level;
    @Unique private final ArrayDeque<BlockState> cf$states=new ArrayDeque<>();
    @Inject(method="destroyBlock",at=@At("HEAD")) private void cf$before(BlockPos pos,CallbackInfoReturnable<Boolean> cir) { cf$states.push(level.getBlockState(pos)); }
    @Inject(method="destroyBlock",at=@At("RETURN")) private void cf$after(BlockPos pos,CallbackInfoReturnable<Boolean> cir) { TrimMining.afterBreak(player,pos,cf$states.pop()); }
}
