package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.trader.TraderRules;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
abstract class TraderAdditionMixin {
    @Inject(method = "addEntity", at = @At("RETURN"))
    private void chocolateflavored$confirmedTrader(EntityAccess entity, boolean loaded,
                                                   CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue() && entity instanceof WanderingTrader trader) TraderRules.added(trader, loaded);
    }
}
