package com.akiotsukino.chocolateflavored.mixin;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class UndeadBodyMixin {
 @Inject(method="isInvertedHealAndHarm",at=@At("HEAD"),cancellable=true)
 private void chocolateflavored$undeadBody(CallbackInfoReturnable<Boolean> cir){if(((LivingEntity)(Object)this).hasEffect(MobContent.UNDEAD_BODY))cir.setReturnValue(true);}
}
