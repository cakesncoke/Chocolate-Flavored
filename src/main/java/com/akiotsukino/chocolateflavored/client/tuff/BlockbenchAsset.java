package com.akiotsukino.chocolateflavored.client.tuff;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

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
    private record Frame(float time,float x,float y,float z){}
    void animate(ModelPart root,String name,float seconds,boolean loop){
        var animation=animations.get(name);if(animation==null)throw new IllegalArgumentException("Missing original animation: "+name);
        float length=animation.get("length").getAsFloat();float time=loop?seconds%length:Mth.clamp(seconds,0,length);
        Map<String,ModelPart> parts=new HashMap<>();for(var part:source.getAsJsonArray("outliner"))findParts(root,part.getAsJsonObject(),parts);
        for(var entry:animation.getAsJsonObject("animators").entrySet()){
            var animator=entry.getValue().getAsJsonObject();ModelPart part=parts.get(animator.get("name").getAsString());if(part==null)continue;
            Map<String,List<Frame>> channels=new HashMap<>();
            for(var frameElement:animator.getAsJsonArray("keyframes")){
                var frame=frameElement.getAsJsonObject();if(!frame.get("interpolation").getAsString().equals("linear"))throw new IllegalArgumentException("Unsupported Blockbench interpolation");
                var point=frame.getAsJsonArray("data_points").get(0).getAsJsonObject();channels.computeIfAbsent(frame.get("channel").getAsString(),key->new ArrayList<>()).add(new Frame(frame.get("time").getAsFloat(),point.get("x").getAsFloat(),point.get("y").getAsFloat(),point.get("z").getAsFloat()));
            }
            for(var channel:channels.entrySet()){
                var frames=channel.getValue();frames.sort(Comparator.comparingDouble(Frame::time));Frame a=frames.getFirst(),b=a;
                for(var frame:frames){if(frame.time<=time)a=frame;if(frame.time>=time){b=frame;break;}b=frame;}
                float delta=b.time==a.time?0:Mth.clamp((time-a.time)/(b.time-a.time),0,1);
                float x=Mth.lerp(delta,a.x,b.x),y=Mth.lerp(delta,a.y,b.y),z=Mth.lerp(delta,a.z,b.z);
                switch(channel.getKey()){
                    case "rotation"->{part.xRot+=x*Mth.DEG_TO_RAD;part.yRot+=y*Mth.DEG_TO_RAD;part.zRot+=z*Mth.DEG_TO_RAD;}
                    case "position"->{part.x+=x;part.y-=y;part.z+=z;}
                    case "scale"->{part.xScale*=x;part.yScale*=y;part.zScale*=z;}
                    default->throw new IllegalArgumentException("Unsupported Blockbench channel "+channel.getKey());
                }
            }
        }
    }
    private static void findParts(ModelPart parent,JsonObject bone,Map<String,ModelPart> parts){String name=bone.get("name").getAsString();var part=parent.getChild(name);parts.put(name,part);for(var child:bone.getAsJsonArray("children"))if(child.isJsonObject())findParts(part,child.getAsJsonObject(),parts);}
}
