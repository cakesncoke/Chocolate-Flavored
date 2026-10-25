package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.trim.TrimMovementContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CollisionContext.class)
interface TrimCollisionContextMixin {
    @Inject(method = "withPosition", at = @At("RETURN"))
    private static void cf$validation(Entity entity, double position, CallbackInfoReturnable<CollisionContext> cir) {
        if (cir.getReturnValue() instanceof TrimMovementContext context) context.chocolateflavored$movementValidation();
    }
}
