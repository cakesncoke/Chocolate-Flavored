package com.akiotsukino.chocolateflavored.client.expansion;
import com.akiotsukino.chocolateflavored.mob.expansion.TuffGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
public final class TuffGolemRenderer extends HumanoidMobRenderer<TuffGolem,TuffGolemState,TuffGolemModel> {
    public TuffGolemRenderer(EntityRendererProvider.Context context){
        super(context,new TuffGolemModel(context.bakeLayer(CreatureRenderer.layer("tuff_golem"))),.3F);
        addLayer(new RenderLayer<TuffGolemState,TuffGolemModel>(this){
            final TuffGolemModel eyes=new TuffGolemModel(context.bakeLayer(CreatureRenderer.layer("tuff_golem_eyes")));
            final TuffGolemModel cloak=new TuffGolemModel(context.bakeLayer(CreatureRenderer.layer("tuff_golem_cloak")));
            @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,TuffGolemState state,float yaw,float pitch){
                coloredCutoutModelCopyLayerRender(eyes,Identifier.withDefaultNamespace("textures/block/black_concrete.png"),pose,collector,light,state,-1,1);
                coloredCutoutModelCopyLayerRender(cloak,Identifier.withDefaultNamespace("textures/block/"+DyeColor.byId(state.cloak).getName()+"_wool.png"),pose,collector,light,state,-1,1);
            }
        });
    }
    @Override public TuffGolemState createRenderState(){return new TuffGolemState();}
    @Override public Identifier getTextureLocation(TuffGolemState state){return Identifier.withDefaultNamespace("textures/block/tuff.png");}
    @Override public void extractRenderState(TuffGolem golem,TuffGolemState state,float ticks){super.extractRenderState(golem,state,ticks);state.statue=golem.isStatue();state.cloak=golem.cloakColor();state.carrying=!golem.getMainHandItem().isEmpty();}
}
