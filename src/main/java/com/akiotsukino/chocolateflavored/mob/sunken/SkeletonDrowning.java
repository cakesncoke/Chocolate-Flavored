package com.akiotsukino.chocolateflavored.mob.sunken;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Matches Zombie's 600 submerged ticks plus a 300-tick committed conversion. */
public final class SkeletonDrowning {
    public static final String SUBMERGED="chocolateflavored:sunken_submerged",CONVERSION="chocolateflavored:sunken_conversion";
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof AbstractSkeleton skeleton)||skeleton instanceof Sunken||!(skeleton.level() instanceof ServerLevel level)||!skeleton.isAlive()||skeleton.isNoAi())return;
        var data=skeleton.getPersistentData();int remaining=data.getIntOr(CONVERSION,-1);
        if(remaining>=0){
            data.putInt(CONVERSION,--remaining);
            if(remaining<0){
                if(!EventHooks.canLivingConvert(skeleton,SunkenContent.SUNKEN.get(),ticks->data.putInt(CONVERSION,ticks))) {if(data.getIntOr(CONVERSION,-1)<0)data.putInt(CONVERSION,0);return;}
                var converted=skeleton.convertTo(SunkenContent.SUNKEN.get(),ConversionParams.single(skeleton,true,true),sunken->{sunken.randomizeColor();EventHooks.onLivingConvert(skeleton,sunken);});
                if(converted!=null&&!skeleton.isSilent())level.levelEvent(null,1040,converted.blockPosition(),0);
            }
        }else if(skeleton.isEyeInFluid(FluidTags.WATER)){
            int time=data.getIntOr(SUBMERGED,0)+1;data.putInt(SUBMERGED,time);if(time>=600)data.putInt(CONVERSION,300);
        }else data.putInt(SUBMERGED,-1);
    }
    private SkeletonDrowning(){}
}
