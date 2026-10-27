package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.cooking.CookingPotMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Vanilla-sized panel and player inventory, without a recipe book. */
public final class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/gui/cooking_pot.png");
    public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void init() { super.init(); titleLabelX = imageWidth - font.width(title) - 8; }
    private void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(leftPos+x-1, topPos+y-1, leftPos+x+17, topPos+y+17, 0xFF373737);
        g.fill(leftPos+x, topPos+y, leftPos+x+17, topPos+y+17, 0xFFFFFFFF);
        g.fill(leftPos+x, topPos+y, leftPos+x+16, topPos+y+16, 0xFF8B8B8B);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Replace only the old controls; retain the original panel border and player inventory.
        g.fill(leftPos+4, topPos+4, leftPos+imageWidth-4, topPos+80, 0xFFC6C6C6);
        for (int i=0;i<6;i++) { slot(g,7+i%3*18,16+i/3*18); slot(g,115+i%3*18,16+i/3*18); }
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+76, topPos+26, 89,25,24,17,256,256);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+79, topPos+62, 47,55,17,15,256,256);
        if (menu.isHeated()) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+79, topPos+62,176,0,17,15,256,256);
        int width=menu.progressWidth();
        if(width>0) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+76, topPos+26,176,15,width,17,256,256);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g,mouseX,mouseY,partialTick);
        if(isHovering(79,62,17,15,mouseX,mouseY)) g.setTooltipForNextFrame(font,
                Component.translatable(menu.isHeated()?"container.chocolateflavored.cooking_pot.heated":"container.chocolateflavored.cooking_pot.not_heated"),mouseX,mouseY);
    }
}
