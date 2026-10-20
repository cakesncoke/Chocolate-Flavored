package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin {
    @Shadow @Final private DataSlot cost;

    // NeoForge wraps vanilla calculation in this method. Its sole hasInfiniteMaterials
    // call guards the 40-level cap. Keep valid outputs for the subsequent update event,
    // which sets their final cost; do not change compatibility or payment checks.
    @Redirect(method = "createResultInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"), remap = false)
    private boolean chocolateflavored$uncappedResult(Player player) { return true; }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$allowFreeResult(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> callback) {
        if (cost.get() == 0) callback.setReturnValue(hasItem);
    }
}
