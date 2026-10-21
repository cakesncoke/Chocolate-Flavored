package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.mob.expansion.SunkenSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep native arrow resources and crossbow handling while making this aquatic archer functional. */
@Mixin(AbstractArrow.class)
abstract class SunkenArrowMixin {
    @Inject(method="getWaterInertia",at=@At("RETURN"),cancellable=true)
    private void chocolateflavored$sunkenWaterDrag(CallbackInfoReturnable<Float> callback) {
        if (((AbstractArrow)(Object)this).getOwner() instanceof SunkenSkeleton)
            callback.setReturnValue(Math.max(callback.getReturnValue(),.99F));
    }
}
