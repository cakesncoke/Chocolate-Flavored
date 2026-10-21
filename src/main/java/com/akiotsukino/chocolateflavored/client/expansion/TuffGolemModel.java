package com.akiotsukino.chocolateflavored.client.expansion;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
/** Original native mesh using only referenced vanilla tuff, wool and black-concrete textures. */
public final class TuffGolemModel extends HumanoidModel<TuffGolemState> {
    public TuffGolemModel(ModelPart root){super(root);}
    public static LayerDefinition layer(int kind){
        MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
        root.addOrReplaceChild("head",kind==0?CubeListBuilder.create().texOffs(0,0).addBox(-3,-6,-3,6,6,6).texOffs(0,0).addBox(-1,-3,-4,2,2,1):kind==1?CubeListBuilder.create().texOffs(0,0).addBox(-2,-3.7F,-3.1F,1,1,.2F).addBox(1,-3.7F,-3.1F,1,1,.2F):CubeListBuilder.create(),PartPose.offset(0,14,0));
        root.getChild("head").addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
        root.addOrReplaceChild("body",kind==0?CubeListBuilder.create().texOffs(0,0).addBox(-3,0,-2,6,6,4):kind==2?CubeListBuilder.create().texOffs(0,0).addBox(-3.1F,0,-2.15F,6.2F,5.8F,.2F).addBox(-3.1F,0,1.95F,6.2F,5.8F,.2F):CubeListBuilder.create(),PartPose.offset(0,14,0));
        root.addOrReplaceChild("right_arm",kind==0?CubeListBuilder.create().texOffs(0,0).addBox(-2,0,-1,2,6,2):CubeListBuilder.create(),PartPose.offset(-3,14,0));
        root.addOrReplaceChild("left_arm",kind==0?CubeListBuilder.create().texOffs(0,0).addBox(0,0,-1,2,6,2):CubeListBuilder.create(),PartPose.offset(3,14,0));
        root.addOrReplaceChild("right_leg",kind==0?CubeListBuilder.create().texOffs(0,0).addBox(-1,0,-1.5F,2,4,3):CubeListBuilder.create(),PartPose.offset(-1.7F,20,0));
        root.addOrReplaceChild("left_leg",kind==0?CubeListBuilder.create().texOffs(0,0).addBox(-1,0,-1.5F,2,4,3):CubeListBuilder.create(),PartPose.offset(1.7F,20,0));
        return LayerDefinition.create(mesh,64,32);
    }
    @Override public void setupAnim(TuffGolemState state){
        super.setupAnim(state);
        head.xRot=state.statue?0:state.xRot*Mth.DEG_TO_RAD;head.yRot=state.statue?0:state.yRot*Mth.DEG_TO_RAD;
        rightLeg.xRot=state.statue?0:Mth.cos(state.walkAnimationPos*.8F)*state.walkAnimationSpeed;
        leftLeg.xRot=state.statue?0:-rightLeg.xRot;
        rightArm.xRot=state.carrying?-1.1F:state.statue?0:-rightLeg.xRot;
        leftArm.xRot=state.carrying?-1.1F:state.statue?0:-leftLeg.xRot;
    }
}
