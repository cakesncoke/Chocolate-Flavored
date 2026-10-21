package com.akiotsukino.chocolateflavored.mob.expansion;

import java.util.UUID;
import net.minecraft.core.particles.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;

/** An owned spell entity, never a placed block: warns, tracks briefly, then falls and shatters. */
public class IceChunk extends Entity {
    private static final EntityDataAccessor<Boolean> FALLING = SynchedEntityData.defineId(IceChunk.class,EntityDataSerializers.BOOLEAN);
    private UUID ownerId;
    private UUID targetId;
    private int age;
    public IceChunk(EntityType<? extends IceChunk> type,Level level) { super(type,level); setNoGravity(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) { data.define(FALLING,false); }
    @Override public boolean hurtServer(ServerLevel level,net.minecraft.world.damagesource.DamageSource source,float amount) { return false; }
    public boolean isFalling() { return entityData.get(FALLING); }
    public int spellAge() { return age; }
    public void prepare(LivingEntity owner,LivingEntity target) {
        ownerId = owner.getUUID(); targetId = target.getUUID();
        setPos(target.getX(),target.getY()+target.getBbHeight()+3,target.getZ());
    }
    @Override public void tick() {
        super.tick(); ++age;
        if (!(level() instanceof ServerLevel server)) return;
        if (age > 160) { discard(); return; }
        if (age < 40) {
            Entity target = targetId == null ? null : server.getEntity(targetId);
            if (target != null && target.isAlive()) setPos(getX()+(target.getX()-getX())*.15,getY(),getZ()+(target.getZ()-getZ())*.15);
            if (age%5 == 0) server.sendParticles(ParticleTypes.SNOWFLAKE,getX(),getY()-.5,getZ(),5,.6,.2,.6,.01);
            return;
        }
        entityData.set(FALLING,true);
        setDeltaMovement(0,Math.max(-1.2,getDeltaMovement().y-.08),0);
        Vec3 next = position().add(getDeltaMovement());
        BlockHitResult blockHit = server.clip(new ClipContext(position(),next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        Entity owner = ownerId == null ? null : server.getEntity(ownerId);
        boolean hit = !server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().expandTowards(getDeltaMovement()),e -> e.isAlive() && e != owner && !(owner instanceof LivingEntity living && living.isAlliedTo(e)) && !e.isSpectator()).isEmpty();
        setPos(blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : next);
        if (hit || blockHit.getType() == HitResult.Type.BLOCK || getY() < server.getMinY()) shatter(server);
    }
    public void shatter(ServerLevel level) {
        if (isRemoved()) return;
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.75),e -> e != owner && e.isAlive() && !e.isSpectator() && !(owner instanceof LivingEntity living && living.isAlliedTo(e)))) {
            affect(level,owner,victim);
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.PACKED_ICE.defaultBlockState()),getX(),getY(),getZ(),40,.7,.7,.7,.1);
        level.playSound(null,blockPosition(),SoundEvents.GLASS_BREAK,SoundSource.HOSTILE,1,.8F);
        discard();
    }
    protected boolean affect(ServerLevel level,Entity owner,LivingEntity victim) {
        if (victim == owner || !victim.isAlive() || victim.isSpectator() || owner instanceof LivingEntity living && living.isAlliedTo(victim)) return false;
        if (!victim.hurtServer(level,damageSources().indirectMagic(this,owner),8)) return false;
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100,1),owner);
        victim.setTicksFrozen(Math.max(victim.getTicksFrozen(),140));
        return true;
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("SpellAge",age);
        if (ownerId != null) out.store("Owner",net.minecraft.core.UUIDUtil.CODEC,ownerId);
        if (targetId != null) out.store("Target",net.minecraft.core.UUIDUtil.CODEC,targetId);
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        age = Math.clamp(in.getIntOr("SpellAge",0),0,161);
        ownerId = in.read("Owner",net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        targetId = in.read("Target",net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        entityData.set(FALLING,age >= 40);
    }
}
