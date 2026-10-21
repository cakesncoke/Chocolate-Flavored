package com.akiotsukino.chocolateflavored.client.tuff;
import com.akiotsukino.chocolateflavored.mob.tuff.TuffGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.util.Mth;
public final class TuffGolemRenderer extends MobRenderer<TuffGolem,TuffGolemRenderer.State,TuffGolemModel> {
    public static final class State extends LivingEntityRenderState {
        public boolean sleeping,carrying;public int color;public String animation="";public float elapsed;
        final ItemStackRenderState item=new ItemStackRenderState();
    }
    private static Identifier texture(String file){return Identifier.fromNamespaceAndPath("chocolateflavored","textures/entity/tuff_golem/"+file+".png");}
    public TuffGolemRenderer(EntityRendererProvider.Context context){
        super(context,new TuffGolemModel(context.bakeLayer(TuffGolemClient.LAYER)),.35F);
        addLayer(new RenderLayer<State,TuffGolemModel>(this){
            @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,State state,float yaw,float pitch){
                if(state.isInvisible)return;
                coloredCutoutModelCopyLayerRender(getParentModel(),texture(DyeColor.byId(state.color).getName()),pose,collector,light,state,-1,1);
                if(state.sleeping)coloredCutoutModelCopyLayerRender(getParentModel(),texture("closed_eyes"),pose,collector,light,state,-1,1);
                if(state.carrying&&state.deathTime==0){
                    pose.pushPose();pose.translate(0,.72F+Mth.sin(state.ageInTicks/10F+Mth.PI)*.05F+.05F,-.575F);pose.mulPose(Axis.XP.rotationDegrees(180));pose.mulPose(Axis.YP.rotation(state.ageInTicks*.05F));
                    state.item.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,state.outlineColor);pose.popPose();
                }
            }
        });
    }
    private static float transitionDuration(String animation){
        return switch(animation){case "showItem","hideItem" -> .458334F;case "wakeAndShowItem","wakeAndHideItem" -> 1.375F;default -> .916667F;};
    }
    @Override public State createRenderState(){return new State();}
    @Override public Identifier getTextureLocation(State state){return texture("tuff_golem");}
    @Override public void extractRenderState(TuffGolem mob,State state,float partial){
        super.extractRenderState(mob,state,partial);state.sleeping=mob.sleeping();state.carrying=mob.holding();state.color=mob.color();state.animation=mob.animation();state.elapsed=mob.animationSeconds(partial);
        net.minecraft.client.Minecraft.getInstance().getItemModelResolver().updateForTopItem(state.item,mob.getMainHandItem(),ItemDisplayContext.GROUND,mob.level(),mob,mob.getId());
        if(mob.sleeping()||mob.glued()||(!state.animation.isEmpty()&&state.elapsed<transitionDuration(state.animation)))state.walkAnimationSpeed=0;
    }
}
