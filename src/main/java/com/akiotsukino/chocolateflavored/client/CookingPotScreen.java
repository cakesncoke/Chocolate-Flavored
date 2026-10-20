package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.cooking.CookingPotMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** No recipe-book component, button, widget, or recipe update listener. */
public final class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/gui/cooking_pot.png");
    private static final Identifier BOWL = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/item/empty_container_slot_bowl.png");
    public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); titleLabelX = 28;
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.isHeated()) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 47, topPos + 55, 176, 0, 17, 15, 256, 256);
        int width = menu.progressWidth();
        if (width > 0) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 89, topPos + 25, 176, 15, width, 17, 256, 256);
        if (!menu.getSlot(7).hasItem()) graphics.blit(RenderPipelines.GUI_TEXTURED, BOWL, leftPos + 92, topPos + 55, 0, 0, 16, 16, 16, 16);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (isHovering(47, 55, 17, 15, mouseX, mouseY)) graphics.setTooltipForNextFrame(font,
                Component.translatable(menu.isHeated() ? "container.chocolateflavored.cooking_pot.heated" : "container.chocolateflavored.cooking_pot.not_heated"), mouseX, mouseY);
    }
}
