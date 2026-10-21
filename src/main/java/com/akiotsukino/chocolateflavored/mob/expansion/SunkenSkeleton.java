// SPDX-License-Identifier: MIT
// Dungeons Mobs aquatic/crossbow skeleton concept; rewritten against pinned 26.1.2 vanilla APIs.
package com.akiotsukino.chocolateflavored.mob.expansion;

import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class SunkenSkeleton extends Skeleton implements CrossbowAttackMob {
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(SunkenSkeleton.class, EntityDataSerializers.BOOLEAN);
    private final RangedCrossbowAttackGoal<SunkenSkeleton> crossbowGoal = new RangedCrossbowAttackGoal<>(this,1.0,15);
    private final WaterBoundPathNavigation waterNavigation;
    private final PathNavigation landNavigation;
    public SunkenSkeleton(EntityType<? extends SunkenSkeleton> type, Level level) {
        super(type,level);
        landNavigation = getNavigation();
        waterNavigation = new WaterBoundPathNavigation(this,level);
        moveControl = new SmoothSwimmingMoveControl(this,85,10,1,.5F,false);
        setPathfindingMalus(PathType.WATER,0);
        setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot,0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { super.defineSynchedData(data); data.define(CHARGING,false); }
    @Override protected void registerGoals() {
        super.registerGoals();
        var sunGoals = goalSelector.getAvailableGoals().stream().map(g -> g.getGoal()).filter(g -> g instanceof RestrictSunGoal || g instanceof FleeSunGoal || g instanceof WaterAvoidingRandomStrollGoal).toList();
        sunGoals.forEach(goalSelector::removeGoal);
        goalSelector.addGoal(5,new RandomStrollGoal(this,1));
    }
    @Override public void reassessWeaponGoal() {
        super.reassessWeaponGoal();
        if (crossbowGoal == null || level().isClientSide()) return;
        goalSelector.removeGoal(crossbowGoal);
        if (isHolding(Items.CROSSBOW)) {
            var conflicting = goalSelector.getAvailableGoals().stream().map(g -> g.getGoal()).filter(g -> g instanceof MeleeAttackGoal || g instanceof RangedBowAttackGoal<?>).toList();
            conflicting.forEach(goalSelector::removeGoal);
            goalSelector.addGoal(4,crossbowGoal);
        }
    }
    @Override protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) { setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW)); }
    @Override public void setChargingCrossbow(boolean charging) { entityData.set(CHARGING,charging); }
    public boolean isChargingCrossbow() { return entityData.get(CHARGING); }
    @Override public void onCrossbowAttackPerformed() { noActionTime = 0; }
    @Override public void performRangedAttack(LivingEntity target, float power) {
        if (isHolding(Items.CROSSBOW)) performCrossbowAttack(this,1.6F); else super.performRangedAttack(target,power);
    }
    @Override public boolean canUseNonMeleeWeapon(ItemStack weapon) { return weapon.is(Items.CROSSBOW) || super.canUseNonMeleeWeapon(weapon); }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public void startFreezeConversion(int time) {} // This aquatic species does not become a stray.
    @Override public boolean isPushedByFluid() { return !isSwimming(); }
    @Override public void updateSwimming() {
        if (!level().isClientSide() && waterNavigation != null) {
            boolean swimming = isInWater();
            navigation = swimming ? waterNavigation : landNavigation;
            setSwimming(swimming);
        }
    }
    @Override public void travel(Vec3 input) {
        if (isInWater()) {
            moveRelative(.02F,input); move(MoverType.SELF,getDeltaMovement()); setDeltaMovement(getDeltaMovement().scale(.9));
        } else super.travel(input);
    }
}
