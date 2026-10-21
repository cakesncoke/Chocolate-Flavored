// SPDX-License-Identifier: MIT
// Model adapted from Dungeons Mobs, Patrigan and contributors.
package com.akiotsukino.chocolateflavored.client.expansion;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
public final class SunkenSkeletonMesh {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.getChild("head").addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
        partdefinition.getChild("head").addOrReplaceChild("head_coral_1", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,new CubeDeformation(.015F)), PartPose.ZERO);
        partdefinition.getChild("head").addOrReplaceChild("head_coral_2", CubeListBuilder.create().texOffs(44, 10).addBox(-1.0F, -3.0F, 2.0F, 6.0F, 1.0F, 4.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
        partdefinition.getChild("right_leg").addOrReplaceChild("right_leg_coral", CubeListBuilder.create().texOffs(52, 1).addBox(-2.0F, 3.0F, -2.1F, 2.0F, 1.0F, 4.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
}
