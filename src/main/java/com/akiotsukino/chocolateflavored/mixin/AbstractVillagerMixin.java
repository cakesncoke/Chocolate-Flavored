package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.gameplay.ItemRestrictions;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractVillager.class)
abstract class AbstractVillagerMixin {
    // 26.1 uses data-driven villager trades; filter both loaded and newly generated offers
    // before the server exposes them to a merchant menu. Do not recreate old trade events.
    @Inject(method = "getOffers", at = @At("RETURN"))
    private void chocolateflavored$filterOffers(CallbackInfoReturnable<MerchantOffers> callback) {
        callback.getReturnValue().removeIf(ItemRestrictions::forbiddenTrade);
    }
}
