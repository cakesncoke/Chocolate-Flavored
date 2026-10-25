package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(CrossbowItem.class)
abstract class TrimCrossbowMixin {
    @Inject(method="getChargeDuration",at=@At("RETURN"),cancellable=true)
    private static void cf$draw(ItemStack stack,LivingEntity user,CallbackInfoReturnable<Integer> cir) { if(has(user,EquipmentSlot.LEGS,"sentry")) cir.setReturnValue(Math.max(1,(int)Math.ceil(cir.getReturnValue()*.8))); }
}
