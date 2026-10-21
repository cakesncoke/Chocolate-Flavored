package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.mob.tuff.TuffGolemConstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CarvedPumpkinBlock.class)
public abstract class TuffGolemConstructionMixin {
    @Inject(method="trySpawnGolem",at=@At("HEAD"),cancellable=true)
    private void chocolateflavored$buildTuffGolem(Level level,BlockPos pos,CallbackInfo ci){
        if(level instanceof ServerLevel server&&TuffGolemConstruction.tryBuild(server,pos))ci.cancel();
    }
    @Inject(method="canSpawnGolem",at=@At("RETURN"),cancellable=true)
    private void chocolateflavored$allowTuffDispenser(LevelReader level,BlockPos pos,CallbackInfoReturnable<Boolean> cir){
        if(!cir.getReturnValue()&&TuffGolemConstruction.canBuild(level,pos))cir.setReturnValue(true);
    }
}
