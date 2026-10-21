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
public final class SunkenRenderer extends HumanoidMobRenderer<Sunken,SunkenRenderer.State,SunkenModel> {
    public static final class State extends HumanoidRenderState {public int color;public boolean sheared;}
    public SunkenRenderer(EntityRendererProvider.Context context){
        super(context,new SunkenModel(context.bakeLayer(SunkenClient.LAYER)),.5F);
        addLayer(new HumanoidArmorLayer<>(this,ArmorModelSet.bake(ModelLayers.SKELETON_ARMOR,context.getModelSet(),HumanoidModel::new),context.getEquipmentRenderer()));
    }
    @Override public State createRenderState(){return new State();}
    @Override public Identifier getTextureLocation(State state){return Identifier.fromNamespaceAndPath("chocolateflavored","textures/entity/sunken/"+switch(state.color){case 1->"red";case 2->"yellow";default->"purple";}+".png");}
    @Override public void extractRenderState(Sunken mob,State state,float partial){super.extractRenderState(mob,state,partial);state.color=mob.coralColor();state.sheared=mob.isSheared();}
    @Override protected HumanoidModel.ArmPose getArmPose(Sunken mob,HumanoidArm arm){
        var held=mob.getItemHeldByArm(arm);
        if(held.is(Items.CROSSBOW))return mob.chargingCrossbow()?HumanoidModel.ArmPose.CROSSBOW_CHARGE:HumanoidModel.ArmPose.CROSSBOW_HOLD;
        if(held.is(Items.TRIDENT)&&mob.isAggressive())return HumanoidModel.ArmPose.THROW_SPEAR;
        if(held.is(Items.BOW)&&mob.isAggressive())return HumanoidModel.ArmPose.BOW_AND_ARROW;
        return held.isEmpty()?HumanoidModel.ArmPose.EMPTY:HumanoidModel.ArmPose.ITEM;
    }
}
