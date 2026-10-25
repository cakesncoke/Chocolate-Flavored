package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(FireworkRocketEntity.class)
abstract class TrimFireworkMixin {
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean cf$noBoost(LivingEntity entity) { return entity.isFallFlying() && !has(entity,EquipmentSlot.CHEST,"spire"); }
}
