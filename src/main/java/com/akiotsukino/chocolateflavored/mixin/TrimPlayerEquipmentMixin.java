package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.akiotsukino.chocolateflavored.trim.TrimAttributes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(PlayerEquipment.class)
abstract class TrimPlayerEquipmentMixin {
    @Shadow @Final private Player player;
    @Inject(method="set",at=@At("RETURN")) private void cf$changed(EquipmentSlot slot,ItemStack stack,CallbackInfoReturnable<ItemStack> cir){if(player.getAttributes()!=null && player.getInventory()!=null && !player.level().isClientSide())TrimAttributes.update(player);}
}
