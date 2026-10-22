package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.trader.TraderRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class TraderMountMixin {
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("RETURN"))
    private void chocolateflavored$rememberKeptTrader(Entity vehicle, boolean force, boolean sendEvents,
                                                     CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue() && (Object)this instanceof WanderingTrader trader)
            TraderRules.mounted(trader);
    }
}
