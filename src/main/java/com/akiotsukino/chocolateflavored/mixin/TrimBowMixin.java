package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
@Mixin(BowItem.class)
abstract class TrimBowMixin {
    @Redirect(method="releaseUsing",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    private float cf$draw(int held,ItemStack stack,Level level,LivingEntity user,int remaining) { return BowItem.getPowerForTime(has(user,EquipmentSlot.LEGS,"sentry") ? (int)(held/0.8f) : held); }
}
