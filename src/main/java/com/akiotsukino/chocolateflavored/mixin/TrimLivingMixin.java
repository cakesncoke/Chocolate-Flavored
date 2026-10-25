package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.trim.*;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static com.akiotsukino.chocolateflavored.trim.TrimAbilities.*;
import static net.minecraft.world.entity.EquipmentSlot.*;

@Mixin(LivingEntity.class)
abstract class TrimLivingMixin {
    @Shadow private void travelInAir(Vec3 input) {}
    @Unique private LivingEntity cf$self() { return (LivingEntity)(Object)this; }
    @Unique private MobEffectInstance cf$virtual(Holder<MobEffect> effect) {
        LivingEntity e = cf$self();
        if (!(e instanceof Player p)) return null;
        if (effect == MobEffects.NIGHT_VISION && has(p, HEAD, "eye") && p.getY() < 0) return new MobEffectInstance(effect, 400, 0, false, false, false);
        if (effect == MobEffects.DOLPHINS_GRACE && has(p, LEGS, "tide") && p.isUnderWater()) return new MobEffectInstance(effect, 400, 0, false, false, false);
        if (effect == MobEffects.LEVITATION && full(p, "spire") && !p.onGround() && p.isShiftKeyDown() && !p.isPassenger()) return new MobEffectInstance(effect, 400, 0, false, false, false);
        return null;
    }
    @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
    private void cf$loadHealth(net.minecraft.world.level.storage.ValueInput input,CallbackInfo ci){
        if(cf$self() instanceof Player p && !p.level().isClientSide()){
            TrimAttributes.update(p);
            // Native loading clamps Health before equipment is decoded; restore only the saved value.
            p.setHealth(input.getFloatOr("Health",p.getHealth()));
        }
    }
    @Inject(method="onEquippedItemBroken",at=@At("TAIL"))
    private void cf$broken(net.minecraft.world.item.Item item,EquipmentSlot slot,CallbackInfo ci){if(cf$self() instanceof Player p && !p.level().isClientSide())TrimAttributes.update(p);}
    @Inject(method="hasEffect", at=@At("RETURN"), cancellable=true)
    private void cf$has(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) { if (cf$virtual(effect) != null) cir.setReturnValue(true); }
    @Inject(method="getEffect", at=@At("RETURN"), cancellable=true)
    private void cf$get(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) { if (cir.getReturnValue() == null) { var virtual = cf$virtual(effect); if (virtual != null) cir.setReturnValue(virtual); } }
    @Inject(method="canBeAffected", at=@At("HEAD"), cancellable=true)
    private void cf$immune(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) { if (has(cf$self(), HEAD, "dune") && (effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS))) cir.setReturnValue(false); }
    @Inject(method="checkTotemDeathProtection", at=@At("RETURN"), cancellable=true)
    private void cf$resurrection(DamageSource source, CallbackInfoReturnable<Boolean> cir) { if (!cir.getReturnValue() && TrimTriggers.resurrect(cf$self(), source)) cir.setReturnValue(true); }
    @Inject(method="getVisibilityPercent", at=@At("RETURN"), cancellable=true)
    private void cf$detection(Entity observer, CallbackInfoReturnable<Double> cir) {
        var e = cf$self(); if (!(e instanceof Player) || observer == null || observer.getType() == EntityType.WARDEN) return;
        double scale = 1;
        if (TrimCombat.hostile(observer) && has(e, HEAD, "silence")) scale = full(e, "silence") ? .6 : .85;
        if (observer.is(net.minecraft.tags.EntityTypeTags.ILLAGER) && has(e, HEAD, "vex")) scale = Math.min(scale, .5);
        if (observer instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin && has(e, HEAD, "snout")) scale = Math.min(scale, .5);
        if (observer.getType() == EntityType.DROWNED && has(e, HEAD, "coast")) scale = Math.min(scale, .5);
        if (observer.is(NETHER_HOSTILES) && has(e, HEAD, "rib")) scale = Math.min(scale, .75);
        cir.setReturnValue(cir.getReturnValue() * scale);
    }
    @Inject(method="canGlideUsing",at=@At("RETURN"),cancellable=true)
    private static void cf$gliderStack(net.minecraft.world.item.ItemStack stack,EquipmentSlot slot,CallbackInfoReturnable<Boolean> cir) {
        if(slot==CHEST && pattern(stack).equals("minecraft:spire") && stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE)!=null && stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE).slot()==CHEST && (!stack.isDamageableItem() || stack.getDamageValue()<stack.getMaxDamage()-1))cir.setReturnValue(true);
    }
    @Inject(method="jumpInLiquid",at=@At("HEAD"),cancellable=true)
    private void cf$jumpWater(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> type,CallbackInfo ci) {
        var e=cf$self();if(type==net.minecraft.tags.FluidTags.WATER && has(e,FEET,"tide") && e.onGround() && !e.isSwimming()){e.jumpFromGround();ci.cancel();}
    }
    @Inject(method="travelInFluid", at=@At("HEAD"), cancellable=true)
    private void cf$walkWater(Vec3 input, CallbackInfo ci) {
        var e = cf$self();
        if (has(e, FEET, "tide") && e.isInWater() && !e.isSwimming()) { travelInAir(input); ci.cancel(); }
    }
    @ModifyVariable(method="travelInAir", at=@At("HEAD"), argsOnly=true)
    private Vec3 cf$levitationControl(Vec3 input) { var e = cf$self(); return has(e, LEGS, "spire") && e.hasEffect(MobEffects.LEVITATION) ? input.multiply(1.5, 1, 1.5) : input; }
}
