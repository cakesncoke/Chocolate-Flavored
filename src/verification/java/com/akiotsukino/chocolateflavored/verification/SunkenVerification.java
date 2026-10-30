package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.sunken.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class SunkenVerification {
    private static final BlockPos POS=new BlockPos(48,180,0);
    private static final AABB AREA=new AABB(POS).inflate(8);
    public static void run(ServerLevel level,FakePlayer player){
        player.setGameMode(GameType.SURVIVAL);
        var sunken=spawn(level);var skeleton=EntityTypes.SKELETON.create(level,EntitySpawnReason.COMMAND);
        for(var attribute:java.util.List.of(Attributes.MOVEMENT_SPEED,Attributes.ATTACK_DAMAGE,Attributes.ARMOR,Attributes.FOLLOW_RANGE))check(sunken.getAttributeValue(attribute)==skeleton.getAttributeValue(attribute),"Skeleton base stats");
        check(sunken.getMaxHealth()==16&&!sunken.canFreeze()&&!sunken.isShaking(),"Murk health and freeze immunity");
        check(sunken.getNavigation() instanceof AmphibiousPathNavigation,"Amphibious navigation");
        check(sunken.is(EntityTypeTags.BURN_IN_DAYLIGHT)&&sunken.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER),"Daylight burning and underwater breathing tags");
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"time set 6000");
        sunken.setNoAi(true);sunken.setNoGravity(true);for(int i=0;i<1000;i++)sunken.aiStep();check(sunken.isOnFire(),"Actually burns in daylight");sunken.clearFire();sunken.discard();
        for(int color=0;color<3;color++){
            sunken=spawn(level);sunken.setCoralColor(color);level.addFreshEntity(sunken);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SHEARS));
            sunken.mobInteract(player,InteractionHand.MAIN_HAND);
            var drops=level.getEntitiesOfClass(ItemEntity.class,AREA);
            var live=switch(color){case 1->Items.FIRE_CORAL;case 2->Items.HORN_CORAL;default->Items.BUBBLE_CORAL;};
            var dead=switch(color){case 1->Items.DEAD_FIRE_CORAL;case 2->Items.DEAD_HORN_CORAL;default->Items.DEAD_BUBBLE_CORAL;};
            check(drops.stream().mapToInt(e->e.getItem().getCount()).sum()==2&&drops.stream().allMatch(e->e.getItem().is(live)||e.getItem().is(dead)),"Two matching live/dead coral rolls");
            check(sunken.isSheared()&&player.getMainHandItem().getDamageValue()==1,"Shearing state and durability");
            sunken.mobInteract(player,InteractionHand.MAIN_HAND);check(level.getEntitiesOfClass(ItemEntity.class,AREA).stream().mapToInt(e->e.getItem().getCount()).sum()==2,"No repeated shear drops");
            var restored=(Sunken)restore(level,sunken);check(restored.isSheared()&&restored.coralColor()==color&&!restored.readyForShearing(),"Color and sheared state survive reload");
            drops.forEach(Entity::discard);sunken.discard();restored.discard();
        }
        boolean[] colors=new boolean[3];
        for(int i=0;i<300;i++){
            sunken=spawn(level);sunken.getRandom().setSeed(918273L+i*9973L);sunken.finalizeSpawn(level,level.getCurrentDifficultyAt(POS),EntitySpawnReason.SPAWN_ITEM_USE,null);
            colors[sunken.coralColor()]=true;check(sunken.getMainHandItem().is(Items.BOW),"Murk spawn weapon is bow");sunken.discard();
        }
        check(colors[0]&&colors[1]&&colors[2],"All three spawn colors");
        verifyDrowning(level);
        verifyTargeting(level,player);
        verifyCombat(level);
        ChocolateFlavored.LOGGER.info("SUNKEN_VERIFICATION_PASSED: Murk health and freeze immunity, daylight burning, three colors, one-time coral shearing and persistence, five skeleton conversions and gear preservation, underwater steering, day/night targeting and bow combat");
    }
    private static void verifyDrowning(ServerLevel level){
        for(var p:BlockPos.betweenClosed(POS.offset(-2,-1,-2),POS.offset(2,4,2)))level.setBlock(p,Blocks.WATER.defaultBlockState(),3);
        for(var type:java.util.List.of(EntityTypes.SKELETON,EntityTypes.STRAY,EntityTypes.BOGGED,EntityTypes.WITHER_SKELETON,EntityTypes.PARCHED)){
            var original=(AbstractSkeleton)type.create(level,EntitySpawnReason.COMMAND);original.snapTo(POS.getX()+.5,POS.getY(),POS.getZ()+.5,0,0);original.setNoGravity(true);
            original.setCustomName(Component.literal("Drowning fixture"));original.setPersistenceRequired();original.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));original.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
            original.tick();original.tick();level.addFreshEntity(original);
            for(int i=0;i<599;i++)SkeletonDrowning.tick(new EntityTickEvent.Post(original));
            check(original.getPersistentData().getIntOr(SkeletonDrowning.CONVERSION,-1)==-1,"No early drowning conversion");
            SkeletonDrowning.tick(new EntityTickEvent.Post(original));check(original.getPersistentData().getIntOr(SkeletonDrowning.CONVERSION,-1)==300,"Conversion starts after 600 submerged ticks");
            var reloaded=(AbstractSkeleton)restore(level,original);check(reloaded.getPersistentData().getIntOr(SkeletonDrowning.CONVERSION,-1)==300,"Conversion timer survives reload");reloaded.discard();
            for(int i=0;i<301;i++)SkeletonDrowning.tick(new EntityTickEvent.Post(original));
            var converted=level.getEntitiesOfClass(Sunken.class,AREA);check(original.isRemoved()&&converted.size()==1,"Variant becomes one Sunken: "+type);
            var result=converted.getFirst();check(result.getMainHandItem().is(Items.BOW)&&result.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET)&&result.isPersistenceRequired()&&result.getCustomName().getString().equals("Drowning fixture"),"Conversion preserves gear, name and persistence");
            for(int i=0;i<1000;i++)SkeletonDrowning.tick(new EntityTickEvent.Post(result));check(!result.isRemoved(),"Sunken never reconverts");result.discard();
        }
        var swimmer=spawn(level);swimmer.tick();swimmer.tick();var target=EntityTypes.IRON_GOLEM.create(level,EntitySpawnReason.COMMAND);target.snapTo(POS.getX()+1,POS.getY()+1,POS.getZ(),0,0);target.tick();target.tick();swimmer.setTarget(target);swimmer.updateSwimming();check(swimmer.isSwimming(),"Swims toward submerged target");
        swimmer.setDeltaMovement(Vec3.ZERO);swimmer.getNavigation().moveTo(POS.getX()+1,POS.getY()+1,POS.getZ(),1);swimmer.getMoveControl().setWantedPosition(POS.getX()+1,POS.getY()+1,POS.getZ(),1);swimmer.getMoveControl().tick();check(swimmer.getDeltaMovement().y>0,"Swimming controller ascends toward target");swimmer.discard();target.discard();
        for(var p:BlockPos.betweenClosed(POS.offset(-2,-1,-2),POS.offset(2,4,2)))level.removeBlock(p,false);
        var dry=EntityTypes.SKELETON.create(level,EntitySpawnReason.COMMAND);dry.snapTo(POS.getX(),POS.getY(),POS.getZ(),0,0);dry.getPersistentData().putInt(SkeletonDrowning.SUBMERGED,599);SkeletonDrowning.tick(new EntityTickEvent.Post(dry));check(dry.getPersistentData().getIntOr(SkeletonDrowning.SUBMERGED,0)==-1,"Leaving water resets exposure before conversion");dry.discard();
    }
    private static void verifyTargeting(ServerLevel level,FakePlayer player){
        var sunken=spawn(level);
        // FakePlayer.tick() is intentionally empty; baseTick updates actual fluid contact.
        player.snapTo(POS.getX()+4,POS.getY(),POS.getZ(),0,0);player.baseTick();player.baseTick();
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"time set 6000");
        check(!sunken.canAttackTarget(player),"Does not acquire dry players in daylight");
        for(var pos:BlockPos.betweenClosed(player.blockPosition().below(),player.blockPosition().above(2)))level.setBlock(pos,Blocks.WATER.defaultBlockState(),3);
        player.baseTick();player.baseTick();check(sunken.canAttackTarget(player),"Acquires swimming players in daylight");
        for(var pos:BlockPos.betweenClosed(player.blockPosition().below(),player.blockPosition().above(2)))level.removeBlock(pos,false);
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"time set 18000");
        check(sunken.canAttackTarget(player)&&!sunken.canAttackTarget(null),"Night targeting and absent target");
        sunken.discard();
    }
    private static void verifyCombat(ServerLevel level){
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"time set 18000");
        for(var pos:BlockPos.betweenClosed(POS.offset(-6,-1,-6),POS.offset(6,-1,6)))level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);
        var sunken=spawn(level);level.addFreshEntity(sunken);var target=EntityTypes.IRON_GOLEM.create(level,EntitySpawnReason.COMMAND);target.snapTo(POS.getX()+4,POS.getY(),POS.getZ(),0,0);level.addFreshEntity(target);sunken.setTarget(target);
        sunken.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
        boolean drewBow=false;
        for(int i=0;i<160;i++){sunken.tick();drewBow|=sunken.isUsingItem();}
        check(drewBow&&!level.getEntitiesOfClass(Arrow.class,AREA).isEmpty(),"Skeleton AI draws and fires the bow");
        level.getEntitiesOfClass(AbstractArrow.class,AREA).forEach(Entity::discard);sunken.discard();target.discard();
    }
    private static Sunken spawn(ServerLevel level){var result=SunkenContent.SUNKEN.get().create(level,EntitySpawnReason.COMMAND);result.snapTo(POS.getX()+.5,POS.getY(),POS.getZ()+.5,0,0);return result;}
    private static Entity restore(ServerLevel level,Entity entity){var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());check(entity.saveAsPassenger(out),"Entity saved");return EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.LOAD, false)).orElseThrow();}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Sunken verification: "+message);}
    private SunkenVerification(){}
}
