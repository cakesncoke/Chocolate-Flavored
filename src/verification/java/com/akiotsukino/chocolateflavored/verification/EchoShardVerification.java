package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.echo.*;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

final class EchoShardVerification {
    private static final class UsePlayer extends FakePlayer {
        UsePlayer(ServerLevel level){super(level,new GameProfile(UUID.randomUUID(),"EchoCheck"));}
        void advance(){if(isUsingItem()){useItem=getItemInHand(getUsedItemHand());super.updateUsingItem(useItem);}}
    }
    static void run(ServerLevel level){
        var player=new UsePlayer(level);player.setGameMode(GameType.SURVIVAL);
        var pos=new BlockPos(512,160,0);level.getChunkAt(pos);level.getChunkAt(pos.offset(32,0,0));
        level.setBlockAndUpdate(pos,Blocks.LODESTONE.defaultBlockState());
        player.setPos(512.5,162,.5);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.ECHO_SHARD,3));
        check(EchoShards.target(player.getMainHandItem())==null&&!player.getMainHandItem().hasFoil(),"Plain shards are unlinked");
        check(!Items.ECHO_SHARD.use(level,player,InteractionHand.MAIN_HAND).consumesAction(),"Plain shards cannot charge");
        check(link(player,pos).consumesAction(),"Native useOn binds shard");
        check(player.getMainHandItem().getCount()==2,"Linking splits exactly one shard from held stack");
        ItemStack shard=ItemStack.EMPTY;
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(EchoShards.target(player.getInventory().getItem(i))!=null){shard=player.getInventory().removeItem(i,64);break;}
        check(!shard.isEmpty(),"Linked split shard reaches inventory");
        UUID id=EchoShards.target(shard);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.ECHO_SHARD));link(player,pos);
        var second=player.getMainHandItem().copy();check(id.equals(EchoShards.target(second)),"Multiple shards share same anchor");
        var ops=level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        shard=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,shard).getOrThrow()).getOrThrow();
        check(id.equals(EchoShards.target(shard)),"Shard link survives serialization");
        var state=EchoAnchors.get(level.getServer());
        check(pos.equals(EchoAnchors.CODEC.parse(JsonOps.INSTANCE,EchoAnchors.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow()).getOrThrow().find(id)),"Anchor incarnation survives world-save codec");
        EchoLinkPayload.apply(new EchoLinkPayload(true,state.ids(),List.of()));
        check(shard.hasFoil()&&second.hasFoil(),"Client cache supplies glint to every linked copy");
        player.setItemInHand(InteractionHand.MAIN_HAND,shard.copyWithCount(2));player.setPos(544.5,161,.5);
        var origin=player.position();check(Items.ECHO_SHARD.use(level,player,InteractionHand.MAIN_HAND).consumesAction(),"Linked shard starts native hold use");
        check(player.getUseItem().getUseDuration(player)==60,"Three-second use duration");
        // Equivalent inventory stack replacement must not break native held-item progression.
        player.setItemInHand(InteractionHand.MAIN_HAND,player.getMainHandItem().copy());
        for(int i=0;i<59;i++)player.advance();
        check(player.position().equals(origin)&&player.getMainHandItem().getCount()==2,"No teleport or consumption at 59 ticks");
        player.advance();
        check(player.position().distanceToSqr(pos.above().getBottomCenter())<.01&&player.getMainHandItem().getCount()==1&&!player.isUsingItem(),"At 60 ticks teleport to lodestone and consume one shard");
        player.setPos(origin);check(Items.ECHO_SHARD.use(level,player,InteractionHand.MAIN_HAND).consumesAction(),"Remaining linked shard can charge");
        for(int i=0;i<20;i++)player.advance();player.releaseUsingItem();
        check(player.getMainHandItem().getCount()==1&&player.position().equals(origin),"Early release cancels without spending shard");
        level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        check(!EchoShards.linked(shard,level)&&!EchoShards.linked(second,level)&&state.find(id)==null,"Actual block removal invalidates all copies");
        EchoLinkPayload.apply(new EchoLinkPayload(false,List.of(),List.of(id)));
        check(!shard.hasFoil()&&!second.hasFoil(),"Stored-copy glint invalidated without inventory mutation");
        Items.ECHO_SHARD.inventoryTick(second,level,player,null);check(EchoShards.target(second)==null,"Stale item metadata clears when ticked");
        level.setBlockAndUpdate(pos,Blocks.LODESTONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.ECHO_SHARD));link(player,pos);
        var fresh=player.getMainHandItem().copy();UUID replacement=EchoShards.target(fresh);
        check(!replacement.equals(id)&&!EchoShards.linked(shard,level),"Replaced lodestone does not revive broken links");
        for(var dimension:List.of(Level.NETHER,Level.END)){
            var other=level.getServer().getLevel(dimension);check(other!=null,"Test dimension exists");
            var traveler=new UsePlayer(other);traveler.setGameMode(GameType.CREATIVE);other.getChunkAt(pos);
            other.setBlockAndUpdate(pos,Blocks.LODESTONE.defaultBlockState());traveler.setPos(512.5,162,.5);
            traveler.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.ECHO_SHARD));
            check(!link(traveler,pos).consumesAction()&&EchoShards.target(traveler.getMainHandItem())==null,"Cannot bind outside Overworld");
            traveler.setItemInHand(InteractionHand.OFF_HAND,fresh.copyWithCount(2));var before=traveler.position();
            check(Items.ECHO_SHARD.use(other,traveler,InteractionHand.OFF_HAND).consumesAction(),"Offworld linked shard starts charge");
            for(int i=0;i<59;i++)traveler.advance();check(traveler.getOffhandItem().getCount()==2,"Offworld use also requires full charge");traveler.advance();
            check(traveler.level()==other&&traveler.position().equals(before)&&traveler.getOffhandItem().getCount()==1,"Offworld completion breaks one shard without teleport, including creative and offhand");
            other.removeBlock(pos,false);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,fresh.copy());Items.ECHO_SHARD.use(level,player,InteractionHand.MAIN_HAND);
        for(int i=0;i<20;i++)player.advance();level.removeBlock(pos,false);
        Items.ECHO_SHARD.inventoryTick(player.getMainHandItem(),level,player,null);
        check(!player.isUsingItem()&&player.getMainHandItem().getCount()==1,"Broken anchor cancels pending charge without spending unlinked shard");
        level.setBlockAndUpdate(pos,Blocks.LODESTONE.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.ECHO_SHARD));link(player,pos);
        level.setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(pos.above(2),Blocks.STONE.defaultBlockState());player.setPos(origin);
        Items.ECHO_SHARD.use(level,player,InteractionHand.MAIN_HAND);for(int i=0;i<60;i++)player.advance();
        check(player.getMainHandItem().isEmpty()&&player.position().equals(origin),"Blocked arrival spends shard without suffocating player");
        level.removeBlock(pos,false);level.removeBlock(pos.above(),false);level.removeBlock(pos.above(2),false);
        EchoLinkPayload.clearClient();player.stopUsingItem();
        ChocolateFlavored.LOGGER.info("ECHO_SHARD_VERIFICATION_PASSED: native binding/splitting, shared links, item/world persistence, glint cache, 60-tick use/cancel, consumption, cross-dimension failure, incarnation invalidation, mid-charge removal, obstructed arrival");
    }
    private static InteractionResult link(UsePlayer player,BlockPos pos){return Items.ECHO_SHARD.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter(),Direction.UP,pos,false)));}
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Echo shard: "+message);}
}
