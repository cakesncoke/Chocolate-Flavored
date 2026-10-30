package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Renders the actual baked item definitions at each wear stage, including repair. */
@EventBusSubscriber(modid=ChocolateFlavored.MODID, value=Dist.CLIENT)
public final class CopperOxidationClientVerification {
    private static int ticks;
    private static boolean opened;
    private static final List<Item> TOOLS=List.of(Items.COPPER_SWORD, Items.COPPER_PICKAXE,
            Items.COPPER_AXE, Items.COPPER_SHOVEL, Items.COPPER_HOE);
    private static final List<String> NAMES=List.of("sword", "pickaxe", "axe", "shovel", "hoe");

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || opened
                || !EchoShardClientVerification.completed || ++ticks<100) return;
        var mc=Minecraft.getInstance();
        if (mc.level==null) return;
        for (String tool:NAMES) {
            if (!(mc.getModelManager().getItemModel(Identifier.withDefaultNamespace("copper_"+tool))
                    instanceof RangeSelectItemModel)) throw new IllegalStateException("Copper damage model missing: "+tool);
        }
        opened=true;
        mc.gui.setScreen(new Gallery());
    }

    private static final class Gallery extends Screen {
        private final ItemStack[][] samples=new ItemStack[5][5];
        private int frames;
        Gallery() {
            super(Component.literal("Copper tool oxidation"));
            for (int row=0;row<5;row++) {
                for (int col=0;col<5;col++) {
                    ItemStack stack=new ItemStack(TOOLS.get(row));
                    stack.setDamageValue((int)Math.ceil(stack.getMaxDamage()*(col<4?col*.25:.9)));
                    if (col==4) stack.setDamageValue(0);
                    samples[row][col]=stack;
                }
            }
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xFF243340);
            g.centeredText(font,"Copper tools — durability-based oxidation",width/2,18,-1);
            String[] stages={"Fresh", "Exposed (25%)", "Weathered (50%)", "Oxidized (75%)", "Repaired"};
            int spacing=width/5;
            for (int col=0;col<5;col++) {
                int x=col*spacing+spacing/2;
                g.centeredText(font,stages[col],x,48,-1);
                for (int row=0;row<5;row++) {
                    int y=80+row*43;
                    g.item(samples[row][col],x-8,y);
                    g.centeredText(font,NAMES.get(row),x,y+20,-1);
                }
            }
            if (++frames==150) ChocolateFlavored.LOGGER.info("COPPER_OXIDATION_CLIENT_VERIFICATION_PASSED: five native baked damage-dispatch models, four wear stages and repaired appearance rendered");
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
