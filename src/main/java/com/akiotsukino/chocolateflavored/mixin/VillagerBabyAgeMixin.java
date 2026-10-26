package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AgeableMob.class)
abstract class VillagerBabyAgeMixin {
    @Inject(method = "getBabyStartAge", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$villagerBabyAge(CallbackInfoReturnable<Integer> callback) {
        if ((Object)this instanceof Villager) callback.setReturnValue(-35 * 60 * 20);
    }
}
