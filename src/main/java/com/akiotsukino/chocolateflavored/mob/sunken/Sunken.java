package com.akiotsukino.chocolateflavored.mob.sunken;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.turtle.Turtle;
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

/** Murk-inspired amphibious skeleton behavior, implemented against the 26.1.2 vanilla APIs. */
public class Sunken extends Bogged implements IShearable {
    private static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(Sunken.class,EntityDataSerializers.INT);
    boolean seekingSurface;
    public Sunken(EntityType<? extends Sunken> type,Level level){
        super(type,level);moveControl=new SunkenSwimming.Control(this);setPathfindingMalus(PathType.WATER,0);reassessWeaponGoal();
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data){super.defineSynchedData(data);data.define(COLOR,0);}
    public int coralColor(){return entityData.get(COLOR);}
    public void setCoralColor(int color){entityData.set(COLOR,Math.clamp(color,0,2));}
    public void randomizeColor(){setCoralColor(random.nextInt(3));}
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("CoralColor",coralColor());}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);setCoralColor(in.getIntOr("CoralColor",0));}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group){
        var result=super.finalizeSpawn(level,difficulty,reason,group);randomizeColor();return result;
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new SunkenSwimming.FindWater(this));
        goalSelector.addGoal(2,new RestrictSunGoal(this));
        goalSelector.addGoal(3,new FleeSunGoal(this,1));
        goalSelector.addGoal(3,new AvoidEntityGoal<>(this,Wolf.class,6,1,1.2));
        goalSelector.addGoal(5,new SunkenSwimming.Beach(this));
        goalSelector.addGoal(6,new SunkenSwimming.Surface(this));
        goalSelector.addGoal(7,new RandomStrollGoal(this,1));
        goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,10,true,false,(target,server)->canAttackTarget(target)));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,IronGolem.class,true));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,Axolotl.class,true));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,Turtle.class,10,true,false,Turtle.BABY_ON_LAND_SELECTOR));
    }
    /** Murk only seeks land players after dark; retaliation and other target goals remain independent. */
    public boolean canAttackTarget(LivingEntity target){return target!=null&&(!level().isBrightOutside()||target.isInWater());}
    @Override public boolean canFreeze(){return false;}
    @Override public boolean isShaking(){return false;}
    @Override public void performRangedAttack(LivingEntity target,float power){
        var weapon=getItemInHand(ProjectileUtil.getWeaponHoldingHand(this,Items.BOW));
        if(!weapon.is(Items.BOW)||!(level() instanceof ServerLevel server))return;
        var ammo=getProjectile(weapon);if(ammo.isEmpty())return;
        var arrow=getArrow(ammo,power,weapon);
        double dx=target.getX()-getX(),dz=target.getZ()-getZ();
        Projectile.spawnProjectileUsingShoot(arrow,server,ammo,dx,target.getY(1.0/3)-arrow.getY()+Math.sqrt(dx*dx+dz*dz)*.2,dz,1.6F,14-server.getDifficulty().getId()*4);
        playSound(SunkenSounds.ATTACK.get(),1,1/(random.nextFloat()*.4F+.8F));
    }
    // Sunken have ordinary skeleton arrows, not Bogged poison arrows.
    @Override protected AbstractArrow getArrow(ItemStack projectile,float power,ItemStack weapon){return ProjectileUtil.getMobArrow(this,projectile,power,weapon);}
    @Override protected int getHardAttackInterval(){return 20;}
    @Override protected int getAttackInterval(){return 40;}
    @Override protected SoundEvent getAmbientSound(){return (isInWater()?SunkenSounds.AMBIENT_WATER:SunkenSounds.AMBIENT).get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return (isInWater()?SunkenSounds.HURT_WATER:SunkenSounds.HURT).get();}
    @Override protected SoundEvent getDeathSound(){return (isInWater()?SunkenSounds.DEATH_WATER:SunkenSounds.DEATH).get();}
    @Override protected SoundEvent getStepSound(){return SoundEvents.SKELETON_STEP;}
    @Override public void playAmbientSound(){playSound(getAmbientSound(),isInWater()?.25F:getSoundVolume(),getVoicePitch());}
    @Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(isInWater()?SunkenSounds.STEP.get():SoundEvents.SKELETON_STEP,.15F,1);}
    @Override protected SoundEvent getSwimSound(){return SoundEvents.DROWNED_SWIM;}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){
        var tool=player.getItemInHand(hand);
        if(tool.is(Items.SHEARS)&&readyForShearing()){
            if(level() instanceof ServerLevel server){shear(server,SoundSource.PLAYERS,tool);gameEvent(GameEvent.SHEAR,player);tool.hurtAndBreak(1,player,hand.asEquipmentSlot());}
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player,hand);
    }
    private List<ItemStack> removeCoral(SoundSource source,ItemStack tool){
        if(!readyForShearing()||level().isClientSide())return List.of();
        setSheared(true);level().playSound(null,this,SunkenSounds.SHEAR.get(),source,1,1);
        var drops=new java.util.ArrayList<ItemStack>();
        String color=switch(coralColor()){case 1->"red";case 2->"yellow";default->"purple";};
        var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.Identifier.fromNamespaceAndPath("chocolateflavored","shearing/sunken_"+color));
        dropFromShearingLootTable((ServerLevel)level(),key,tool,(server,stack)->drops.add(stack));
        return drops;
    }
    @Override public void shear(ServerLevel level,SoundSource source,ItemStack tool){for(var drop:removeCoral(source,tool))spawnAtLocation(level,drop,getBbHeight());}
    @Override public boolean isShearable(Player player,ItemStack tool,Level level,BlockPos pos){return readyForShearing();}
    @Override public List<ItemStack> onSheared(Player player,ItemStack tool,Level level,BlockPos pos){return removeCoral(player==null?SoundSource.BLOCKS:SoundSource.PLAYERS,tool);}
    @Override protected PathNavigation createNavigation(Level level){return new AmphibiousPathNavigation(this,level);}
    boolean wantsToSwim(){return seekingSurface||(getTarget()!=null&&getTarget().isInWater());}
    @Override public void updateSwimming(){if(!level().isClientSide())setSwimming(isEffectiveAi()&&isInWater()&&wantsToSwim());}
    @Override public boolean isVisuallySwimming(){return isSwimming()&&!isPassenger();}
    @Override public boolean shouldDiscardFriction(){return isSwimming();}
    @Override public boolean isPushedByFluid(){return !isSwimming();}
    @Override protected void travelInWater(Vec3 input,double gravity,boolean falling,double oldY){
        if(isEffectiveAi()&&isInWater()&&wantsToSwim()){moveRelative(.01F,input);move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.9));}
        else super.travelInWater(input,gravity,falling,oldY);
    }
    @Override public boolean checkSpawnObstruction(LevelReader level){return level.isUnobstructed(this);}
}
