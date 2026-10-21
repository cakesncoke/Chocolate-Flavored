package com.akiotsukino.chocolateflavored.client.sunken;
import com.akiotsukino.chocolateflavored.mob.sunken.Sunken;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;
public final class SunkenRenderer extends AbstractSkeletonRenderer<Sunken,SunkenRenderer.State> {
    public static final class State extends net.minecraft.client.renderer.entity.state.SkeletonRenderState {public int color;public boolean sheared;}
    public SunkenRenderer(EntityRendererProvider.Context context){
        super(context,ModelLayers.SKELETON_ARMOR,new SunkenModel(context.bakeLayer(SunkenClient.LAYER)));
    }
    @Override public State createRenderState(){return new State();}
    @Override public Identifier getTextureLocation(State state){return Identifier.fromNamespaceAndPath("chocolateflavored","textures/entity/sunken/"+switch(state.color){case 1->"red";case 2->"yellow";default->"purple";}+".png");}
    @Override public void extractRenderState(Sunken mob,State state,float partial){super.extractRenderState(mob,state,partial);state.color=mob.coralColor();state.sheared=mob.isSheared();}
}
