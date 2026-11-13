package com.akiotsukino.chocolateflavored.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.akiotsukino.chocolateflavored.ModContent;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

/** While the fire starter is held in use, the first-person hand plays vanilla's swing so it looks like rubbing. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
abstract class FireStarterHandMixin {
    @Shadow
    private void swingArm(float attack, PoseStack poseStack, int invert, HumanoidArm arm) {
        throw new AssertionError();
    }

    @Inject(method = "applyItemArmTransform", at = @At("TAIL"))
    private void chocolateflavored$starterSwing(PoseStack poseStack, HumanoidArm arm, float inverseHeight, CallbackInfo callback) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || !player.isUsingItem() || !player.getUseItem().is(ModContent.FIRE_STARTER.get())) {
            return;
        }
        HumanoidArm usedArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        if (usedArm != arm) {
            return;
        }
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        swingArm(player.getSwingAnimation(partial), poseStack, arm == HumanoidArm.RIGHT ? 1 : -1, arm);
    }
}
