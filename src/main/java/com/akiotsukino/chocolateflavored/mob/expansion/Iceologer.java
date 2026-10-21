// SPDX-License-Identifier: MIT
// Dungeons Mobs ice-cloud caster concept, implemented with current vanilla goals and block visuals.
package com.akiotsukino.chocolateflavored.mob.expansion;

import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;

public class Iceologer extends AbstractIllager {
    private static final EntityDataAccessor<Integer> CAST = SynchedEntityData.defineId(Iceologer.class,EntityDataSerializers.INT);
    private int cooldown = 40;
    public Iceologer(EntityType<? extends Iceologer> type,Level level) { super(type,level); setCanJoinRaid(false); }
    public static AttributeSupplier.Builder createAttributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FOLLOW_RANGE,24); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { super.defineSynchedData(data); data.define(CAST,0); }
    public int castingTicks() { return entityData.get(CAST); }
    @Override public boolean canJoinRaid() { return false; }
    @Override public boolean canBeLeader() { return false; }
    @Override public void applyRaidBuffs(ServerLevel level,int wave,boolean captain) {}
    @Override public SoundEvent getCelebrateSound() { return SoundEvents.EVOKER_CELEBRATE; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.EVOKER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return SoundEvents.EVOKER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.EVOKER_DEATH; }
    @Override public IllagerArmPose getArmPose() { return castingTicks() > 0 ? IllagerArmPose.SPELLCASTING : IllagerArmPose.CROSSED; }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new AvoidEntityGoal<>(this,Player.class,3,1.2,1.2));
        goalSelector.addGoal(2,new WaterAvoidingRandomStrollGoal(this,.7));
        goalSelector.addGoal(3,new LookAtPlayerGoal(this,Player.class,16));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,AbstractVillager.class,true));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,IronGolem.class,true));
    }
    public IceChunk summonIce(ServerLevel level,LivingEntity target) {
        IceChunk ice = ExpansionContent.ICE_CHUNK.get().create(level,EntitySpawnReason.TRIGGERED);
        if (ice != null) { ice.prepare(this,target); level.addFreshEntity(ice); playSound(SoundEvents.EVOKER_CAST_SPELL); }
        return ice;
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (castingTicks() > 0) {
            getNavigation().stop(); entityData.set(CAST,castingTicks()-1);
            if (target != null) getLookControl().setLookAt(target,90,90);
            if (castingTicks() == 10 && target != null && target.isAlive() && hasLineOfSight(target) && distanceToSqr(target) <= 576) summonIce(level,target);
            return;
        }
        if (cooldown > 0) --cooldown;
        if (target == null || !target.isAlive()) return;
        if (cooldown == 0 && hasLineOfSight(target) && distanceToSqr(target) <= 576) { cooldown = 120; entityData.set(CAST,40); playSound(SoundEvents.EVOKER_PREPARE_ATTACK); }
        else if (distanceToSqr(target) > 100 || !hasLineOfSight(target)) getNavigation().moveTo(target,1);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putInt("IceCooldown",cooldown); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); cooldown = Math.clamp(in.getIntOr("IceCooldown",40),0,120); entityData.set(CAST,0); }
}
