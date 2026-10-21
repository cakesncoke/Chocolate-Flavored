package com.akiotsukino.chocolateflavored.mob.tuff;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;

/** Upright, head-last construction; shares the vanilla pumpkin placement/dispenser hooks. */
public final class TuffGolemConstruction {
    private static DyeColor woolColor(LevelReader level,BlockPos head) {
        if(!level.getBlockState(head.below(2)).is(Blocks.TUFF))return null;
        var wool=level.getBlockState(head.below());
        for(var color:DyeColor.values())if(wool.is(BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(color.getName()+"_wool"))))return color;
        return null;
    }
    public static boolean canBuild(LevelReader level,BlockPos head){return woolColor(level,head)!=null;}
    public static boolean tryBuild(ServerLevel level,BlockPos head){
        var color=woolColor(level,head);
        var state=level.getBlockState(head);
        if(color==null||(!state.is(Blocks.CARVED_PUMPKIN)&&!state.is(Blocks.JACK_O_LANTERN)))return false;
        var golem=TuffGolemContent.TUFF_GOLEM.get().create(level,EntitySpawnReason.TRIGGERED);
        if(golem==null)return false;
        var base=head.below(2);
        float yaw=state.getValue(CarvedPumpkinBlock.FACING).toYRot();
        golem.snapTo(base.getX()+.5,base.getY()+.05,base.getZ()+.5,yaw,0);
        golem.initializeConstruction(color,yaw);
        if(!level.addFreshEntity(golem))return false;
        for(int i=0;i<3;i++){
            var pos=head.below(i);var old=level.getBlockState(pos);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
            level.levelEvent(2001,pos,Block.getId(old));
        }
        for(var player:level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class,golem.getBoundingBox().inflate(5)))CriteriaTriggers.SUMMONED_ENTITY.trigger(player,golem);
        for(int i=0;i<3;i++)level.updateNeighborsAt(head.below(i),Blocks.AIR);
        return true;
    }
    private TuffGolemConstruction(){}
}
