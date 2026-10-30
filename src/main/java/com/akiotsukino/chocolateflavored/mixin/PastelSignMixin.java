package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.dye.DyedWood;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 26.2 renders placed wood as tinted block models; keep the editor's wood tinted too. */
@Mixin({SignEditScreen.class, HangingSignEditScreen.class})
public abstract class PastelSignMixin extends AbstractSignEditScreen {
    protected PastelSignMixin(SignBlockEntity sign, boolean front, boolean filter) {
        super(sign, front, filter);
    }

    @Redirect(method="extractSignBackground", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void chocolateflavored$tint(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture,
            int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        var dye = DyedWood.BLOCK_COLORS.get(this.sign.getBlockState().getBlock());
        graphics.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight,
                dye == null ? -1 : DyedWood.pastel(dye));
    }
}
