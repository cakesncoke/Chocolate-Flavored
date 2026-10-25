package com.akiotsukino.chocolateflavored.mixin;
import com.akiotsukino.chocolateflavored.trim.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(EnchantmentHelper.class)
abstract class TrimEnchantmentMixin {
    @Inject(method="getDamageProtection", at=@At("RETURN"), cancellable=true)
    private static void cf$protection(ServerLevel level, LivingEntity e, DamageSource s, CallbackInfoReturnable<Float> cir) { cir.setReturnValue(cir.getReturnValue() + TrimCombat.protection(e,s)); }
    @Inject(method="getFishingLuckBonus", at=@At("RETURN"), cancellable=true)
    private static void cf$fishing(ServerLevel level, ItemStack rod, Entity fisher, CallbackInfoReturnable<Integer> cir) { if (fisher instanceof LivingEntity e && TrimAbilities.has(e, EquipmentSlot.CHEST, "coast")) cir.setReturnValue(cir.getReturnValue()+2); }
}
