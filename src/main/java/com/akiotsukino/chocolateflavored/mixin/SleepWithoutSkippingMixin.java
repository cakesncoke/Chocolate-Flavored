package com.akiotsukino.chocolateflavored.mixin;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
abstract class SleepWithoutSkippingMixin {
    // Suppress the complete vanilla skip/wake/weather-clear branch, not the level tick itself.
    // Native player sleeping checks still wake players when their bed rule stops allowing sleep.
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
    private boolean chocolateflavored$keepSleeping(SleepStatus status, int percentage, List<ServerPlayer> players) {
        return false;
    }

    @ModifyConstant(method = "announceSleepStatus", constant = @Constant(stringValue = "sleep.skipping_night"))
    private String chocolateflavored$sleepMessage(String original) {
        return "message.chocolateflavored.sleep_acceleration";
    }
}
