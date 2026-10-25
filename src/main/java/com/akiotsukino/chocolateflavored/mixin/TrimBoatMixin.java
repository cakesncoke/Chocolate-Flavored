package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(AbstractBoat.class)
abstract class TrimBoatMixin {
    @ModifyVariable(method="controlBoat",at=@At(value="STORE"),ordinal=0)
    private float cf$boatSpeed(float acceleration) { var p=rider((AbstractBoat)(Object)this);return p!=null && has(p,EquipmentSlot.LEGS,"coast") ? acceleration*1.2f : acceleration; }
}
