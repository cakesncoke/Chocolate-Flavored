package com.akiotsukino.chocolateflavored.mob.tuff;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

/** Standalone state machine guided by Friends & Foes; uses native 26.1.2 navigation/equipment. */
public class TuffGolem extends AbstractGolem {
    private static final EntityDataAccessor<Boolean> SLEEPING=SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GLUED=SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ANIMATION=SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> ANIMATION_START=SynchedEntityData.defineId(TuffGolem.class,EntityDataSerializers.LONG);
    private Vec3 home;
    private float homeYaw;
    private int phaseTicks=6000, startupTicks=200, transitionTicks, returnTicks;
    public TuffGolem(EntityType<? extends TuffGolem> type,Level level){super(type,level);setPersistenceRequired();setCanPickUpLoot(false);setDropChance(EquipmentSlot.MAINHAND,2);}
    public static AttributeSupplier.Builder createAttributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.225).add(Attributes.KNOCKBACK_RESISTANCE,1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SLEEPING,false);b.define(GLUED,false);b.define(COLOR,DyeColor.RED.getId());b.define(ANIMATION,"");b.define(ANIMATION_START,0L);}
    public void initializeConstruction(DyeColor color,float yaw){
        entityData.set(COLOR,color.getId());home=position();homeYaw=yaw;yBodyRot=yaw;yHeadRot=yaw;
    }
    public boolean sleeping(){return entityData.get(SLEEPING);}
    public boolean glued(){return entityData.get(GLUED);}
    public int color(){return entityData.get(COLOR);}
    public String animation(){return entityData.get(ANIMATION);}
    public float animationSeconds(float partial){return (level().getGameTime()-entityData.get(ANIMATION_START)+partial)/20F;}
    public Vec3 home(){return home==null?position():home;}
    public boolean holding(){return !getMainHandItem().isEmpty();}
    public boolean immobilized(){return sleeping()||glued()||transitionTicks>0||startupTicks>0;}
    private void animate(String animation,int ticks){entityData.set(ANIMATION,animation);entityData.set(ANIMATION_START,level().getGameTime());transitionTicks=ticks;getNavigation().stop();}
    public void setSleeping(boolean sleep){
        if(sleeping()==sleep)return;
        entityData.set(SLEEPING,sleep);phaseTicks=sleep?6000+random.nextInt(6001):6000+random.nextInt(2001);returnTicks=0;
        animate((sleep?"sleep":"wake")+(holding()?"WithItem":""),19);playSound(TuffGolemContent.sound(sleep?"sleep":"wake"));
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(4,new WaterAvoidingRandomStrollGoal(this,.6){@Override public boolean canUse(){return !immobilized()&&phaseTicks>0&&super.canUse();}@Override public boolean canContinueToUse(){return !immobilized()&&phaseTicks>0&&super.canContinueToUse();}});
        goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,6){@Override public boolean canUse(){return !immobilized()&&super.canUse();}});
    }
    @Override protected void customServerAiStep(ServerLevel level){
        super.customServerAiStep(level);
        if(home==null){home=position();homeYaw=getYRot();}
        if(transitionTicks>0)--transitionTicks;
        if(startupTicks>0){--startupTicks;return;}
        if(glued()||transitionTicks>0){getNavigation().stop();return;}
        if(sleeping()){
            getNavigation().stop();setYRot(homeYaw);yBodyRot=homeYaw;yHeadRot=homeYaw;
            if(--phaseTicks<=0)setSleeping(false);
            return;
        }
        if(--phaseTicks<=0){
            getNavigation().moveTo(home.x,home.y,home.z,.6);
            if(position().distanceToSqr(home)<.15){setPos(home);setSleeping(true);}
            else if(++returnTicks>=600){home=position();homeYaw=getYRot();setSleeping(true);}
        }else if(!holding()&&tickCount%10==0){
            level.getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(6),i->i.isAlive()&&!i.hasPickUpDelay()&&!i.getItem().isEmpty()).stream().min(Comparator.comparingDouble(this::distanceToSqr)).ifPresent(item->{getNavigation().moveTo(item,.6);collect(item);});
        }
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(holding()?.175:.225);
    }
    public boolean collect(ItemEntity item){
        if(level().isClientSide()||immobilized()||holding()||!item.isAlive()||item.hasPickUpDelay()||item.getItem().isEmpty()||distanceToSqr(item)>1.5)return false;
        take(item,1);setItemSlot(EquipmentSlot.MAINHAND,item.getItem().split(1));if(item.getItem().isEmpty())item.discard();animate("showItem",10);return true;
    }
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){
        ItemStack held=player.getItemInHand(hand);
        if(held.getItem() instanceof SpawnEggItem)return super.mobInteract(player,hand);
        if(level().isClientSide())return InteractionResult.SUCCESS;
        ServerLevel level=(ServerLevel)level();
        if(held.is(Items.TUFF)&&getHealth()<getMaxHealth()){heal(5);held.consume(1,player);playSound(TuffGolemContent.sound("repair"));return InteractionResult.SUCCESS;}
        var dye=held.get(DataComponents.DYE);
        if(dye!=null&&held.getItem() instanceof DyeItem&&color()!=dye.getId()){entityData.set(COLOR,dye.getId());held.consume(1,player);playSound(net.minecraft.sounds.SoundEvents.DYE_USE);return InteractionResult.SUCCESS;}
        if(held.is(Items.HONEYCOMB)&&!glued()){entityData.set(GLUED,true);held.consume(1,player);getNavigation().stop();playSound(TuffGolemContent.sound("glue_on"));level.sendParticles(ParticleTypes.WAX_ON,getX(),getY()+.5,getZ(),7,.3,.3,.3,0);return InteractionResult.SUCCESS;}
        if(held.is(ItemTags.AXES)&&glued()){entityData.set(GLUED,false);held.hurtAndBreak(1,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);playSound(TuffGolemContent.sound("glue_off"));level.sendParticles(ParticleTypes.WAX_OFF,getX(),getY()+.5,getZ(),7,.3,.3,.3,0);return InteractionResult.SUCCESS;}
        if(transitionTicks>0)return InteractionResult.SUCCESS;
        if(holding()){
            ItemStack item=getMainHandItem();setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            if(!player.addItem(item))spawnAtLocation(level,item);
            boolean wasSleeping=sleeping();entityData.set(SLEEPING,false);phaseTicks=6000+random.nextInt(2001);animate(wasSleeping?"wakeAndHideItem":"hideItem",wasSleeping?28:10);
        }else if(!held.isEmpty()){
            setItemSlot(EquipmentSlot.MAINHAND,held.copyWithCount(1));held.consume(1,player);
            boolean wasSleeping=sleeping();entityData.set(SLEEPING,false);phaseTicks=6000+random.nextInt(2001);animate(wasSleeping?"wakeAndShowItem":"showItem",wasSleeping?28:10);
        }else return InteractionResult.PASS;
        return InteractionResult.SUCCESS;
    }
    @Override public boolean isPushable(){return !immobilized()&&super.isPushable();}
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return TuffGolemContent.sound("hurt");}
    @Override protected SoundEvent getDeathSound(){return TuffGolemContent.sound("hurt");}
    @Override protected void playStepSound(BlockPos pos,BlockState state){playSound(TuffGolemContent.sound("step"),.25F,1);}
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putBoolean("Sleeping",sleeping());out.putBoolean("Glued",glued());out.putInt("Color",color());out.putInt("PhaseTicks",phaseTicks);out.putInt("StartupTicks",startupTicks);out.putInt("ReturnTicks",returnTicks);out.putDouble("HomeX",home().x);out.putDouble("HomeY",home().y);out.putDouble("HomeZ",home().z);out.putFloat("HomeYaw",homeYaw);}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);entityData.set(SLEEPING,in.getBooleanOr("Sleeping",false));entityData.set(GLUED,in.getBooleanOr("Glued",false));entityData.set(COLOR,Math.clamp(in.getIntOr("Color",14),0,15));phaseTicks=Math.clamp(in.getIntOr("PhaseTicks",6000),0,12000);startupTicks=Math.clamp(in.getIntOr("StartupTicks",0),0,200);returnTicks=Math.clamp(in.getIntOr("ReturnTicks",0),0,600);home=new Vec3(in.getDoubleOr("HomeX",getX()),in.getDoubleOr("HomeY",getY()),in.getDoubleOr("HomeZ",getZ()));homeYaw=in.getFloatOr("HomeYaw",getYRot());entityData.set(ANIMATION,"");transitionTicks=0;}
}
