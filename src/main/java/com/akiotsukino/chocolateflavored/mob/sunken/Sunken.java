package com.akiotsukino.chocolateflavored.mob.sunken;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.IShearable;
import java.util.List;

/** Skeleton combat and shearing state, with native crossbow/projectile APIs and amphibious movement. */
public class Sunken extends Bogged implements CrossbowAttackMob, IShearable {
    private static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(Sunken.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHARGING=SynchedEntityData.defineId(Sunken.class,EntityDataSerializers.BOOLEAN);
    public static final float TRIDENT_CHANCE=.1F;
    private final RangedCrossbowAttackGoal<Sunken> crossbowGoal=new RangedCrossbowAttackGoal<>(this,1,15);
    private final RangedBowAttackGoal<Sunken> bowGoal=new RangedBowAttackGoal<>(this,1,40,15);
    private final MeleeAttackGoal meleeGoal=new MeleeAttackGoal(this,1.2,false);
    private final RangedAttackGoal tridentGoal=new RangedAttackGoal(this,1,40,10){
        @Override public void start(){super.start();setAggressive(true);startUsingItem(InteractionHand.MAIN_HAND);}
        @Override public void stop(){super.stop();stopUsingItem();setAggressive(false);}
    };
    boolean seekingSurface;
    public Sunken(EntityType<? extends Sunken> type,Level level){
        super(type,level);moveControl=new SunkenSwimming.Control(this);setPathfindingMalus(PathType.WATER,0);reassessWeaponGoal();
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data){super.defineSynchedData(data);data.define(COLOR,0);data.define(CHARGING,false);}
    public int coralColor(){return entityData.get(COLOR);}
    public void setCoralColor(int color){entityData.set(COLOR,Math.clamp(color,0,2));}
    public void randomizeColor(){setCoralColor(random.nextInt(3));}
    public Item coralFan(){return switch(coralColor()){case 1->Items.FIRE_CORAL_FAN;case 2->Items.HORN_CORAL_FAN;default->Items.BUBBLE_CORAL_FAN;};}
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("CoralColor",coralColor());}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);setCoralColor(in.getIntOr("CoralColor",0));}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group){
        var result=super.finalizeSpawn(level,difficulty,reason,group);randomizeColor();return result;
    }
    @Override protected void populateDefaultEquipmentSlots(RandomSource random,DifficultyInstance difficulty){
        super.populateDefaultEquipmentSlots(random,difficulty);setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(random.nextFloat()<TRIDENT_CHANCE?Items.TRIDENT:Items.CROSSBOW));
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new SunkenSwimming.FindWater(this));
        goalSelector.addGoal(3,new FleeSunGoal(this,1));
        goalSelector.addGoal(3,new AvoidEntityGoal<>(this,Wolf.class,6,1,1.2));
        goalSelector.addGoal(5,new SunkenSwimming.Beach(this));
        goalSelector.addGoal(6,new SunkenSwimming.Surface(this));
        goalSelector.addGoal(7,new RandomStrollGoal(this,1));
        goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,IronGolem.class,true));
    }
    @Override public void reassessWeaponGoal(){
        // AbstractSkeleton calls this during its constructor, before our goals exist.
        if(crossbowGoal==null||level().isClientSide())return;
        goalSelector.removeGoal(crossbowGoal);goalSelector.removeGoal(bowGoal);goalSelector.removeGoal(tridentGoal);goalSelector.removeGoal(meleeGoal);
        setChargingCrossbow(false);
        if(isHolding(Items.CROSSBOW))goalSelector.addGoal(4,crossbowGoal);
        else if(getMainHandItem().is(Items.TRIDENT))goalSelector.addGoal(4,tridentGoal);
        else if(isHolding(Items.BOW)){bowGoal.setMinAttackInterval(level().getDifficulty()==Difficulty.HARD?20:40);goalSelector.addGoal(4,bowGoal);}
        else goalSelector.addGoal(4,meleeGoal);
    }
    @Override public boolean canUseNonMeleeWeapon(ItemStack stack){return stack.is(Items.CROSSBOW)||stack.is(Items.BOW)||stack.is(Items.TRIDENT);}
    @Override public void setChargingCrossbow(boolean charging){entityData.set(CHARGING,charging);}
    public boolean chargingCrossbow(){return entityData.get(CHARGING);}
    @Override public void onCrossbowAttackPerformed(){noActionTime=0;}
    @Override public void performRangedAttack(LivingEntity target,float power){
        if(isHolding(Items.CROSSBOW)){performCrossbowAttack(this,1.6F);return;}
        if(!getMainHandItem().is(Items.TRIDENT)){super.performRangedAttack(target,power);return;}
        if(level() instanceof ServerLevel server){
            var trident=new ThrownTrident(level(),this,getMainHandItem());
            double dx=target.getX()-getX(),dz=target.getZ()-getZ();
            Projectile.spawnProjectileUsingShoot(trident,server,getMainHandItem(),dx,target.getY(1.0/3)-trident.getY()+Math.sqrt(dx*dx+dz*dz)*.2,dz,1.6F,14-server.getDifficulty().getId()*4);
            playSound(SoundEvents.DROWNED_SHOOT,1,1/(random.nextFloat()*.4F+.8F));
        }
    }
    // Sunken have ordinary skeleton arrows, not Bogged poison arrows.
    @Override protected AbstractArrow getArrow(ItemStack projectile,float power,ItemStack weapon){return ProjectileUtil.getMobArrow(this,projectile,power,weapon);}
    @Override protected int getHardAttackInterval(){return 20;}
    @Override protected int getAttackInterval(){return 40;}
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.SKELETON_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.SKELETON_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.SKELETON_DEATH;}
    @Override protected SoundEvent getStepSound(){return SoundEvents.SKELETON_STEP;}
    @Override protected SoundEvent getSwimSound(){return SoundEvents.DROWNED_SWIM;}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){
        var tool=player.getItemInHand(hand);
        if(tool.is(Items.SHEARS)&&readyForShearing()){
            if(level() instanceof ServerLevel server){shear(server,SoundSource.PLAYERS,tool);gameEvent(GameEvent.SHEAR,player);tool.hurtAndBreak(1,player,hand.asEquipmentSlot());}
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player,hand);
    }
    private List<ItemStack> removeCoral(SoundSource source){
        if(!readyForShearing()||level().isClientSide())return List.of();
        setSheared(true);level().playSound(null,this,SoundEvents.BOGGED_SHEAR,source,1,1);return List.of(new ItemStack(coralFan()));
    }
    @Override public void shear(ServerLevel level,SoundSource source,ItemStack tool){for(var drop:removeCoral(source))spawnAtLocation(level,drop,getBbHeight());}
    @Override public boolean isShearable(Player player,ItemStack tool,Level level,BlockPos pos){return readyForShearing();}
    @Override public List<ItemStack> onSheared(Player player,ItemStack tool,Level level,BlockPos pos){return removeCoral(player==null?SoundSource.BLOCKS:SoundSource.PLAYERS);}
    @Override protected PathNavigation createNavigation(Level level){return new AmphibiousPathNavigation(this,level);}
    boolean wantsToSwim(){return seekingSurface||(getTarget()!=null&&getTarget().isInWater());}
    @Override public void updateSwimming(){if(!level().isClientSide())setSwimming(isEffectiveAi()&&isUnderWater()&&wantsToSwim());}
    @Override public boolean isVisuallySwimming(){return isSwimming()&&!isPassenger();}
    @Override public boolean isPushedByFluid(){return !isSwimming();}
    @Override protected void travelInWater(Vec3 input,double gravity,boolean falling,double oldY){
        if(isUnderWater()&&wantsToSwim()){moveRelative(.01F,input);move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.9));}
        else super.travelInWater(input,gravity,falling,oldY);
    }
    @Override public boolean checkSpawnObstruction(LevelReader level){return level.isUnobstructed(this);}
}
