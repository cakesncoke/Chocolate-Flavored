package com.akiotsukino.chocolateflavored.mob.sunken;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

/** Amphibious steering, daylight water seeking and nighttime surfacing guided by 26.1.2 Drowned. */
final class SunkenSwimming {
    static final class Control extends MoveControl {
        private final Sunken mob;
        Control(Sunken mob){super(mob);this.mob=mob;}
        @Override public void tick(){
            if(!mob.isInWater()||!mob.wantsToSwim()){
                if(!mob.onGround())mob.setDeltaMovement(mob.getDeltaMovement().add(0,-.008,0));
                super.tick();return;
            }
            if(mob.seekingSurface||(mob.getTarget()!=null&&mob.getTarget().getY()>mob.getY()))mob.setDeltaMovement(mob.getDeltaMovement().add(0,.002,0));
            if(operation!=Operation.MOVE_TO||mob.getNavigation().isDone()){mob.setSpeed(0);return;}
            var delta=new Vec3(wantedX-mob.getX(),wantedY-mob.getY(),wantedZ-mob.getZ());
            if(delta.lengthSqr()<1.0E-8){mob.setSpeed(0);return;}
            mob.setYRot(rotlerp(mob.getYRot(),(float)(Mth.atan2(delta.z,delta.x)*180/Math.PI)-90,90));mob.yBodyRot=mob.getYRot();
            float speed=Mth.lerp(.125F,mob.getSpeed(),(float)(speedModifier*mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            mob.setSpeed(speed);mob.setDeltaMovement(mob.getDeltaMovement().add(speed*delta.x*.005,speed*delta.normalize().y*.1,speed*delta.z*.005));
        }
    }
    static final class FindWater extends Goal {
        private final Sunken mob;private BlockPos destination;
        FindWater(Sunken mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){
            if(!mob.level().isBrightOutside()||mob.isInWater())return false;
            for(int i=0;i<10;i++){
                var p=mob.blockPosition().offset(mob.getRandom().nextInt(20)-10,2-mob.getRandom().nextInt(8),mob.getRandom().nextInt(20)-10);
                if(mob.level().getFluidState(p).is(FluidTags.WATER)){destination=p;return true;}
            }
            return false;
        }
        @Override public boolean canContinueToUse(){return !mob.getNavigation().isDone()&&!mob.isInWater();}
        @Override public void start(){mob.getNavigation().moveTo(destination.getX()+.5,destination.getY(),destination.getZ()+.5,1);}
    }
    static final class Beach extends MoveToBlockGoal {
        private final Sunken mob;
        Beach(Sunken mob){super(mob,1,8,2);this.mob=mob;}
        @Override public boolean canUse(){return !mob.level().isBrightOutside()&&mob.isInWater()&&mob.getY()>=mob.level().getSeaLevel()-3&&super.canUse();}
        @Override protected boolean isValidTarget(LevelReader level,BlockPos pos){return level.isEmptyBlock(pos.above())&&level.isEmptyBlock(pos.above(2))&&level.getBlockState(pos).entityCanStandOn(level,pos,mob);}
        @Override public void start(){mob.seekingSurface=false;super.start();}
    }
    static final class Surface extends Goal {
        private final Sunken mob;private boolean stuck;
        Surface(Sunken mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){return !mob.level().isBrightOutside()&&mob.isInWater()&&mob.getY()<mob.level().getSeaLevel()-2;}
        @Override public boolean canContinueToUse(){return canUse()&&!stuck;}
        @Override public void start(){mob.seekingSurface=true;stuck=false;}
        @Override public void stop(){mob.seekingSurface=false;}
        @Override public void tick(){
            var path=mob.getNavigation().getPath();
            if(path!=null&&!path.isDone()&&mob.distanceToSqr(Vec3.atCenterOf(path.getTarget()))>=4)return;
            var next=DefaultRandomPos.getPosTowards(mob,4,8,new Vec3(mob.getX(),mob.level().getSeaLevel()-1,mob.getZ()),Math.PI/2);
            if(next==null){stuck=true;return;}mob.getNavigation().moveTo(next.x,next.y,next.z,1);
        }
    }
    private SunkenSwimming(){}
}
