package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.core.Holder;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(VibrationSystem.User.class)
interface TrimVibrationMixin {
    @Inject(method="isValidVibration",at=@At("HEAD"),cancellable=true)
    default void cf$quiet(Holder<GameEvent> event,GameEvent.Context context,CallbackInfoReturnable<Boolean> cir) {
        if ((event==GameEvent.STEP || event==GameEvent.HIT_GROUND) && context.sourceEntity() instanceof LivingEntity e && has(e,EquipmentSlot.FEET,"silence")) cir.setReturnValue(false);
    }
}
