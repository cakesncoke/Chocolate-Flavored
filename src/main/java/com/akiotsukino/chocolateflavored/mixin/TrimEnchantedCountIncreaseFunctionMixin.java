package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import com.akiotsukino.chocolateflavored.trim.TrimLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(EnchantedCountIncreaseFunction.class)
abstract class TrimEnchantedCountIncreaseFunctionMixin {
    @Redirect(method="run",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int cf$looting(Holder<Enchantment> ench,LivingEntity entity,net.minecraft.world.item.ItemStack stack,LootContext context) {return TrimLoot.looting(ench,entity,context);}
}
