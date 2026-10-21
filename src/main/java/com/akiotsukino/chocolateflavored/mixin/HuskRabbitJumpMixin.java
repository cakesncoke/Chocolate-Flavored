// SPDX-License-Identifier: LGPL-3.0-only
package com.akiotsukino.chocolateflavored.mixin;
import com.akiotsukino.chocolateflavored.mob.entity.HuskRabbit;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Rabbit.class)
public abstract class HuskRabbitJumpMixin {
    @Shadow private int jumpDelayTicks;
    @Inject(method="setLandingDelay", at=@At("RETURN"))
    private void chocolateflavored$jockeyDelay(CallbackInfo ci) {
        if ((Object)this instanceof HuskRabbit rabbit && rabbit.hasControllingPassenger()) jumpDelayTicks = 5;
    }
}
