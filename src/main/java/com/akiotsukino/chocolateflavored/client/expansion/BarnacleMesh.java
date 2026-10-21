// SPDX-License-Identifier: CC-BY-NC-3.0
// Native conversion of Ultimate Earth & Dungeons Variants squid/squid2.jem.
// Source pack: TheFancyFedora; ReCrafted Mobs base by GillsBills; original designs Mojang.
package com.akiotsukino.chocolateflavored.client.expansion;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
public final class BarnacleMesh {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition modelRoot = mesh.getRoot();
        PartDefinition p0 = modelRoot.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,24.0F,0F,0F,0F,0F));
        PartDefinition p1 = p0.addOrReplaceChild("barnacle",CubeListBuilder.create().texOffs(48,0).addBox(-4.0F,-5.0F,-4.0F,8.0F,8.0F,8.0F).texOffs(48,16).addBox(-4.0F,-17.0F,-4.0F,8.0F,12.0F,8.0F).texOffs(80,0).addBox(-0.5F,3.0F,-0.5F,1.0F,21.0F,1.0F),PartPose.offsetAndRotation(0F,-3.0F,-1.0F,0F,0F,0F));
        PartDefinition p2 = p1.addOrReplaceChild("tentacles",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p3 = p2.addOrReplaceChild("barnacle_tentacle1",CubeListBuilder.create(),PartPose.offsetAndRotation(-3.0F,0F,4.0F,0F,0F,0F));
        PartDefinition p4 = p3.addOrReplaceChild("inner_barnacle_tentacle1",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-3.0F,0F,-3.0F,6.0F,22.0F,6.0F),PartPose.offsetAndRotation(0F,0F,0F,0F,3.141593F,0F));
        PartDefinition p5 = p2.addOrReplaceChild("barnacle_tentacle3",CubeListBuilder.create(),PartPose.offsetAndRotation(-3.0F,0F,-2.0F,0F,0F,0F));
        PartDefinition p6 = p5.addOrReplaceChild("inner_barnacle_tentacle3",CubeListBuilder.create().texOffs(24,0).mirror().addBox(-3.0F,0F,-3.0F,6.0F,22.0F,6.0F),PartPose.offsetAndRotation(0F,0F,0F,0F,-3.141593F,0F));
        PartDefinition p7 = p2.addOrReplaceChild("barnacle_tentacle4",CubeListBuilder.create(),PartPose.offsetAndRotation(3.0F,0F,-2.0F,0F,0F,0F));
        PartDefinition p8 = p7.addOrReplaceChild("inner_barnacle_tentacle4",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-3.0F,0F,-3.0F,6.0F,22.0F,6.0F),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p9 = p2.addOrReplaceChild("barnacle_tentacle2",CubeListBuilder.create(),PartPose.offsetAndRotation(3.0F,0F,4.0F,0F,0F,0F));
        PartDefinition p10 = p9.addOrReplaceChild("inner_barnacle_tentacle2",CubeListBuilder.create().texOffs(24,0).mirror().addBox(-3.0F,0F,-3.0F,6.0F,22.0F,6.0F),PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F));
        PartDefinition p11 = modelRoot.addOrReplaceChild("tentacle1",CubeListBuilder.create(),PartPose.offsetAndRotation(-5.0F,31.0F,0F,0F,0F,0F));
        PartDefinition p12 = modelRoot.addOrReplaceChild("tentacle2",CubeListBuilder.create(),PartPose.offsetAndRotation(-3.5F,31.0F,-3.5F,0F,0F,0F));
        PartDefinition p13 = modelRoot.addOrReplaceChild("tentacle3",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,31.0F,-5.0F,0F,0F,0F));
        PartDefinition p14 = modelRoot.addOrReplaceChild("tentacle4",CubeListBuilder.create(),PartPose.offsetAndRotation(3.5F,31.0F,-3.5F,0F,0F,0F));
        PartDefinition p15 = modelRoot.addOrReplaceChild("tentacle5",CubeListBuilder.create(),PartPose.offsetAndRotation(-10.0F,31.0F,-5.0F,0F,1.570796F,0F));
        PartDefinition p16 = modelRoot.addOrReplaceChild("tentacle6",CubeListBuilder.create(),PartPose.offsetAndRotation(3.5F,31.0F,3.5F,0F,0F,0F));
        PartDefinition p17 = modelRoot.addOrReplaceChild("tentacle7",CubeListBuilder.create(),PartPose.offsetAndRotation(0F,31.0F,1.65F,0F,3.141593F,0F));
        PartDefinition p18 = modelRoot.addOrReplaceChild("tentacle8",CubeListBuilder.create(),PartPose.offsetAndRotation(-3.5F,31.0F,3.5F,0F,0F,0F));
        return LayerDefinition.create(mesh,128,64);
    }
}
