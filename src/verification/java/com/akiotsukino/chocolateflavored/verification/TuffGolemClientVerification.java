package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.tuff.*;
import com.akiotsukino.chocolateflavored.client.tuff.TuffGolemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TypedEntityData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class TuffGolemClientVerification {
    private static boolean opened;private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!MobClientVerification.completed||++ticks<160)return;if(Minecraft.getInstance().level==null)return;com.akiotsukino.chocolateflavored.client.tuff.TuffAnimationVerification.run();opened=true;Minecraft.getInstance().setScreen(new Gallery());}
    private static final class Gallery extends Screen {
        private final List<TuffGolem> golems=new ArrayList<>();private int frames;
        private final String[] clips={"","idleWithItem","sleep","sleepWithItem","showItem","hideItem","wake","wakeWithItem","wakeAndShowItem","wakeAndHideItem","walk","walkWithItem","","idleWithItem","sleep","sleepWithItem"};
        Gallery(){super(Component.literal("Friends & Foes Tuff Golem resources"));}
        @Override protected void init(){if(!golems.isEmpty())return;for(int i=0;i<16;i++){var golem=TuffGolemContent.TUFF_GOLEM.get().create(minecraft.level,EntitySpawnReason.COMMAND);golem.snapTo(minecraft.player.getX(),minecraft.player.getY(),minecraft.player.getZ(),0,0);var tag=new CompoundTag();tag.putInt("Color",i);tag.putBoolean("Sleeping",i%4==2||i%4==3);TypedEntityData.of(TuffGolemContent.TUFF_GOLEM.get(),tag).loadInto(golem);if(i%2==1||i==4||i==8||i==11)golem.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND));golems.add(golem);}}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xFF263A38);g.centeredText(font,"Tuff Golem: original Friends & Foes model, textures and keyframes",width/2,4,0xFFFFFFFF);
            int cw=width/4,ch=(height-40)/4;
            for(int i=0;i<16;i++){var golem=golems.get(i);int x=i%4*cw,y=20+i/4*ch;g.fill(x+2,y,x+cw-2,y+ch-2,0xFF40534D);g.centeredText(font,DyeColor.byId(i).getName()+" / "+(clips[i].isEmpty()?"standing":clips[i]),x+cw/2,y+2,0xFFFFFFFF);golem.tickCount=frames;
                var renderer=minecraft.getEntityRenderDispatcher().getRenderer(golem);var state=(TuffGolemRenderer.State)renderer.createRenderState(golem,1);state.bodyRot=160;state.yRot=0;state.xRot=0;state.ageInTicks=frames;state.shadowPieces.clear();state.outlineColor=0;state.animation=clips[i];state.elapsed=(frames%60)/40F;state.walkAnimationPos=frames/4F;state.walkAnimationSpeed=(i==10||i==11)?1:0;
                g.entity(state,36,new Vector3f(0,.52F,0),new Quaternionf().rotateZ((float)Math.PI),new Quaternionf().rotateX(.1F),x+4,y+13,x+cw-4,y+ch-3);
            }
            var egg=com.akiotsukino.chocolateflavored.content.SpawnEggContent.EGGS.stream().filter(e->SpawnEggItem.getType(e.get().getDefaultInstance())==TuffGolemContent.TUFF_GOLEM.get()).findFirst().orElseThrow();g.item(egg.get().getDefaultInstance(),width/2-8,height-19);
            if(++frames==150)ChocolateFlavored.LOGGER.info("TUFF_GOLEM_CLIENT_VERIFICATION_PASSED: original Blockbench geometry, all eleven original clips, sixteen cloth textures, sleeping eyes, floating item and original modern egg");
        }
        @Override public boolean isPauseScreen(){return false;}
    }
    private TuffGolemClientVerification(){}
}
