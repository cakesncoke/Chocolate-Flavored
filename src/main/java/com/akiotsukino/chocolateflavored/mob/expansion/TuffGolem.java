package com.akiotsukino.chocolateflavored.mob.expansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import java.util.Comparator;

/** A persistent walking display: carries one item, wears carpet, and returns home to rest. */
public class TuffGolem extends AbstractGolem {
    private static final EntityDataAccessor<Boolean> STATUE = SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CLOAK = SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.INT);
    private BlockPos home;
    private int phaseTicks = 1200;
    private int returnTicks;
    public TuffGolem(EntityType<? extends TuffGolem> type, Level level) { super(type,level); setPersistenceRequired(); setCanPickUpLoot(false); setDropChance(EquipmentSlot.MAINHAND,2); }
    public static AttributeSupplier.Builder createAttributes() { return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,10).add(Attributes.MOVEMENT_SPEED,.2).add(Attributes.KNOCKBACK_RESISTANCE,1); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { super.defineSynchedData(data); data.define(STATUE,true); data.define(CLOAK,DyeColor.RED.getId()); }
    public boolean isStatue() { return entityData.get(STATUE); }
    public int cloakColor() { return entityData.get(CLOAK); }
    public BlockPos home() { return home == null ? blockPosition() : home; }
    public void setStatue(boolean statue) {
        entityData.set(STATUE,statue);
        phaseTicks = statue ? 1200 + random.nextInt(1201) : 400;
        returnTicks = 0;
        if (statue) { getNavigation().stop(); setDeltaMovement(0,0,0); }
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(4,new WaterAvoidingRandomStrollGoal(this,1) {
            @Override public boolean canUse() { return !isStatue() && phaseTicks > 0 && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isStatue() && phaseTicks > 0 && super.canContinueToUse(); }
        });
        goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,6) { @Override public boolean canUse() { return !isStatue() && super.canUse(); } });
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (home == null) home = blockPosition();
        if (--phaseTicks <= 0) {
            if (isStatue()) setStatue(false);
            else {
                getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);
                if (home.distToCenterSqr(position()) < 1 || ++returnTicks >= 600) {
                    if (returnTicks >= 600) home = blockPosition();
                    setStatue(true);
                }
            }
        }
        if (!isStatue() && phaseTicks > 0 && getMainHandItem().isEmpty() && tickCount % 10 == 0) {
            level.getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(6),i -> i.isAlive() && !i.hasPickUpDelay() && !i.getItem().isEmpty()).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).ifPresent(item -> {
                    getNavigation().moveTo(item,1.1);
                    collectItem(item);
                });
        }
    }
    public boolean collectItem(ItemEntity item) {
        if (level().isClientSide() || isStatue() || !getMainHandItem().isEmpty() || item.hasPickUpDelay() || !item.isAlive() || item.getItem().isEmpty() || distanceToSqr(item) >= 1.5) return false;
        take(item,1); setItemSlot(EquipmentSlot.MAINHAND,item.getItem().split(1));
        if (item.getItem().isEmpty()) item.discard();
        playSound(SoundEvents.ITEM_PICKUP,.5F,1);
        return true;
    }
    @Override public boolean isPushable() { return !isStatue() && super.isPushable(); }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        for (DyeColor color : DyeColor.values()) {
            if (held.is(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getName()+"_carpet")))) {
                if (!level().isClientSide() && cloakColor() != color.getId()) { entityData.set(CLOAK,color.getId()); held.consume(1,player); playSound(SoundEvents.WOOL_PLACE); }
                return InteractionResult.SUCCESS;
            }
        }
        if (!level().isClientSide()) {
            if (!getMainHandItem().isEmpty()) {
                ItemStack displayed = getMainHandItem(); setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                if (!player.addItem(displayed)) spawnAtLocation((ServerLevel)level(),displayed);
            } else if (!held.isEmpty()) {
                setItemSlot(EquipmentSlot.MAINHAND,held.copyWithCount(1)); held.consume(1,player);
            } else setStatue(!isStatue());
            playSound(SoundEvents.TUFF_PLACE);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out); out.putBoolean("Statue",isStatue()); out.putInt("CloakColor",cloakColor());
        out.putInt("PhaseTicks",phaseTicks); out.putInt("ReturnTicks",returnTicks); out.store("Home",BlockPos.CODEC,home());
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in); entityData.set(STATUE,in.getBooleanOr("Statue",true)); entityData.set(CLOAK,Math.clamp(in.getIntOr("CloakColor",DyeColor.RED.getId()),0,15));
        phaseTicks = Math.clamp(in.getIntOr("PhaseTicks",1200),0,2400); returnTicks = Math.clamp(in.getIntOr("ReturnTicks",0),0,600); home = in.read("Home",BlockPos.CODEC).orElse(blockPosition());
    }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damage) { return SoundEvents.TUFF_BREAK; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.TUFF_BREAK; }
}
