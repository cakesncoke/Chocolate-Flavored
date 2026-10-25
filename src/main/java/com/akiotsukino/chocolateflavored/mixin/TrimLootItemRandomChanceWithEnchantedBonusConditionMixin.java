package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import com.akiotsukino.chocolateflavored.trim.TrimLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LootItemRandomChanceWithEnchantedBonusCondition.class)
abstract class TrimLootItemRandomChanceWithEnchantedBonusConditionMixin {
    @Redirect(method="test",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int cf$looting(Holder<Enchantment> ench,LivingEntity entity,LootContext context) {return TrimLoot.looting(ench,entity,context);}
}
