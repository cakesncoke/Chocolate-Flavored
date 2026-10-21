package com.akiotsukino.chocolateflavored.client.tuff;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.animation.*;

/** Reads original, unmodified Blockbench box-UV assets; no handwritten model geometry/keyframes. */
final class BlockbenchAsset {
    private final JsonObject source;
    private final Map<String,JsonObject> cubes=new HashMap<>();
    private final Map<String,JsonObject> animations=new HashMap<>();
    BlockbenchAsset(String path){
        try(var input=Objects.requireNonNull(BlockbenchAsset.class.getResourceAsStream(path),path);var reader=new InputStreamReader(input,StandardCharsets.UTF_8)){source=JsonParser.parseReader(reader).getAsJsonObject();}
        catch(IOException e){throw new IllegalStateException("Cannot read Blockbench asset "+path,e);}
        for(var element:source.getAsJsonArray("elements")){var cube=element.getAsJsonObject();cubes.put(cube.get("uuid").getAsString(),cube);}
        for(var element:source.getAsJsonArray("animations")){var animation=element.getAsJsonObject();animations.put(animation.get("name").getAsString(),animation);}
    }
    private static float v(JsonObject object,String name,int axis){return object.getAsJsonArray(name).get(axis).getAsFloat();}
    LayerDefinition layer(){
        var mesh=new MeshDefinition();
        for(var entry:source.getAsJsonArray("outliner"))bone(mesh.getRoot(),entry.getAsJsonObject(),null);
        var resolution=source.getAsJsonObject("resolution");
        return LayerDefinition.create(mesh,resolution.get("width").getAsInt(),resolution.get("height").getAsInt());
    }
    private void bone(PartDefinition parent,JsonObject bone,JsonObject parentBone){
        var builder=CubeListBuilder.create();
        for(var child:bone.getAsJsonArray("children"))if(child.isJsonPrimitive()){
            var cube=cubes.get(child.getAsString());
            float dx=v(cube,"to",0)-v(cube,"from",0),dy=v(cube,"to",1)-v(cube,"from",1),dz=v(cube,"to",2)-v(cube,"from",2);
            int u=0,w=0;
            if(cube.has("uv_offset")){u=cube.getAsJsonArray("uv_offset").get(0).getAsInt();w=cube.getAsJsonArray("uv_offset").get(1).getAsInt();}
            else {var uv=cube.getAsJsonObject("faces").getAsJsonObject("east").getAsJsonArray("uv");u=Math.round(uv.get(0).getAsFloat());w=Math.round(uv.get(1).getAsFloat()-dz);}
            boolean mirror=cube.has("mirror_uv")&&cube.get("mirror_uv").getAsBoolean();
            builder.texOffs(u,w).mirror(mirror).addBox(v(bone,"origin",0)-v(cube,"to",0),v(bone,"origin",1)-v(cube,"to",1),v(cube,"from",2)-v(bone,"origin",2),dx,dy,dz,new CubeDeformation(cube.has("inflate")?cube.get("inflate").getAsFloat():0));
        }
        float px=parentBone==null?0:v(parentBone,"origin",0),py=parentBone==null?24:v(parentBone,"origin",1),pz=parentBone==null?0:v(parentBone,"origin",2);
        var part=parent.addOrReplaceChild(bone.get("name").getAsString(),builder,PartPose.offset(px-v(bone,"origin",0),py-v(bone,"origin",1),v(bone,"origin",2)-pz));
        for(var child:bone.getAsJsonArray("children"))if(child.isJsonObject())bone(part,child.getAsJsonObject(),bone);
    }
    Map<String,KeyframeAnimation> bakeAnimations(ModelPart root) {
        Map<String,KeyframeAnimation> result=new HashMap<>();
        for(var entry:animations.entrySet()) {
            var animation=entry.getValue();
            var builder=AnimationDefinition.Builder.withLength(animation.get("length").getAsFloat());
            if(animation.get("loop").getAsString().equals("loop"))builder.looping();
            for(var element:animation.getAsJsonObject("animators").entrySet()) {
                var animator=element.getValue().getAsJsonObject();
                Map<String,List<Keyframe>> channels=new HashMap<>();
                for(var value:animator.getAsJsonArray("keyframes")) {
                    var frame=value.getAsJsonObject();
                    if(!frame.get("interpolation").getAsString().equals("linear"))throw new IllegalArgumentException("Unsupported interpolation");
                    var point=frame.getAsJsonArray("data_points").get(0).getAsJsonObject();
                    float x=point.get("x").getAsFloat(),y=point.get("y").getAsFloat(),z=point.get("z").getAsFloat();
                    String channel=frame.get("channel").getAsString();
                    var vector=switch(channel) {
                        case "position" -> KeyframeAnimations.posVec(x,y,z);
                        case "rotation" -> KeyframeAnimations.degreeVec(x,y,z);
                        case "scale" -> KeyframeAnimations.scaleVec(x,y,z);
                        default -> throw new IllegalArgumentException("Unsupported channel "+channel);
                    };
                    channels.computeIfAbsent(channel,key->new ArrayList<>()).add(new Keyframe(frame.get("time").getAsFloat(),vector,AnimationChannel.Interpolations.LINEAR));
                }
                for(var channel:channels.entrySet()) {
                    channel.getValue().sort(Comparator.comparingDouble(Keyframe::timestamp));
                    var target=switch(channel.getKey()) {
                        case "position" -> AnimationChannel.Targets.POSITION;
                        case "rotation" -> AnimationChannel.Targets.ROTATION;
                        case "scale" -> AnimationChannel.Targets.SCALE;
                        default -> throw new IllegalArgumentException(channel.getKey());
                    };
                    builder.addAnimation(animator.get("name").getAsString(),new AnimationChannel(target,channel.getValue().toArray(Keyframe[]::new)));
                }
            }
            result.put(entry.getKey(),builder.build().bake(root));
        }
        return Map.copyOf(result);
    }
}
