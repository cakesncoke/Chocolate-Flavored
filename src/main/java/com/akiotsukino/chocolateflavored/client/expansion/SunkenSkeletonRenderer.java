package com.akiotsukino.chocolateflavored.client.expansion;
import com.akiotsukino.chocolateflavored.mob.expansion.SunkenSkeleton;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;
public final class SunkenSkeletonRenderer extends AbstractSkeletonRenderer<SunkenSkeleton,SkeletonRenderState> {
    public SunkenSkeletonRenderer(EntityRendererProvider.Context context){super(context,CreatureRenderer.layer("sunken_skeleton"),ModelLayers.SKELETON_ARMOR);}
    @Override public SkeletonRenderState createRenderState(){return new SkeletonRenderState();}
    @Override public Identifier getTextureLocation(SkeletonRenderState state){return Identifier.fromNamespaceAndPath("chocolateflavored","textures/entity/sunken_skeleton/sunken_skeleton.png");}
    @Override protected HumanoidModel.ArmPose getArmPose(SunkenSkeleton mob,HumanoidArm arm){
        if(mob.getMainArm()==arm&&mob.getMainHandItem().is(Items.CROSSBOW))return mob.isChargingCrossbow()?HumanoidModel.ArmPose.CROSSBOW_CHARGE:HumanoidModel.ArmPose.CROSSBOW_HOLD;
        return super.getArmPose(mob,arm);
    }
}
