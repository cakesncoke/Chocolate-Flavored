package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.horn.CopperHornItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
abstract class CopperHornSubtitleMixin {
    @Inject(method = "onPlaySound", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$hideCopperHornSubtitle(SoundInstance sound, WeighedSoundEvents sounds,
            float range, CallbackInfo callback) {
        var player = Minecraft.getInstance().player;
        if (player == null || sound.getSource() != SoundSource.RECORDS
                || player.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) >= 1) return;
        var stack = player.getUseItem();
        if (!(stack.getItem() instanceof CopperHornItem)) return;
        var instrument = stack.get(DataComponents.INSTRUMENT);
        if (instrument != null && sound.getIdentifier().equals(instrument.instrument().value().soundEvent().value().location())) {
            callback.cancel();
        }
    }
}
