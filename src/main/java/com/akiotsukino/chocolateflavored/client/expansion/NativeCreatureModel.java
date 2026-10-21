// SPDX-License-Identifier: MIT
// Iceologer/Wildfire geometry adapted from Infamous-Misadventures/Dungeons-Mobs (Patrigan, MIT).
// Barnacle geometry adapted from Ultimate Earth & Dungeons Variants (CC BY-NC 3.0); see THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.client.expansion;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;

public final class NativeCreatureModel extends EntityModel<CreatureState> {
    private final String kind;
    private final Map<String,ModelPart> parts = new HashMap<>();
    public NativeCreatureModel(ModelPart root,String kind) {
        super(root); this.kind=kind;
        switch(kind) {
            case "iceologer" -> {
                parts.put("root",root.getChild("root"));
                parts.put("bipedLegs",root.getChild("root").getChild("bipedLegs"));
                parts.put("bipedLegLeft",root.getChild("root").getChild("bipedLegs").getChild("bipedLegLeft"));
                parts.put("armorBipedLeftLeg",root.getChild("root").getChild("bipedLegs").getChild("bipedLegLeft").getChild("armorBipedLeftLeg"));
                parts.put("armorBipedLeftFoot",root.getChild("root").getChild("bipedLegs").getChild("bipedLegLeft").getChild("armorBipedLeftFoot"));
                parts.put("bipedLegRight",root.getChild("root").getChild("bipedLegs").getChild("bipedLegRight"));
                parts.put("armorBipedRightLeg",root.getChild("root").getChild("bipedLegs").getChild("bipedLegRight").getChild("armorBipedRightLeg"));
                parts.put("armorBipedRightFoot",root.getChild("root").getChild("bipedLegs").getChild("bipedLegRight").getChild("armorBipedRightFoot"));
                parts.put("bipedBody",root.getChild("root").getChild("bipedBody"));
                parts.put("bipedArms",root.getChild("root").getChild("bipedBody").getChild("bipedArms"));
                parts.put("bipedArmLeft",root.getChild("root").getChild("bipedBody").getChild("bipedArms").getChild("bipedArmLeft"));
                parts.put("bipedHandLeft",root.getChild("root").getChild("bipedBody").getChild("bipedArms").getChild("bipedArmLeft").getChild("bipedHandLeft"));
                parts.put("armorBipedLeftArm",root.getChild("root").getChild("bipedBody").getChild("bipedArms").getChild("bipedArmLeft").getChild("armorBipedLeftArm"));
                parts.put("bipedArmRight",root.getChild("root").getChild("bipedBody").getChild("bipedArms").getChild("bipedArmRight"));
                parts.put("bipedHandRight",root.getChild("root").getChild("bipedBody").getChild("bipedArms").getChild("bipedArmRight").getChild("bipedHandRight"));
                parts.put("armorBipedRightArm",root.getChild("root").getChild("bipedBody").getChild("bipedArms").getChild("bipedArmRight").getChild("armorBipedRightArm"));
                parts.put("illagerArms",root.getChild("root").getChild("bipedBody").getChild("illagerArms"));
                parts.put("armorIllagerLeftArm",root.getChild("root").getChild("bipedBody").getChild("illagerArms").getChild("armorIllagerLeftArm"));
                parts.put("armorIllagerRightArm",root.getChild("root").getChild("bipedBody").getChild("illagerArms").getChild("armorIllagerRightArm"));
                parts.put("armorIllagerArmsMiddle",root.getChild("root").getChild("bipedBody").getChild("illagerArms").getChild("armorIllagerArmsMiddle"));
                parts.put("bipedCape",root.getChild("root").getChild("bipedBody").getChild("bipedCape"));
                parts.put("bipedPotionSlot",root.getChild("root").getChild("bipedBody").getChild("bipedPotionSlot"));
                parts.put("armorBipedBody",root.getChild("root").getChild("bipedBody").getChild("armorBipedBody"));
                parts.put("bipedHeadBaseRotator",root.getChild("root").getChild("bipedBody").getChild("bipedHeadBaseRotator"));
                parts.put("bipedHead",root.getChild("root").getChild("bipedBody").getChild("bipedHeadBaseRotator").getChild("bipedHead"));
                parts.put("armorBipedHead",root.getChild("root").getChild("bipedBody").getChild("bipedHeadBaseRotator").getChild("bipedHead").getChild("armorBipedHead"));
            }
            case "wildfire" -> {
                parts.put("everything",root.getChild("everything"));
                parts.put("head",root.getChild("everything").getChild("head"));
                parts.put("armorHead",root.getChild("everything").getChild("head").getChild("armorHead"));
                parts.put("rod",root.getChild("everything").getChild("rod"));
                parts.put("shields",root.getChild("everything").getChild("shields"));
                parts.put("shield2",root.getChild("everything").getChild("shields").getChild("shield2"));
                parts.put("shield1",root.getChild("everything").getChild("shields").getChild("shield1"));
                parts.put("shield3",root.getChild("everything").getChild("shields").getChild("shield3"));
                parts.put("shield4",root.getChild("everything").getChild("shields").getChild("shield4"));
            }
            case "barnacle" -> {
                parts.put("body",root.getChild("body"));
                parts.put("barnacle",root.getChild("body").getChild("barnacle"));
                parts.put("tentacles",root.getChild("body").getChild("barnacle").getChild("tentacles"));
                parts.put("barnacle_tentacle1",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle1"));
                parts.put("inner_barnacle_tentacle1",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle1").getChild("inner_barnacle_tentacle1"));
                parts.put("barnacle_tentacle3",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle3"));
                parts.put("inner_barnacle_tentacle3",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle3").getChild("inner_barnacle_tentacle3"));
                parts.put("barnacle_tentacle4",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle4"));
                parts.put("inner_barnacle_tentacle4",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle4").getChild("inner_barnacle_tentacle4"));
                parts.put("barnacle_tentacle2",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle2"));
                parts.put("inner_barnacle_tentacle2",root.getChild("body").getChild("barnacle").getChild("tentacles").getChild("barnacle_tentacle2").getChild("inner_barnacle_tentacle2"));
                parts.put("tentacle1",root.getChild("tentacle1"));
                parts.put("tentacle2",root.getChild("tentacle2"));
                parts.put("tentacle3",root.getChild("tentacle3"));
                parts.put("tentacle4",root.getChild("tentacle4"));
                parts.put("tentacle5",root.getChild("tentacle5"));
                parts.put("tentacle6",root.getChild("tentacle6"));
                parts.put("tentacle7",root.getChild("tentacle7"));
                parts.put("tentacle8",root.getChild("tentacle8"));
            }
        }
    }
    @Override public void setupAnim(CreatureState s) {
        super.setupAnim(s);
        if (kind.equals("wildfire")) {
            parts.get("head").yRot += s.yRot*Mth.DEG_TO_RAD;
            parts.get("head").xRot += s.xRot*Mth.DEG_TO_RAD;
            parts.get("everything").y += Mth.sin(s.ageInTicks*.08F)*.6F;
            parts.get("shields").yRot += s.ageInTicks*.035F;
            for(int i=1;i<=4;i++)parts.get("shield"+i).visible=i<=s.shields;
        } else if (kind.equals("iceologer")) {
            parts.get("bipedHead").xRot += s.xRot*Mth.DEG_TO_RAD;
            parts.get("bipedHead").yRot += s.yRot*Mth.DEG_TO_RAD;
            parts.forEach((name,part)-> { if(name.startsWith("armor") || name.equals("bipedCape") || name.equals("bipedPotionSlot"))part.visible=false; });
            parts.get("illagerArms").visible=!s.casting;
            parts.get("bipedArmLeft").visible=s.casting; parts.get("bipedArmRight").visible=s.casting;
            parts.get("bipedLegLeft").xRot+=Mth.cos(s.walkAnimationPos*.6662F)*1.4F*s.walkAnimationSpeed;
            parts.get("bipedLegRight").xRot+=Mth.cos(s.walkAnimationPos*.6662F+Mth.PI)*1.4F*s.walkAnimationSpeed;
            if(s.casting){ parts.get("bipedArmLeft").xRot=-2.2F;parts.get("bipedArmRight").xRot=-2.2F;parts.get("bipedArmLeft").zRot=-.3F;parts.get("bipedArmRight").zRot=.3F; }
        } else {
            for(int i=1;i<=4;i++){
                ModelPart tentacle=parts.get("barnacle_tentacle"+i);
                tentacle.xRot += Mth.sin(s.ageInTicks*.12F+i)*(.15F+(s.casting?.3F:0));
                tentacle.zRot += (i%2==0?-1:1)*.15F;
            }
        }
    }
    public static LayerDefinition iceologerLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition modelRoot = mesh.getRoot();
        PartDefinition p0 = modelRoot.addOrReplaceChild("root",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,22.0F,0F,0F,0F,0F));
        PartDefinition p1 = p0.addOrReplaceChild("bipedLegs",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,-10.0F,0F,0F,0F,0F));
        PartDefinition p2 = p1.addOrReplaceChild("bipedLegLeft",CubeListBuilder.create().texOffs(0,22).mirror().addBox(-2.0F,0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)).mirror(false),PartPose.offsetAndRotation(2.0F,0F,0F,0F,0F,0F));
        PartDefinition p3 = p2.addOrReplaceChild("armorBipedLeftLeg",CubeListBuilder.create().texOffs(16,48).addBox(-2.0F,0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p4 = p2.addOrReplaceChild("armorBipedLeftFoot",CubeListBuilder.create().texOffs(16,48).addBox(-2.0F,0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p5 = p1.addOrReplaceChild("bipedLegRight",CubeListBuilder.create().texOffs(0,22).addBox(-2.0F,0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(-2.0F,0F,0F,0F,0F,0F));
        PartDefinition p6 = p5.addOrReplaceChild("armorBipedRightLeg",CubeListBuilder.create().texOffs(0,16).addBox(-2.0F,0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p7 = p5.addOrReplaceChild("armorBipedRightFoot",CubeListBuilder.create().texOffs(0,16).addBox(-2.0F,0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p8 = p0.addOrReplaceChild("bipedBody",CubeListBuilder.create().texOffs(16,20).addBox(-4.0F,-10.0F,-3.0F,8.0F,12.0F,6.0F,new CubeDeformation(0F)).texOffs(0,38).addBox(-4.0F,-10.0F,-3.0F,8.0F,16.0F,6.0F,new CubeDeformation(0.25F)),PartPose.offsetAndRotation(0F,-12.0F,0F,0F,0F,0F));
        PartDefinition p9 = p8.addOrReplaceChild("bipedArms",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,-8.0F,0F,0F,0F,0F));
        PartDefinition p10 = p9.addOrReplaceChild("bipedArmLeft",CubeListBuilder.create().texOffs(40,46).addBox(-1.0F,-2.0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(5.0F,0F,0F,0F,0F,0F));
        PartDefinition p11 = p10.addOrReplaceChild("bipedHandLeft",CubeListBuilder.create().texOffs(0,0).addBox(0F,0F,0F,0F,0F,0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(1.0F,11.0F,0F,0F,0F,0F));
        PartDefinition p12 = p10.addOrReplaceChild("armorBipedLeftArm",CubeListBuilder.create().texOffs(0,48).addBox(-1.0F,-2.0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p13 = p9.addOrReplaceChild("bipedArmRight",CubeListBuilder.create().texOffs(40,46).mirror().addBox(-3.0F,-2.0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)).mirror(false),PartPose.offsetAndRotation(-5.0F,0F,0F,0F,0F,0F));
        PartDefinition p14 = p13.addOrReplaceChild("bipedHandRight",CubeListBuilder.create().texOffs(0,0).addBox(0F,0F,0F,0F,0F,0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(-1.0F,11.0F,0F,0F,0F,0F));
        PartDefinition p15 = p13.addOrReplaceChild("armorBipedRightArm",CubeListBuilder.create().texOffs(0,48).addBox(-3.0F,-2.0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p16 = p8.addOrReplaceChild("illagerArms",CubeListBuilder.create().texOffs(44,22).mirror().addBox(4.0F,0F,-2.0F,4.0F,8.0F,4.0F,new CubeDeformation(0F)).mirror(false).texOffs(44,22).addBox(-8.0F,0F,-2.0F,4.0F,8.0F,4.0F,new CubeDeformation(0F)).texOffs(40,34).addBox(-4.0F,4.0F,-2.0F,8.0F,4.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-8.5F,0.3F,0.750492F,0F,0F));
        PartDefinition p17 = p16.addOrReplaceChild("armorIllagerLeftArm",CubeListBuilder.create().texOffs(0,48).addBox(-1.0F,-1.5F,-2.3F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(5.0F,0F,0F,0F,0F,0F));
        PartDefinition p18 = p16.addOrReplaceChild("armorIllagerRightArm",CubeListBuilder.create().texOffs(0,48).addBox(-3.0F,-1.5F,-2.3F,4.0F,12.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(-5.0F,0F,0F,0F,0F,0F));
        PartDefinition p19 = p16.addOrReplaceChild("armorIllagerArmsMiddle",CubeListBuilder.create().texOffs(0,56).addBox(-4.0F,4.0F,-2.0F,8.0F,4.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p20 = p8.addOrReplaceChild("bipedCape",CubeListBuilder.create().texOffs(42,0).addBox(-5.0F,0F,0F,10.0F,16.0F,1.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-10.0F,3.0F,-0.1309F,0F,0F));
        PartDefinition p21 = p8.addOrReplaceChild("bipedPotionSlot",CubeListBuilder.create().texOffs(0,0).addBox(1.0F,0F,0F,0F,0F,0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(4.0F,1.0F,0F,0F,0F,0F));
        PartDefinition p22 = p8.addOrReplaceChild("armorBipedBody",CubeListBuilder.create().texOffs(0,46).addBox(-4.0F,0F,-3.0F,8.0F,12.0F,6.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-10.0F,0F,0F,0F,0F));
        PartDefinition p23 = p8.addOrReplaceChild("bipedHeadBaseRotator",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,-10.0F,0F,0F,0F,0F));
        PartDefinition p24 = p23.addOrReplaceChild("bipedHead",CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-10.0F,-4.0F,8.0F,10.0F,8.0F,new CubeDeformation(0F)).texOffs(24,0).addBox(-1.0F,-3.0F,-6.0F,2.0F,4.0F,2.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p25 = p24.addOrReplaceChild("armorBipedHead",CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-2.0F,0F,0F,0F,0F));
        return LayerDefinition.create(mesh,64,64);
    }
    public static LayerDefinition wildfireLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition modelRoot = mesh.getRoot();
        PartDefinition p0 = modelRoot.addOrReplaceChild("everything",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,9.0F,0F,0F,0F,0F));
        PartDefinition p1 = p0.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-7.0F,0F,0F,0F,0F));
        PartDefinition p2 = p1.addOrReplaceChild("armorHead",CubeListBuilder.create().texOffs(32,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0.02F)),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p3 = p0.addOrReplaceChild("rod",CubeListBuilder.create().texOffs(2,40).addBox(-2.0F,0F,-2.0F,4.0F,20.0F,4.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-6.0F,0F,0F,0F,0F));
        PartDefinition p4 = p0.addOrReplaceChild("shields",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,14.0F,0F,0F,0F,0F));
        PartDefinition p5 = p4.addOrReplaceChild("shield2",CubeListBuilder.create().texOffs(20,16).addBox(-5.0F,-8.0F,-1.0F,10.0F,17.0F,2.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(-13.0F,-13.0F,0F,0F,-1.570796F,0.305433F));
        PartDefinition p6 = p4.addOrReplaceChild("shield1",CubeListBuilder.create().texOffs(20,16).addBox(-5.0F,-8.0F,-1.0F,10.0F,17.0F,2.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-13.0F,-13.0F,0.305433F,0F,0F));
        PartDefinition p7 = p4.addOrReplaceChild("shield3",CubeListBuilder.create().texOffs(20,16).addBox(-5.0F,-8.0F,-1.0F,10.0F,17.0F,2.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(0F,-13.0F,13.0F,-2.83616F,0F,3.141593F));
        PartDefinition p8 = p4.addOrReplaceChild("shield4",CubeListBuilder.create().texOffs(20,16).addBox(-5.0F,-8.0F,-1.0F,10.0F,17.0F,2.0F,new CubeDeformation(0F)),PartPose.offsetAndRotation(13.0F,-13.0F,0F,0F,1.570796F,-0.305433F));
        return LayerDefinition.create(mesh,64,64);
    }
}
