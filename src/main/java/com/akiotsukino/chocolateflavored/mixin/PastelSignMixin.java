package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.dye.DyedWood;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tint the wood only. Native front/back text, glow ink, wax and editing stay untouched. */
@Mixin(AbstractSignRenderer.class)
public abstract class PastelSignMixin {
    @Unique private int chocolateflavored$woodColor = -1;
    @Inject(method="submit(Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at=@At("HEAD"))
    private void chocolateflavored$color(SignRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        var dye = DyedWood.BLOCK_COLORS.get(((BlockEntityRenderStateAccessor)state).chocolateflavored$blockState().getBlock());
        chocolateflavored$woodColor = dye == null ? -1 : DyedWood.pastel(dye);
    }
    @ModifyArg(method="submitSign", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index=5)
    private int chocolateflavored$tint(int original) { return chocolateflavored$woodColor; }
}
