package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.crop.CropContent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class CropClientVerification {
    public static boolean completed;
    private static boolean opened;
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || opened || !SunkenClientVerification.completed || ++ticks < 180) return;
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        opened = true;
        var models = minecraft.getModelManager().getBlockStateModelSet();
        for (String name : List.of("blueberry_bush","soybean_crop","chili_crop","cotton","coffee","cabbages","onions","tomatoes","rice_crop","rice_panicles",
                "wild_soybean","wild_chili","wild_cotton","wild_coffee","wild_cabbages","wild_onions","wild_tomatoes","wild_carrots","wild_potatoes","wild_beetroots","wild_rice")) {
            var block = BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("chocolateflavored", name));
            for (var state : block.getStateDefinition().getPossibleStates()) {
                if (models.get(state) == models.missingModel() || models.get(state).particleMaterial().sprite().contents().name().getPath().equals("missingno")) throw new IllegalStateException("Missing crop model: " + state);
            }
        }
        minecraft.gui.setScreen(new Gallery());
    }
    private static final class Gallery extends Screen {
        private final List<ItemStack> items = new ArrayList<>();
        private int frames;
        Gallery() { super(Component.literal("Crop resources")); }
        @Override protected void init() {
            if (!items.isEmpty()) return;
            for (String name : List.of("blueberries","soybeans","soybean_pod","chili","chili_seeds","cotton_boll","cotton_seeds","coffee_beans","cabbage","cabbage_seeds","onion","tomato","tomato_seeds","rice","rice_panicle",
                "wild_soybean","wild_chili","wild_cotton","wild_coffee","wild_cabbages","wild_onions","wild_tomatoes","wild_carrots","wild_potatoes","wild_beetroots","wild_rice")) {
                items.add(new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("chocolateflavored", name))));
            }
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
            g.fill(0,0,width,height,0xFF273D29);
            g.centeredText(font,"Standalone crops: raw harvests, seeds and wild plants",width/2,8,-1);
            int cellW = width / 5, cellH = (height - 30) / 6;
            for (int i=0; i<items.size(); i++) {
                int x = i%5*cellW, y = 27+i/5*cellH;
                g.fill(x+2,y,x+cellW-2,y+cellH-2,0xFF3C5737);
                g.item(items.get(i),x+cellW/2-8,y+3);
                g.centeredText(font,items.get(i).getHoverName(),x+cellW/2,y+23,-1);
            }
            if (++frames == 150) { completed = true; ChocolateFlavored.LOGGER.info("CROP_CLIENT_VERIFICATION_PASSED: all registered crop state models baked; harvest, seed and wild plant icons rendered"); }
        }
        @Override public boolean isPauseScreen() { return false; }
    }
    private CropClientVerification() {}
}
