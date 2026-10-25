package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(Entity.class)
abstract class TrimEntityMixin {
    @Inject(method="updateSwimming",at=@At("HEAD"),cancellable=true)
    private void cf$underwaterSprint(CallbackInfo ci){
        if((Object)this instanceof net.minecraft.world.entity.player.Player p && has(p,EquipmentSlot.FEET,"tide") && p.isInWater() && p.onGround() && !p.isJumping()){
            p.setSwimming(false);ci.cancel();
        }
    }
    @Inject(method="getMaxAirSupply", at=@At("RETURN"), cancellable=true)
    private void cf$air(CallbackInfoReturnable<Integer> cir) { if ((Object)this instanceof LivingEntity e && e.getAttributes() != null && has(e, EquipmentSlot.CHEST, "tide")) cir.setReturnValue(cir.getReturnValue() + (full(e, "tide") ? 2400 : 1200)); }
}
