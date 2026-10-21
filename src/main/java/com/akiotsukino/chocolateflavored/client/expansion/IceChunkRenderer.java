package com.akiotsukino.chocolateflavored.client.expansion;
import com.akiotsukino.chocolateflavored.mob.expansion.IceChunk;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
public final class IceChunkRenderer extends EntityRenderer<IceChunk,IceChunkRenderer.State> {
    public static final class State extends EntityRenderState { final BlockModelRenderState block=new BlockModelRenderState(); }
    private final BlockModelResolver blocks;
    private static final BlockDisplayContext CONTEXT=BlockDisplayContext.create();
    public IceChunkRenderer(EntityRendererProvider.Context context){super(context);blocks=context.getBlockModelResolver();}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(IceChunk ice,State state,float ticks){super.extractRenderState(ice,state,ticks);blocks.update(state.block,Blocks.PACKED_ICE.defaultBlockState(),CONTEXT);}
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){
        pose.pushPose();pose.scale(1.5F,1.5F,1.5F);pose.translate(-.5,0,-.5);state.block.submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,state.outlineColor);pose.popPose();super.submit(state,pose,collector,camera);
    }
}
