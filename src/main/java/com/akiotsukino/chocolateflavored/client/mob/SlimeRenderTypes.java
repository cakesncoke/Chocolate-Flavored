package com.akiotsukino.chocolateflavored.client.mob;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import com.akiotsukino.chocolateflavored.mixin.EntityPipelineAccessor;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
/** Shared vanilla entity shader with the reference's 48-frame texture UV animation. */
public final class SlimeRenderTypes {
 public static final RenderPipeline PIPELINE=RenderPipeline.builder(EntityPipelineAccessor.chocolateflavored$entitySnippet()).withLocation(MobContent.id("pipeline/tropical_slime")).withShaderDefine("APPLY_TEXTURE_MATRIX").withShaderDefine("ALPHA_CUTOUT",.1F).withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.SAMPLER1).withCull(false).build();
 private static final RenderType[] FRAMES=new RenderType[48];
 public static RenderType entityAnimationWithAllTexture(Identifier texture,int duration,int frames,int tick){
  int frame=Math.floorMod(tick*(frames-1)/duration,frames);
  if(FRAMES[frame]==null){final float offset=frame/(float)frames;FRAMES[frame]=RenderType.create("chocolateflavored:tropical_slime_"+frame,RenderSetup.builder(PIPELINE).withTexture("Sampler0",texture).setTextureTransform(new TextureTransform("slime_frame",()->new Matrix4f().translation(0,offset,0))).useLightmap().useOverlay().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup());}
  return FRAMES[frame];
 }
 private SlimeRenderTypes(){}
}
