package com.akiotsukino.chocolateflavored.mob.expansion;

import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** Mob A: an aquatic ambusher that tethers nearby swimmers/boats and pulls them underwater. */
public class Barnacle extends Monster {
    private static final EntityDataAccessor<Integer> PREY = SynchedEntityData.defineId(Barnacle.class,EntityDataSerializers.INT);
    private int biteTicks;
    public Barnacle(EntityType<? extends Barnacle> type,Level level) { super(type,level); moveControl = new SmoothSwimmingMoveControl(this,85,10,1,0,false); setPathfindingMalus(PathType.WATER,0); }
    public static AttributeSupplier.Builder createAttributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FOLLOW_RANGE,20); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { super.defineSynchedData(data); data.define(PREY,0); }
    public int preyId() { return entityData.get(PREY); }
    @Override protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this,level); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(4,new RandomSwimmingGoal(this,1,40));
        goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,12));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public void travel(Vec3 input) {
        if (isInWater()) { moveRelative(.02F,input); move(MoverType.SELF,getDeltaMovement()); setDeltaMovement(getDeltaMovement().scale(.9)); }
        else super.travel(input);
    }
    public boolean canTether(LivingEntity prey) { return isInWater() && prey != null && prey.isAlive() && !prey.isSpectator() && !(prey instanceof Player player && player.isCreative()) && !isAlliedTo(prey) && (prey.isInWater() || prey.getVehicle() instanceof AbstractBoat) && distanceToSqr(prey) <= 144 && hasLineOfSight(prey); }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity prey = getTarget();
        if (!canTether(prey)) { entityData.set(PREY,0); biteTicks = 0; return; }
        getLookControl().setLookAt(prey,90,90);
        if (distanceToSqr(prey) > 36) { entityData.set(PREY,0); biteTicks = 0; getNavigation().moveTo(prey,1); return; }
        getNavigation().stop(); entityData.set(PREY,prey.getId());
        Vec3 pull = position().add(0,-.5,0).subtract(prey.position()).normalize().scale(.045);
        Entity pulled = prey.getVehicle() instanceof AbstractBoat ? prey.getVehicle() : prey;
        pulled.setDeltaMovement(pulled.getDeltaMovement().add(pull.x,Math.min(-.025,pull.y),pull.z)); pulled.needsSync = true;
        if (tickCount%4 == 0) for (int i=1;i<8;i++) {
            Vec3 bubble = getEyePosition().lerp(prey.getEyePosition(),i/8D);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE,bubble.x,bubble.y,bubble.z,1,0,0,0,.01);
        }
        if (prey.isInWater()) prey.setAirSupply(Math.max(-20,prey.getAirSupply()-1));
        if (++biteTicks >= 40) { prey.hurtServer(level,damageSources().mobAttack(this),4); biteTicks = 0; playSound(SoundEvents.GUARDIAN_ATTACK,.6F,.6F); }
    }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.SQUID_AMBIENT; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damage) { return SoundEvents.SQUID_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.SQUID_DEATH; }
}
