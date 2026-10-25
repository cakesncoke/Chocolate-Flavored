package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(Entity.class)
abstract class TrimEntityMixin {
    @Inject(method="getMaxAirSupply", at=@At("RETURN"), cancellable=true)
    private void cf$air(CallbackInfoReturnable<Integer> cir) { if ((Object)this instanceof LivingEntity e && has(e, EquipmentSlot.CHEST, "tide")) cir.setReturnValue(cir.getReturnValue() + (full(e, "tide") ? 2400 : 1200)); }
}
