package com.akiotsukino.chocolateflavored.client.animal;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Native meshes matching Earth2Java's original 64x32 texture UVs. */
public final class LegacyAnimalModels {
    public static LayerDefinition cow() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -6, 8, 8, 6)
                .texOffs(22, 0).addBox(-5, -5, -5, 1, 3, 1).addBox(4, -5, -5, 1, 3, 1), PartPose.offset(0, 4, -8));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 4).addBox(-6, -10, -7, 12, 18, 10)
                .texOffs(52, 0).addBox(-2, 2, -8, 4, 6, 1), PartPose.offsetAndRotation(0, 5, 2, (float) Math.PI / 2, 0, 0));
        CubeListBuilder legs = CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4);
        root.addOrReplaceChild("right_hind_leg", legs, PartPose.offset(-4, 12, 7));
        root.addOrReplaceChild("left_hind_leg", legs, PartPose.offset(4, 12, 7));
        root.addOrReplaceChild("right_front_leg", legs, PartPose.offset(-4, 12, -5));
        root.addOrReplaceChild("left_front_leg", legs, PartPose.offset(4, 12, -5));
        return LayerDefinition.create(mesh, 64, 32);
    }
    public static LayerDefinition pig() {
        MeshDefinition mesh = QuadrupedModel.createBodyMesh(6, true, false, CubeDeformation.NONE);
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4,-4,-8,8,8,8)
                .texOffs(16,16).addBox(-2,0,-9,4,3,1), PartPose.offset(0,12,-6));
        return LayerDefinition.create(mesh, 64,32);
    }
    private LegacyAnimalModels() {}
}
