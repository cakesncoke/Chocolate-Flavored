package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.akiotsukino.chocolateflavored.trim.TrimAbilities;
@Mixin(UseDuration.class)
abstract class TrimBowVisualMixin {
    @Inject(method="useDuration",at=@At("RETURN"),cancellable=true)
    private static void cf$pull(ItemStack item,LivingEntity owner,CallbackInfoReturnable<Integer> cir){if(item.getItem() instanceof BowItem && TrimAbilities.has(owner,EquipmentSlot.LEGS,"sentry"))cir.setReturnValue((int)(cir.getReturnValue()/.8f));}
}
