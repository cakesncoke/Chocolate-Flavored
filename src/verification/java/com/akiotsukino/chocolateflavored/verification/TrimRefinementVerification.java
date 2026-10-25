package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import static net.minecraft.world.entity.EquipmentSlot.*;

final class TrimRefinementVerification {
    static void run(ServerLevel level, FakePlayer player) {
        try {
            player.getPersistentData().putBoolean(TrimTriggers.key("refined_cooldowns"),false);
            player.getPersistentData().putLong(TrimTriggers.key("eye"),TrimTriggers.clock(player)+11000);
            TrimTriggers.migrateCooldowns(player);
            check(player.getPersistentData().getLongOr(TrimTriggers.key("eye"),0)-TrimTriggers.clock(player)==1400,"Old cooldown retains elapsed time under new two-minute limit");
            fireResistance(level, player);
            collision(level, player);
            swimming(level, player);
            basalt(level, player);
            lightning(level);
            TrimVerification.set(level,player,"eye");
            player.setItemSlot(MAINHAND,new ItemStack(Items.DIAMOND_PICKAXE));
            player.setShiftKeyDown(true);
            player.getPersistentData().putBoolean(TrimTriggers.key("crouching"),false);
            player.getPersistentData().putLong(TrimTriggers.key("eye"),0);
            TrimTriggers.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            check(player.getPersistentData().getLongOr(TrimTriggers.key("eye"),0)-TrimTriggers.clock(player)==2400,"Eye two-minute trigger cooldown");
            player.setShiftKeyDown(false);TrimHighlights.clearAll(player);TrimVerification.clear(player);
            ChocolateFlavored.LOGGER.info("TRIM_REFINEMENT_VERIFICATION_PASSED: Nether Fire Resistance, oxygen attributes, movement-validation leaf collision, native airborne swimming and floor jumps, basalt source disk/aging/restoration, fireless lightning, Eye cooldown");
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
    private static void fireResistance(ServerLevel level,FakePlayer player) {
        var nether=level.getServer().getLevel(Level.NETHER);
        var p=new FakePlayer(nether,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"RibNether"));
        p.setPos(0,180,0);p.setItemSlot(CHEST,TrimVerification.armor(nether,Items.DIAMOND_CHESTPLATE,"rib","iron"));
        check(p.hasEffect(MobEffects.FIRE_RESISTANCE)&&p.getEffect(MobEffects.FIRE_RESISTANCE).isInfiniteDuration(),"Infinite Nether passive");
        check(!p.hurtServer(nether,p.damageSources().lava(),20)&&p.getHealth()==20,"Actual native lava damage resisted");
        p.setItemSlot(CHEST,ItemStack.EMPTY);check(!p.hasEffect(MobEffects.FIRE_RESISTANCE),"No residual resistance after unequip");
        TrimVerification.set(level,player,"rib");check(!player.hasEffect(MobEffects.FIRE_RESISTANCE),"Overworld excluded");
    }
    private static void collision(ServerLevel level,FakePlayer p) throws ReflectiveOperationException {
        TrimVerification.set(level,p,"wild");p.setPos(380.5,161,.5);p.setShiftKeyDown(false);
        BlockPos pos=p.blockPosition().east();var leaves=Blocks.OAK_LEAVES.defaultBlockState();level.setBlockAndUpdate(pos,leaves);
        check(CollisionContext.of(p).getCollisionShape(leaves,level,pos).isEmpty(),"Normal leaf movement");
        check(CollisionContext.withPosition(p,p.getY()).getCollisionShape(leaves,level,pos).isEmpty(),"Server validation agrees with movement");
        check(!CollisionContext.placementContext(p).getCollisionShape(leaves,level,pos).isEmpty(),"Actual block placement remains solid");
        var validate=net.minecraft.server.network.ServerGamePacketListenerImpl.class.getDeclaredMethod("isEntityCollidingWithAnythingNew",net.minecraft.world.level.LevelReader.class,Entity.class,net.minecraft.world.phys.AABB.class,double.class,double.class,double.class);validate.setAccessible(true);
        check(!(Boolean)validate.invoke(p.connection,level,p,p.getBoundingBox(),p.getX()+1,p.getY(),p.getZ()),"Native movement packet validation accepts leaf traversal");
        p.setPos(381.5,162,.5);check(!CollisionContext.withPosition(p,162).getCollisionShape(leaves,level,pos).isEmpty(),"Validation retains leaf top support");
        p.setShiftKeyDown(true);check(CollisionContext.withPosition(p,162).getCollisionShape(leaves,level,pos).isEmpty(),"Validation accepts crouch descent");
        p.setShiftKeyDown(false);level.removeBlock(pos,false);
    }
    private static void swimming(ServerLevel level,FakePlayer p) throws ReflectiveOperationException {
        TrimVerification.clear(p);p.setPos(390.5,165,.5);p.setOnGround(false);p.setSwimming(false);p.setSprinting(false);
        var water=Entity.class.getDeclaredField("wasTouchingWater");water.setAccessible(true);water.setBoolean(p,true);
        var travel=LivingEntity.class.getDeclaredMethod("travelInFluid",Vec3.class);travel.setAccessible(true);
        p.setDeltaMovement(Vec3.ZERO);travel.invoke(p,new Vec3(0,0,1));Vec3 vanilla=p.getDeltaMovement();
        p.setPos(390.5,165,.5);p.setOnGround(false);p.setDeltaMovement(Vec3.ZERO);
        p.setItemSlot(FEET,TrimVerification.armor(level,Items.LEATHER_BOOTS,"tide","iron"));
        travel.invoke(p,new Vec3(0,0,1));check(p.getDeltaMovement().distanceToSqr(vanilla)<1e-12,"Airborne non-swimming movement exactly vanilla");
        var jump=LivingEntity.class.getDeclaredMethod("jumpInLiquid",net.minecraft.tags.TagKey.class);jump.setAccessible(true);
        p.setDeltaMovement(Vec3.ZERO);p.setOnGround(false);jump.invoke(p,net.minecraft.tags.FluidTags.WATER);check(Math.abs(p.getDeltaMovement().y-.04)<1e-6,"Normal off-floor swim jump");
        p.setDeltaMovement(Vec3.ZERO);p.setOnGround(true);jump.invoke(p,net.minecraft.tags.FluidTags.WATER);check(p.getDeltaMovement().y>.4,"Floor jump launches upward");
        water.setBoolean(p,false);p.setOnGround(false);p.setDeltaMovement(Vec3.ZERO);
    }
    private static void basalt(ServerLevel level,FakePlayer p) {
        BlockPos center=new BlockPos(400,160,0);
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-4,0,-4),center.offset(4,1,4)))level.setBlock(pos,pos.getY()==160?Blocks.LAVA.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        level.setBlock(center.east(),Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,1),2);
        p.setPos(400.5,161,.5);p.setOnGround(false);TrimBasalt.walk(p);check(level.getBlockState(center).is(Blocks.LAVA),"No mid-air crust");
        p.setOnGround(true);TrimBasalt.walk(p);check(level.getBlockState(center).is(TrimBasalt.BLOCK.get()),"Source lava crust");
        check(level.getBlockState(center.east()).is(Blocks.LAVA),"Flowing lava excluded");check(level.getBlockState(center.east(3)).is(Blocks.LAVA),"Circular radius excludes edge");
        var block=TrimBasalt.BLOCK.get();
        for(int age=0;age<4 && level.getBlockState(center).is(block);age++){
            level.getRandom().setSeed(0);
            level.getBlockState(center).tick(level,center,level.getRandom());
        }
        // Explicit final aging works regardless of the seeded dense-neighbor branch.
        level.setBlock(center,block.defaultBlockState().setValue(TrimBasalt.AGE,3),2);
        for(int i=0;i<100&&level.getBlockState(center).is(block);i++)level.getBlockState(center).tick(level,center,level.getRandom());
        check(level.getBlockState(center).is(Blocks.LAVA),"Crust ages back to lava in darkness");
        TrimBasalt.walk(p);check(level.getBlockState(center).is(Blocks.LAVA),"Standing still cannot refresh crust");
        level.setBlock(center,block.defaultBlockState(),2);p.setItemSlot(MAINHAND,ItemStack.EMPTY);p.gameMode.destroyBlock(center);check(level.getBlockState(center).is(Blocks.LAVA),"Breaking weak basalt restores lava");
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-4,0,-4),center.offset(4,1,4)))level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        TrimBasalt.reset(p);p.setOnGround(false);
    }
    private static void lightning(ServerLevel level) throws ReflectiveOperationException {
        var rule=net.minecraft.world.level.gamerules.GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER;
        int previous=level.getGameRules().get(rule);
        level.getGameRules().set(rule,-1,level.getServer());
        try {
        BlockPos pos=new BlockPos(410,161,0);level.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());level.removeBlock(pos,false);
        var bolt=EntityType.LIGHTNING_BOLT.create(level,EntitySpawnReason.TRIGGERED);bolt.setPos(Vec3.atBottomCenterOf(pos));
        var fire=LightningBolt.class.getDeclaredMethod("spawnFire",int.class);fire.setAccessible(true);
        bolt.getPersistentData().putBoolean(TrimTriggers.key("fireless"),true);fire.invoke(bolt,4);check(level.getBlockState(pos).isAir(),"Trim bolt does not ignite");
        bolt.getPersistentData().putBoolean(TrimTriggers.key("fireless"),false);fire.invoke(bolt,0);check(level.getBlockState(pos).is(Blocks.FIRE),"Natural lightning ignition unchanged");
        level.removeBlock(pos,false);level.removeBlock(pos.below(),false);
        } finally { level.getGameRules().set(rule,previous,level.getServer()); }
    }
    private static void check(boolean condition,String description) { if(!condition)throw new IllegalStateException("Trim refinement: "+description); }
}
