package com.akiotsukino.chocolateflavored.client.sunken;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;

/** Reads Recrafted Mobs' original CEM geometry/UVs, retaining native humanoid articulation. */
public final class SunkenModel extends HumanoidModel<SunkenRenderer.State> {
    public SunkenModel(ModelPart root){super(root);}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
        try(var input=Objects.requireNonNull(SunkenModel.class.getResourceAsStream("/assets/chocolateflavored/upstream/recrafted_mobs/sunken.jem"));var reader=new InputStreamReader(input,StandardCharsets.UTF_8)){
            var asset=JsonParser.parseReader(reader).getAsJsonObject();
            for(var value:asset.getAsJsonArray("models")){
                var part=value.getAsJsonObject();String name=part.get("part").getAsString();if(name.equals("headwear"))name="hat";
                var pivot=switch(name){case "left_arm"->PartPose.offset(5,2,0);case "right_arm"->PartPose.offset(-5,2,0);case "left_leg"->PartPose.offset(2,12,0);case "right_leg"->PartPose.offset(-2,12,0);default->PartPose.ZERO;};
                var base=CubeListBuilder.create();var coral=CubeListBuilder.create();var translate=part.getAsJsonArray("translate");
                if(part.has("boxes")){
                    int index=0;for(var box:part.getAsJsonArray("boxes"))addBox(index++==0?base:coral,box.getAsJsonObject(),translate,part.has("mirrorTexture"));
                }
                if(part.has("submodels"))for(var child:part.getAsJsonArray("submodels")){
                    var sub=child.getAsJsonObject();for(var box:sub.getAsJsonArray("boxes"))addBox(coral,box.getAsJsonObject(),translate,sub.has("mirrorTexture"));
                }
                var definition=root.addOrReplaceChild(name,base,pivot);definition.addOrReplaceChild("coral",coral,PartPose.ZERO);
            }
            var size=asset.getAsJsonArray("textureSize");return LayerDefinition.create(mesh,size.get(0).getAsInt(),size.get(1).getAsInt());
        }catch(IOException exception){throw new IllegalStateException("Cannot load original Sunken model",exception);}
    }
    private static void addBox(CubeListBuilder builder,JsonObject box,JsonArray translate,boolean mirror){
        var c=box.getAsJsonArray("coordinates");var uv=box.getAsJsonArray("textureOffset");
        float x=c.get(0).getAsFloat(),y=c.get(1).getAsFloat(),z=c.get(2).getAsFloat(),w=c.get(3).getAsFloat(),h=c.get(4).getAsFloat(),d=c.get(5).getAsFloat();
        builder.texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).mirror(mirror).addBox(-x-w-translate.get(0).getAsFloat(),-y-h-translate.get(1).getAsFloat(),z+translate.get(2).getAsFloat(),w,h,d);
    }
    @Override public void setupAnim(SunkenRenderer.State state){
        super.setupAnim(state);head.getChild("coral").visible=!state.sheared;rightLeg.getChild("coral").visible=!state.sheared;
    }
    @Override public void translateToHand(SunkenRenderer.State state,HumanoidArm arm,PoseStack pose){
        root().translateAndRotate(pose);var part=getArm(arm);float offset=arm==HumanoidArm.RIGHT?1:-1;part.x+=offset;part.translateAndRotate(pose);part.x-=offset;
    }
}
