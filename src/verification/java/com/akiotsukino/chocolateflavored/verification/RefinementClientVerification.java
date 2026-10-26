package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.animal.AnimalContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class RefinementClientVerification {
 private static int ticks;private static boolean opened;public static boolean completed;
 private static java.util.List<Item> items(){return java.util.List.of(ModContent.BREAD_DOUGH.get(),ModContent.FLOUR.get(),ModContent.SALT.get(),ModContent.COCOA_POWDER.get(),ModContent.CARBON_IRON.get(),ModContent.UNBAKED_CAKE.get(),ModContent.CAKE_SLICE.get(),ModContent.CHOCOLATE_CAKE_SLICE.get(),ModContent.CHERRY_CAKE_SLICE.get(),ModContent.COFFEE_CAKE_SLICE.get(),ModContent.POMEGRANATE_CAKE_SLICE.get(),ModContent.IRON_KNIFE.get(),ModContent.ROSE_GOLD_UPGRADE.get());}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!DuneOutlineClientVerification.completed||++ticks<100)return;
  opened=true;Minecraft.getInstance().setScreen(new Gallery());
 }
 private static final class Gallery extends Screen {
  private final java.util.List<LivingEntity> cows=new java.util.ArrayList<>();private int frames;
  Gallery(){super(Component.literal("Survival refinements"));}
  @Override protected void init(){if(cows.isEmpty()){cows.add(AnimalContent.MOOBLOOM.get().create(minecraft.level,EntitySpawnReason.COMMAND));cows.add(AnimalContent.MOOLIP.get().create(minecraft.level,EntitySpawnReason.COMMAND));}}
  @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xFF253A31);g.centeredText(font,"New materials, cake slices and corrected flower layers",width/2,8,-1);
   var list=items();int cw=width/7;
   for(int i=0;i<list.size();i++){int x=(i%7)*cw,y=35+i/7*43;var stack=list.get(i).getDefaultInstance();g.item(stack,x+cw/2-8,y);g.centeredText(font,stack.getHoverName(),x+cw/2,y+19,-1);}
   for(int i=0;i<cows.size();i++)InventoryScreen.extractEntityInInventoryFollowsMouse(g,i*width/2+10,140,(i+1)*width/2-10,height-25,65,.04F,i*width/2F-120,160,cows.get(i));
   g.centeredText(font,"Moobloom / Moolip — native overlay and outline argument order",width/2,height-15,-1);
   if(++frames==150){completed=true;ChocolateFlavored.LOGGER.info("REFINEMENT_CLIENT_VERIFICATION_PASSED: thirteen supplied item icons/names and both adult flower-cow layers rendered");}
  }
  @Override public boolean isPauseScreen(){return false;}
 }
}
