package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import com.akiotsukino.chocolateflavored.mob.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class MobClientVerification {
 private static boolean opened;private static int ticks;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!AnimalClientVerification.completed||++ticks<20)return;var mc=Minecraft.getInstance();if(mc.level==null)return;opened=true;mc.setScreen(new Gallery());}
 private static final class Gallery extends Screen {
  private final List<Mob> mobs=new ArrayList<>();private int frames;
  Gallery(){super(Component.literal("Chocolate Flavored — Earth monsters"));}
  @Override protected void init(){if(!mobs.isEmpty())return;
   for(String key:List.of("ambient","growl","hurt","death","pant","whine","step","shake"))if(!net.minecraft.locale.Language.getInstance().has("subtitles.mob.skeleton_wolf."+key))throw new IllegalStateException("Missing wolf subtitle: "+key);
   for(var holder:List.of(MobContent.MELON_GOLEM,MobContent.BOULDERING_ZOMBIE,MobContent.BOULDERING_DROWNED,MobContent.SWAMPED,MobContent.SKELETON_WOLF,MobContent.BONE_SPIDER,MobContent.TROPICAL_SLIME))for(int variant=0;variant<2;variant++){
    Mob mob=(Mob)holder.get().create(minecraft.level,EntitySpawnReason.COMMAND);mob.snapTo(minecraft.player.getX(),minecraft.player.getY(),minecraft.player.getZ(),0,0);mob.setAggressive(variant==1);
    if(mob instanceof net.minecraft.world.entity.monster.zombie.Zombie){mob.setBaby(variant==1);mob.setItemSlot(EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(Items.IRON_HELMET));mob.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(mob instanceof BoulderingDrowned?Items.TRIDENT:Items.IRON_AXE));}
    if(mob instanceof BoulderingZombie z)z.setClimbing(variant==1);
    if(mob instanceof SkeletonWolf wolf&&variant==1){wolf.tame(minecraft.player);wolf.startPersistentAngerTimer();wolf.setBodyArmorItem(new net.minecraft.world.item.ItemStack(Items.WOLF_ARMOR));}
    if(mob instanceof TropicalSlime slime){slime.setSize(2,true);slime.setHasFish(variant==0);}
    if(mob instanceof BoneSpider spider&&variant==1)spider.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.SLOWNESS));
    mobs.add(mob);
   }
  }
  @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xFF202B34);g.centeredText(font,"Chocolate Flavored: Earth monsters",width/2,5,0xFFFFFFFF);int cw=width/3,ch=(height-20)/3;
   for(int i=0;i<mobs.size();i++){int card=i/2,x=(card%3)*cw,y=20+(card/3)*ch;var mob=mobs.get(i);if(i%2==0){g.fill(x+2,y+1,x+cw-2,y+ch-2,0xFF354653);g.centeredText(font,mob.getType().getDescription().getString(),x+cw/2,y+3,0xFFFFFFFF);}mob.tickCount=frames;int left=x+5+(i%2)*(cw/2),right=left+cw/2-10;InventoryScreen.extractEntityInInventoryFollowsMouse(g,left,y+16,right,y+ch-5,27,.025F,(left+right)/2F-100,y+ch/2F,mob);}
   if(++frames==150)ChocolateFlavored.LOGGER.info("MOB_CLIENT_VERIFICATION_PASSED: seven forms, adult/baby armor, angry golem head, bone eyes and animated tropical slime submitted");
  }
  @Override public boolean isPauseScreen(){return false;}
 }
 private MobClientVerification(){}
}
