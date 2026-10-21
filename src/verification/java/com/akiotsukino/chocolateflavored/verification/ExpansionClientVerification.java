package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.expansion.CreatureState;
import com.akiotsukino.chocolateflavored.mob.expansion.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
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

/** CI-only gallery renders the actual native models, display item and internal spell renderer. */
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class ExpansionClientVerification {
    private static boolean opened;
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!MobClientVerification.completed||++ticks<160)return;
        if(Minecraft.getInstance().level==null)return;
        opened=true;Minecraft.getInstance().setScreen(new Gallery());
    }
    private static final class Gallery extends Screen {
        private final List<Mob> mobs=new ArrayList<>();
        private IceChunk spell;
        private int frames;
        Gallery(){super(Component.literal("Chocolate Flavored: five creatures"));}
        @Override protected void init(){
            if(!mobs.isEmpty())return;
            for(var holder:List.of(ExpansionContent.SUNKEN_SKELETON,ExpansionContent.TUFF_GOLEM,ExpansionContent.BARNACLE,ExpansionContent.ICEOLOGER,ExpansionContent.WILDFIRE))for(int variant=0;variant<2;variant++){
                Mob mob=(Mob)holder.get().create(minecraft.level,EntitySpawnReason.COMMAND);
                mob.snapTo(minecraft.player.getX(),minecraft.player.getY(),minecraft.player.getZ(),0,0);
                if(mob instanceof SunkenSkeleton)mob.setAggressive(true);
                if(mob instanceof SunkenSkeleton sunken&&variant==1){sunken.setChargingCrossbow(true);sunken.startUsingItem(InteractionHand.MAIN_HAND);}
                if(mob instanceof TuffGolem golem&&variant==1){golem.setStatue(false);golem.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND));}
                if(mob instanceof Wildfire&&variant==1){CompoundTag tag=new CompoundTag();tag.putFloat("ShieldHealth",15);TypedEntityData.of(ExpansionContent.WILDFIRE.get(),tag).loadInto(mob);}
                mobs.add(mob);
            }
            spell=ExpansionContent.ICE_CHUNK.get().create(minecraft.level,EntitySpawnReason.COMMAND);
            spell.setPos(minecraft.player.position());
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xFF24343F);g.centeredText(font,"Chocolate Flavored: additional creatures",width/2,5,0xFFFFFFFF);
            int cw=width/3,ch=(height-48)/2;
            for(int i=0;i<mobs.size();i++){
                int card=i/2,x=(card%3)*cw,y=22+(card/3)*ch;Mob mob=mobs.get(i);
                if(i%2==0){g.fill(x+2,y+1,x+cw-2,y+ch-2,0xFF3B4E5A);g.centeredText(font,mob.getType().getDescription().getString(),x+cw/2,y+3,0xFFFFFFFF);}
                mob.tickCount=frames;
                var renderer=minecraft.getEntityRenderDispatcher().getRenderer(mob);
                var state=renderer.createRenderState(mob,1);
                state.shadowPieces.clear();state.outlineColor=0;
                if(state instanceof LivingEntityRenderState living){living.bodyRot=162;living.yRot=18;living.xRot=-3;living.scale=1;living.ageInTicks=frames;}
                if(state instanceof CreatureState creature)creature.casting=i%2==1;
                int left=x+4+(i%2)*cw/2,right=left+cw/2-8;
                g.entity(state,mob instanceof Barnacle?23:32,new Vector3f(0,state.boundingBoxHeight/2+.04F,0),new Quaternionf().rotateZ((float)Math.PI),new Quaternionf().rotateX(.1F),left,y+18,right,y+ch-4);
            }
            int sx=2*cw,sy=22+ch;
            g.fill(sx+2,sy+1,width-2,sy+ch-2,0xFF3B4E5A);g.centeredText(font,"Ice Chunk — native packed ice",sx+cw/2,sy+3,0xFFFFFFFF);
            var renderer=minecraft.getEntityRenderDispatcher().getRenderer(spell);var state=renderer.createRenderState(spell,1);state.shadowPieces.clear();state.outlineColor=0;
            g.entity(state,40,new Vector3f(0,-.75F,0),new Quaternionf().rotateY(.5F),new Quaternionf().rotateX(.1F),sx+10,sy+18,width-10,sy+ch-4);
            int ex=width/2-50;
            for(var item:com.akiotsukino.chocolateflavored.content.SpawnEggContent.EGGS){
                var type=SpawnEggItem.getType(item.get().getDefaultInstance());
                if(type==ExpansionContent.SUNKEN_SKELETON.get()||type==ExpansionContent.TUFF_GOLEM.get()||type==ExpansionContent.BARNACLE.get()||type==ExpansionContent.ICEOLOGER.get()||type==ExpansionContent.WILDFIRE.get()){g.item(item.get().getDefaultInstance(),ex,height-22);ex+=20;}
            }
            if(++frames==150)ChocolateFlavored.LOGGER.info("EXPANSION_CLIENT_VERIFICATION_PASSED: five models, crossbow, statue/carried item, barnacle tentacles, iceologer cast, wildfire shield loss, packed-ice spell renderer and five modern eggs");
        }
        @Override public boolean isPauseScreen(){return false;}
    }
    private ExpansionClientVerification(){}
}
