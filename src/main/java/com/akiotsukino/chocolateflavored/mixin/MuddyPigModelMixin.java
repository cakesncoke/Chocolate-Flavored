package com.akiotsukino.chocolateflavored.mixin;
import com.akiotsukino.chocolateflavored.client.animal.AnimalClient;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(QuadrupedModel.class)
public abstract class MuddyPigModelMixin {
    @Shadow protected ModelPart head;
    @Shadow protected ModelPart body;
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void chocolateflavored$shake(LivingEntityRenderState state, CallbackInfo ci) {
        float shake = state.getRenderDataOrDefault(AnimalClient.SHAKE, 0F);
        if (shake > 0F) {
            float roll = Mth.sin(shake * (float) Math.PI) * Mth.sin(shake * (float) Math.PI * 11F) * .15F * (float) Math.PI;
            head.zRot += roll; body.yRot += roll;
        }
    }
}
