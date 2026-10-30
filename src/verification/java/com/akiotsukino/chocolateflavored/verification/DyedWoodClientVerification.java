package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.dye.DyedWood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class DyedWoodClientVerification {
    public static boolean completed;
    private static int ticks;private static boolean opened;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!CakeClientVerification.completed||++ticks<180)return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;opened=true;
        var models=mc.getModelManager().getBlockStateModelSet();
        for(var f:DyedWood.FAMILIES.values())for(var b:f.blocks.values())for(var s:b.get().getStateDefinition().getPossibleStates())
            if(models.get(s)==models.missingModel()||models.get(s).particleMaterial().sprite().contents().name().getPath().equals("missingno"))throw new IllegalStateException("Missing dyed wood model: "+s);
        for(var f:DyedWood.FAMILIES.values())for(String kind:List.of("sign","wall_sign","hanging_sign","wall_hanging_sign"))
            for(var state:f.block(kind).getStateDefinition().getPossibleStates()) {
                var parts=new ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
                models.get(state).collectParts(net.minecraft.util.RandomSource.create(0),parts);
                var quads=new ArrayList<net.minecraft.client.resources.model.geometry.BakedQuad>();
                for(var part:parts) {
                    quads.addAll(part.getQuads(null));
                    for(var direction:net.minecraft.core.Direction.values())quads.addAll(part.getQuads(direction));
                }
                if(quads.isEmpty()||quads.stream().anyMatch(q->q.materialInfo().tintIndex()!=0))
                    throw new IllegalStateException("Pastel sign must have tinted wood geometry: "+state);
            }
        mc.gui.setScreen(new Gallery());
    }
    private static final class Gallery extends Screen {
        final List<Boat> boats=new ArrayList<>();int frames;
        Gallery(){super(Component.literal("Pastel wood families"));}
        @Override protected void init(){if(!boats.isEmpty())return;for(var f:DyedWood.FAMILIES.values())boats.add(f.boat.get().create(minecraft.level,EntitySpawnReason.COMMAND));}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xFF20252D);g.centeredText(font,"Pastel wood: all 16 dyes / 13 items / native boats",width/2,4,-1);
            int rowH=(height-29)/16,colW=(width-105)/13;
            for(int i=0;i<16;i++) {
                var f=DyedWood.FAMILIES.get(DyeColor.byId(i));int y=20+i*rowH;
                g.fill(1,y,width-1,y+rowH-1, i%2==0?0xFF39414D:0xFF2D3540);
                g.text(font,Component.literal(f.color.getName()),3,y+4,-1,false);
                for(int j=0;j<13;j++)g.item(f.item(DyedWood.KINDS.get(j)).getDefaultInstance(),78+j*colW,y);
                var boat=boats.get(i);boat.tickCount=frames;
                var renderer=minecraft.getEntityRenderDispatcher().getRenderer(boat);var state=renderer.createRenderState(boat,1);
                state.shadowPieces.clear();state.outlineColor=0;
                g.entity(state,13,new Vector3f(0,.25F,0),new Quaternionf().rotateZ((float)Math.PI).rotateY(.6F),new Quaternionf().rotateX(.3F),width-27,y,width-2,y+rowH);
            }
            if(++frames==150) { completed=true; ChocolateFlavored.LOGGER.info("DYED_WOOD_CLIENT_VERIFICATION_PASSED: every block state baked, all 208 item models and sixteen native tinted boat renderers submitted"); }
        }
        @Override public boolean isPauseScreen(){return false;}
    }
}
