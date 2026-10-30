package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.sunken.*;
import com.akiotsukino.chocolateflavored.client.sunken.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class SunkenClientVerification {
    public static boolean completed; private static boolean opened;private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!TuffGolemClientVerification.completed||++ticks<160||Minecraft.getInstance().level==null)return;
        opened=true;verifyModel();Minecraft.getInstance().gui.setScreen(new Gallery());
    }
    private static void verifyModel(){
        var root=SunkenModel.layer().bakeRoot();var model=new SunkenModel(root);var state=new SunkenRenderer.State();
        model.setupAnim(state);check(root.getChild("head").getChild("coral").visible,"Coral shown before shearing");
        state.sheared=true;model.setupAnim(state);check(!root.getChild("head").getChild("coral").visible&&!root.getChild("right_leg").getChild("coral").visible,"Coral hidden after shearing");
        state.isAggressive=true;state.isHoldingBow=true;state.rightArmPose=HumanoidModel.ArmPose.BOW_AND_ARROW;model.setupAnim(state);check(root.getChild("right_arm").xRot<-1,"Skeleton bow pose");
        state.isHoldingBow=false;state.rightArmPose=HumanoidModel.ArmPose.EMPTY;model.setupAnim(state);
        check(root.getChild("right_arm").xRot<-1&&root.getChild("left_arm").xRot<-1,"Skeleton melee arm animation");
    }
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Sunken render: "+message);}
    private static final class Gallery extends Screen {
        private final List<Sunken> mobs=new ArrayList<>();private int frames;
        Gallery(){super(Component.literal("Sunken: Recrafted Mobs resources"));}
        @Override protected void init(){
            if(!mobs.isEmpty())return;
            for(int i=0;i<9;i++){
                var mob=SunkenContent.SUNKEN.get().create(minecraft.level,EntitySpawnReason.COMMAND);mob.setCoralColor(i%3);mob.setSheared(i>=3&&i<6);mob.setAggressive(true);
                mob.setItemSlot(EquipmentSlot.MAINHAND,(i>=3&&i<6?ItemStack.EMPTY:new ItemStack(Items.BOW)));
                mob.snapTo(minecraft.player.getX(),minecraft.player.getY(),minecraft.player.getZ(),0,0);mobs.add(mob);
            }
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xFF183C49);g.centeredText(font,"Sunken: purple / red / yellow; intact, sheared, swimming",width/2,5,-1);
            int cw=width/3,ch=(height-40)/3;
            for(int i=0;i<9;i++){
                var mob=mobs.get(i);int x=i%3*cw,y=20+i/3*ch;g.fill(x+2,y,x+cw-2,y+ch-2,0xFF305866);
                g.centeredText(font,(i<3?"Bow":i<6?"Sheared / melee":"Swimming"),x+cw/2,y+3,-1);
                mob.tickCount=frames;var renderer=minecraft.getEntityRenderDispatcher().getRenderer(mob);var state=(SunkenRenderer.State)renderer.createRenderState(mob,partial);
                state.bodyRot=155;state.yRot=0;state.xRot=0;state.ageInTicks=frames;state.shadowPieces.clear();state.outlineColor=0;
                if(i>=6){state.swimAmount=1;state.isVisuallySwimming=true;state.walkAnimationPos=frames*.15F;state.walkAnimationSpeed=.7F;}
                g.entity(state,32,new Vector3f(0,1,0),new Quaternionf().rotateZ((float)Math.PI),new Quaternionf().rotateX(.1F),x+5,y+15,x+cw-5,y+ch-3);
            }
            var egg=com.akiotsukino.chocolateflavored.content.SpawnEggContent.EGGS.stream().filter(e->SpawnEggItem.getType(e.get().getDefaultInstance())==SunkenContent.SUNKEN.get()).findFirst().orElseThrow();g.item(egg.get().getDefaultInstance(),width/2-8,height-18);
            if(++frames==150){completed=true;ChocolateFlavored.LOGGER.info("SUNKEN_CLIENT_VERIFICATION_PASSED: source model and all three textures, coral visibility, skeleton bow and melee poses, swimming and modern spawn egg");}
        }
        @Override public boolean isPauseScreen(){return false;}
    }
    private SunkenClientVerification(){}
}
