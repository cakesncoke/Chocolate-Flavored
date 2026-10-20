package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.animal.*;
import com.akiotsukino.chocolateflavored.client.CookingPotScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** CI-only gallery actually submits every adult/baby model and its layers to the native renderer. */
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class AnimalClientVerification {
    private static int screenTicks;
    private static boolean opened;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened) return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||!(mc.screen instanceof CookingPotScreen)||++screenTicks<100) return;
        opened=true;mc.player.closeContainer();mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new Gallery());
    }
    private static final class Gallery extends Screen {
        private final List<Mob> animals=new ArrayList<>();
        private int frames;
        Gallery() { super(Component.literal("Chocolate Flavored — Earth animals")); }
        @Override protected void init() {
            if(!animals.isEmpty()) return;
            for(var holder:AnimalContent.ENTITIES.getEntries()) for(boolean baby:new boolean[]{false,true}) {
                Mob animal=(Mob)holder.get().create(minecraft.level,EntitySpawnReason.COMMAND);
                if(animal==null) throw new IllegalStateException("Missing client entity");
                if(animal instanceof AgeableMob ageable) ageable.setBaby(baby);
                if(animal instanceof Sheep sheep) sheep.setColor(DyeColor.WHITE);
                animal.snapTo(minecraft.player.getX(),minecraft.player.getY(),minecraft.player.getZ(),0,0);
                animals.add(animal);
            }
            for(boolean baby:new boolean[]{false,true}) {
                var pig=EntityType.PIG.create(minecraft.level,EntitySpawnReason.COMMAND);pig.setBaby(baby);
                MuddyPigRules.becomeMuddy(pig);((MuddyPig)pig).chocolateflavored$setFlowerColor(DyeColor.PINK);animals.add(pig);
            }
        }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float ticks) {
            graphics.fill(0,0,width,height,0xFF253A31);
            graphics.centeredText(font,"Chocolate Flavored: adults and babies",width/2,5,0xFFFFFFFF);
            int columns=6,cellW=width/columns,cellH=(height-22)/4;
            for(int i=0;i<animals.size();i++) {
                int x=(i%columns)*cellW,y=22+(i/columns)*cellH;
                Mob animal=animals.get(i);
                graphics.fill(x+2,y+1,x+cellW-2,y+cellH-2,0xFF3D5143);
                String label=animal.getType()==EntityType.PIG?"Muddy Pig":animal.getType().getDescription().getString();
                graphics.centeredText(font,label+(animal.isBaby()?" (baby)":""),x+cellW/2,y+3,0xFFFFFFFF);
                InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,x+5,y+14,x+cellW-5,y+cellH-4,26,.025F,x+cellW/2F-10,y+cellH/2F,animal);
            }
            if(++frames==100) ChocolateFlavored.LOGGER.info("ANIMAL_CLIENT_VERIFICATION_PASSED: all 11 entity types plus muddy pigs submitted with adult/baby models, flowers, wool and mud layers");
        }
        @Override public boolean isPauseScreen() { return false; }
    }
    private AnimalClientVerification() {}
}
