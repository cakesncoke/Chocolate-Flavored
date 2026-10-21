// SPDX-License-Identifier: MIT
// Dungeons Mobs four regenerating shields concept, with vanilla Blaze motion and fireballs.
package com.akiotsukino.chocolateflavored.mob.expansion;

import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

public class Wildfire extends Blaze {
    private static final EntityDataAccessor<Float> SHIELD_HEALTH = SynchedEntityData.defineId(Wildfire.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(Wildfire.class,EntityDataSerializers.INT);
    private int regeneration = 150;
    private int attackCooldown = 40;
    public Wildfire(EntityType<? extends Wildfire> type,Level level) { super(type,level); }
    public static AttributeSupplier.Builder createAttributes() { return Blaze.createAttributes().add(Attributes.MAX_HEALTH,50).add(Attributes.KNOCKBACK_RESISTANCE,.5); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { super.defineSynchedData(data); data.define(SHIELD_HEALTH,60F); data.define(CHARGE,0); }
    public int shields() { return (int)Math.ceil(entityData.get(SHIELD_HEALTH)/15F); }
    public int chargeTicks() { return entityData.get(CHARGE); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(5,new MoveTowardsRestrictionGoal(this,1));
        goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,.8));
        goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,24));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override protected void actuallyHurt(ServerLevel level,DamageSource source,float amount) {
        if (shields() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypes.DROWN)) {
            int before = shields();
            entityData.set(SHIELD_HEALTH,Math.max(0,entityData.get(SHIELD_HEALTH)-amount)); regeneration = 150;
            playSound(shields() < before ? SoundEvents.SHIELD_BREAK : SoundEvents.SHIELD_BLOCK,1,.7F);
        } else super.actuallyHurt(level,source,amount);
    }
    public void shootVolley(ServerLevel level,LivingEntity target) {
        Vec3 direction = target.getEyePosition().subtract(getX(),getEyeY(),getZ()).normalize();
        for (int spread = -1; spread <= 1; spread++) {
            Vec3 shot = direction.yRot(spread*.12F);
            SmallFireball fireball = new SmallFireball(level,this,shot);
            fireball.setPos(getX(),getEyeY()-.2,getZ()); level.addFreshEntity(fireball);
        }
        playSound(SoundEvents.BLAZE_SHOOT,1,.7F);
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (shields() < 4 && --regeneration <= 0) { entityData.set(SHIELD_HEALTH,Math.min(60,entityData.get(SHIELD_HEALTH)+15)); regeneration = 150; playSound(SoundEvents.AMETHYST_BLOCK_CHIME,.7F,.5F); }
        LivingEntity target = getTarget();
        if (attackCooldown > 0) --attackCooldown;
        if (target == null || !target.isAlive() || !hasLineOfSight(target)) { entityData.set(CHARGE,0); return; }
        getLookControl().setLookAt(target,90,90);
        if (distanceToSqr(target) > 576) { getNavigation().moveTo(target,1); return; }
        getNavigation().stop();
        if (chargeTicks() > 0) {
            entityData.set(CHARGE,chargeTicks()-1);
            if (chargeTicks() == 0) { shootVolley(level,target); attackCooldown = 80; }
        } else if (attackCooldown <= 0) {
            if (distanceToSqr(target) < 9) {
                if (target.hurtServer(level,damageSources().mobAttack(this),6)) target.knockback(1,getX()-target.getX(),getZ()-target.getZ());
                playSound(SoundEvents.BLAZE_SHOOT); attackCooldown = 60;
            } else entityData.set(CHARGE,30);
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putFloat("ShieldHealth",entityData.get(SHIELD_HEALTH)); out.putInt("ShieldRegeneration",regeneration); out.putInt("AttackCooldown",attackCooldown); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); entityData.set(SHIELD_HEALTH,Math.clamp(in.getFloatOr("ShieldHealth",60),0,60)); regeneration = Math.clamp(in.getIntOr("ShieldRegeneration",150),0,150); attackCooldown = Math.clamp(in.getIntOr("AttackCooldown",40),0,80); entityData.set(CHARGE,0); }
}
