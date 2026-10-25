package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.trim.TrimTriggers;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only trim-triggered bolts suppress ignition; damage and transformations stay native. */
@Mixin(LightningBolt.class)
abstract class TrimLightningMixin {
    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    private void cf$noFire(int additionalSources, CallbackInfo ci) {
        if (((LightningBolt)(Object)this).getPersistentData().getBooleanOr(TrimTriggers.key("fireless"), false)) ci.cancel();
    }
}
