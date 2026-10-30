package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.horn.CopperHornItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class HornClientVerification {
 public static boolean completed;
 private static int ticks;private static boolean opened;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!SteelTrimClientVerification.completed||++ticks<100)return;
  opened=true;Minecraft.getInstance().gui.setScreen(new Gallery());
 }
 private static final class Gallery extends Screen {
  private final ItemStack plain=new ItemStack(ModContent.COPPER_HORN.get());
  private final ItemStack linked=new ItemStack(ModContent.COPPER_HORN.get());private int frames;
  Gallery(){super(Component.literal("Copper Horn"));CopperHornItem.link(linked,java.util.UUID.randomUUID());}
  @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xFF243340);g.centeredText(font,"Copper Horn — supplied texture and linked glint",width/2,25,-1);
   g.centeredText(font,"Unlinked",width/3,70,-1);g.centeredText(font,"Linked",width*2/3,70,-1);
   g.item(plain,width/3-8,100);g.item(linked,width*2/3-8,100);
   g.centeredText(font,plain.getHoverName().getString(),width/2,150,-1);
   g.centeredText(font,new ItemStack(Items.GOAT_HORN).getHoverName().getString(),width/2,175,-1);
   if(++frames==150){completed=true;
    if(!new ItemStack(Items.GOAT_HORN).getHoverName().getString().equals("Horn")||plain.hasFoil()||!linked.hasFoil())throw new IllegalStateException("Horn client name/glint");
    ChocolateFlavored.LOGGER.info("HORN_CLIENT_VERIFICATION_PASSED: supplied copper horn sprite, linked enchantment glint, Horn rename");
   }
  }
  @Override public boolean isPauseScreen(){return false;}
 }
}
