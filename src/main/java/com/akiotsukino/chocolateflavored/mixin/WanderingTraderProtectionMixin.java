package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.trader.TraderRules;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
abstract class WanderingTraderProtectionMixin {
    @Inject(method = "maybeDespawn", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$keepTrader(CallbackInfo callback) {
        var trader = (WanderingTrader)(Object)this;
        TraderRules.observe(trader);
        if (TraderRules.protectedFromDespawn(trader)) callback.cancel();
    }
}
