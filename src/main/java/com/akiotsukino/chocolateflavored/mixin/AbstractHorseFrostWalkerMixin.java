package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
abstract class AbstractHorseFrostWalkerMixin {
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void chocolateflavored$runBodyArmorLocationEffects(CallbackInfo ci) {
        AbstractHorse horse = (AbstractHorse)(Object)this;
        if (horse.level() instanceof ServerLevel serverLevel) {
            ItemStack armor = horse.getItemBySlot(EquipmentSlot.BODY);
            if (!armor.isEmpty()) {
                EnchantmentHelper.runLocationChangedEffects(serverLevel, armor, horse, EquipmentSlot.BODY);
            }
        }
    }
}
