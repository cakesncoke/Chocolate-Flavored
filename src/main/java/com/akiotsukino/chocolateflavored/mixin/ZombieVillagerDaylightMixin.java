package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
abstract class ZombieVillagerDaylightMixin {
    @Inject(method = "isSunSensitive", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$daylightSafe(CallbackInfoReturnable<Boolean> callback) {
        if ((Object)this instanceof ZombieVillager) callback.setReturnValue(false);
    }
}
