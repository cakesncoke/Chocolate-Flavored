package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(AbstractBoat.class)
abstract class TrimBoatMixin {
    @Unique private Vec3 cf$before=Vec3.ZERO;
    @Inject(method="controlBoat",at=@At("HEAD")) private void cf$before(CallbackInfo ci){cf$before=((AbstractBoat)(Object)this).getDeltaMovement();}
    @Inject(method="controlBoat",at=@At("RETURN")) private void cf$after(CallbackInfo ci){var boat=(AbstractBoat)(Object)this;var p=rider(boat);if(p!=null && has(p,EquipmentSlot.LEGS,"coast"))boat.setDeltaMovement(cf$before.add(boat.getDeltaMovement().subtract(cf$before).multiply(1.2,1,1.2)));}
}
