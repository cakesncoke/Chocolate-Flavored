package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.akiotsukino.chocolateflavored.trim.*;
@Mixin(LocalPlayer.class)
abstract class TrimLocalPlayerMixin {
    @Inject(method="itemUseSpeedMultiplier",at=@At("RETURN"),cancellable=true)
    private void cf$drawMove(CallbackInfoReturnable<Float> cir) { var p=(LocalPlayer)(Object)this; if(TrimAbilities.has(p,EquipmentSlot.FEET,"sentry") && TrimTriggers.ranged(p.getUseItem())) cir.setReturnValue(1f); }
}
