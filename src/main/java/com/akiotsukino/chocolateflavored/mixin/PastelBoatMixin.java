package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.client.dye.PastelBoatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(AbstractBoatRenderer.class)
public abstract class PastelBoatMixin {
    @Redirect(method="submit", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private <S> void chocolateflavored$tint(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poses,
            Identifier texture, int light, int overlay, int outline, ModelFeatureRenderer.CrumblingOverlay breaking) {
        int color = (Object)this instanceof PastelBoatRenderer pastel ? pastel.color : -1;
        collector.submitModel(model, state, poses, model.renderType(texture), light, overlay, color, null, outline, breaking);
    }
}
