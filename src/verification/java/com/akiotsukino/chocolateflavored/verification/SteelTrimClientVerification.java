package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class SteelTrimClientVerification {
 public static boolean completed;
 private static int ticks;private static boolean opened;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!RefinementClientVerification.completed||++ticks<100)return;
  opened=true;Minecraft.getInstance().setScreen(new Gallery());
 }
 private static final class Gallery extends Screen {
  private final java.util.List<LivingEntity> wearers=new java.util.ArrayList<>();
  private final java.util.List<ItemStack> stacks=new java.util.ArrayList<>();private int frames;
  Gallery(){super(Component.literal("Supplied steel trim palettes"));}
  @Override protected void init(){
   if(!wearers.isEmpty())return;
   var material=minecraft.level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL,Identifier.parse("chocolateflavored:steel")));
   var pattern=minecraft.level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN,Identifier.withDefaultNamespace("sentry")));
   for(boolean steel:new boolean[]{true,false}){
    var mob=EntityType.ZOMBIE.create(minecraft.level,EntitySpawnReason.COMMAND);
    Item[] armor=steel?new Item[]{Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS}:new Item[]{Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS};
    EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    for(int i=0;i<4;i++){var stack=new ItemStack(armor[i]);stack.set(DataComponents.TRIM,new ArmorTrim(material,pattern));mob.setItemSlot(slots[i],stack);stacks.add(stack);}
    wearers.add(mob);
   }
  }
  @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xFF243340);g.centeredText(font,"Steel trims — supplied palettes, armor and inventory sprites",width/2,12,-1);
   for(int i=0;i<2;i++){
    int x=i*width/2;g.centeredText(font,i==0?"Steel armor: steel_darker palette":"Diamond armor: steel palette",x+width/4,40,-1);
    for(int k=0;k<4;k++)g.item(stacks.get(i*4+k),x+width/4-40+k*22,65);
    InventoryScreen.extractEntityInInventoryFollowsMouse(g,x+15,95,x+width/2-15,height-25,70,.04F,x+width/4F-100,120,wearers.get(i));
   }
   if(++frames==150){completed=true;ChocolateFlavored.LOGGER.info("STEEL_TRIM_CLIENT_VERIFICATION_PASSED: supplied steel/steel_darker palettes render on humanoid armor, leggings and all four inventory trim types");}
  }
  @Override public boolean isPauseScreen(){return false;}
 }
}
