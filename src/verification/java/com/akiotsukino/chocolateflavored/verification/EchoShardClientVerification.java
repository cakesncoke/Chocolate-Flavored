package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.echo.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="chocolateflavored",value=Dist.CLIENT)
public final class EchoShardClientVerification {
    private static int ticks,phase,wait;
    private static ItemStack stale;
    private static BlockPos anchor;
    private static UUID oldId;
    private static volatile boolean ready;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||!HornClientVerification.completed||phase==4||++ticks<100)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        var server=mc.getSingleplayerServer();var playerId=mc.player.getUUID();
        if(phase==0){
            phase=1;server.execute(()->{
                var player=server.getPlayerList().getPlayer(playerId);var level=player.level();anchor=player.blockPosition().offset(8,0,0);
                level.getChunkAt(anchor);level.setBlockAndUpdate(anchor,Blocks.LODESTONE.defaultBlockState());
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.ECHO_SHARD));
                Items.ECHO_SHARD.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(anchor.getCenter(),Direction.UP,anchor,false)));
                ready=true;
            });return;
        }
        if(++wait>300)throw new IllegalStateException("Echo shard client synchronization timeout at phase "+phase);
        var held=mc.player.getMainHandItem();var id=EchoShards.target(held);
        if(phase==1&&ready&&id!=null&&EchoLinkPayload.clientValid(id)){
            if(!held.hasFoil())throw new IllegalStateException("Linked echo shard lacks glint");
            stale=held.copy();oldId=id;phase=2;wait=0;
            server.execute(()->server.overworld().removeBlock(anchor,false));
        }else if(phase==2&&!EchoLinkPayload.clientValid(oldId)){
            if(stale.hasFoil())throw new IllegalStateException("Lodestone break left glint on a stored shard copy");
            phase=3;wait=0;server.execute(()->{
                var player=server.getPlayerList().getPlayer(playerId);var level=player.level();
                level.setBlockAndUpdate(anchor,Blocks.LODESTONE.defaultBlockState());
                Items.ECHO_SHARD.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(anchor.getCenter(),Direction.UP,anchor,false)));
            });
        }else if(phase==3&&id!=null&&!id.equals(oldId)&&EchoLinkPayload.clientValid(id)){
            if(stale.hasFoil()||!held.hasFoil())throw new IllegalStateException("Lodestone replacement revived an obsolete link");
            phase=4;mc.setScreen(new Gallery(stale.copy(),held.copy()));
        }
    }
    private static final class Gallery extends Screen {
        private final ItemStack broken,linked;private int frames;
        Gallery(ItemStack broken,ItemStack linked){super(Component.literal("Echo shard lodestone links"));this.broken=broken;this.linked=linked;}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xFF243340);g.centeredText(font,"Echo Shards — live lodestone link synchronization",width/2,25,-1);
            g.centeredText(font,"Broken lodestone: unlinked",width/3,70,-1);g.centeredText(font,"New link: glint",width*2/3,70,-1);
            g.item(broken,width/3-8,100);g.item(linked,width*2/3-8,100);
            if(++frames==150)ChocolateFlavored.LOGGER.info("ECHO_SHARD_CLIENT_VERIFICATION_PASSED: real server bind/break/rebind packets, linked glint, stored-copy invalidation and non-reviving replacement");
        }
        @Override public boolean isPauseScreen(){return false;}
    }
}
