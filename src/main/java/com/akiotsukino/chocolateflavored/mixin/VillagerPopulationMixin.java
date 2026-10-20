package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
abstract class VillagerPopulationMixin {
    // Covers both panic and gossip summons; player construction and outpost templates use other paths.
    @Inject(method = "spawnGolemIfNeeded", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$noGolemSummons(ServerLevel level, long timestamp, int villagersNeeded, CallbackInfo callback) {
        callback.cancel();
    }
}
