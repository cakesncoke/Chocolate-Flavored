package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.item.SpearItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Modern spear use allows full movement/sprinting, unlike 1.21.1's default item-use slowdown. */
@Mixin(LocalPlayer.class)
public abstract class SpearMovementMixin {
    @Redirect(method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean chocolateflavored$spearDoesNotSlowMovement(LocalPlayer player) {
        return player.isUsingItem() && !(player.getUseItem().getItem() instanceof SpearItem);
    }
}
