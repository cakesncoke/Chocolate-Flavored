package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.cake.CakeContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class CakeClientVerification {
    private static boolean opened;private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || opened || !CropClientVerification.completed || ++ticks<180) return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        opened=true;
        var models=mc.getModelManager().getBlockStateModelSet();
        for(var flavor:CakeContent.FLAVORS) {
            verify(mc,flavor.block().get());
            flavor.candles().values().forEach(b->verify(mc,b.get()));
        }
        CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(),true,mc.level.registryAccess());
        var tab=mc.level.registryAccess().lookupOrThrow(Registries.CREATIVE_MODE_TAB).getOrThrow(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.parse("chocolateflavored:chocolate_flavored"))).value();
        if(!tab.getIconItem().is(CakeContent.CHOCOLATE.item().get()))throw new IllegalStateException("Chocolate cake tab icon");
        for(var flavor:CakeContent.FLAVORS)if(tab.getDisplayItems().stream().noneMatch(s->s.is(flavor.item().get())))throw new IllegalStateException("Cake absent from mod creative tab");
        mc.setScreen(new Gallery());
    }
    private static void verify(Minecraft mc,Block block) {
        var models=mc.getModelManager().getBlockStateModelSet();
        for(var state:block.getStateDefinition().getPossibleStates())
            if(models.get(state)==models.missingModel()||models.get(state).particleMaterial().sprite().contents().name().getPath().equals("missingno"))throw new IllegalStateException("Missing cake state model: "+state);
    }
    private static final class Gallery extends Screen {
        private int frames;
        Gallery(){super(Component.literal("Flavored vanilla cakes"));}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xFF342738);
            g.centeredText(font,"Vanilla cakes: seven servings and all seventeen candle types",width/2,20,-1);
            int i=0,cw=width/4;
            for(var flavor:CakeContent.FLAVORS) {
                int x=i++*cw;g.fill(x+3,60,x+cw-3,height-40,0xFF5C414B);
                g.item(flavor.item().get().getDefaultInstance(),x+cw/2-8,height/2-25);
                g.centeredText(font,flavor.item().get().getDefaultInstance().getHoverName(),x+cw/2,height/2+5,-1);
            }
            g.centeredText(font,"Creative tab icon: Chocolate Cake",width/2,height-25,-1);
            if(++frames==150)ChocolateFlavored.LOGGER.info("CAKE_CLIENT_VERIFICATION_PASSED: all cake/slice/lit/unlit candle state models baked; four item textures, creative entries and chocolate cake tab icon");
        }
        @Override public boolean isPauseScreen(){return false;}
    }
    private CakeClientVerification(){}
}
