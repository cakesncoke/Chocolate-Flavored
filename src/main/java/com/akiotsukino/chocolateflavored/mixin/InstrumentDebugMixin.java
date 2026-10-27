package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.client.InstrumentHud;
import net.minecraft.client.gui.components.debug.DebugEntryPosition;
import net.minecraft.client.gui.components.debug.DebugEntrySectionPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DebugEntryPosition.class, DebugEntrySectionPosition.class})
public abstract class InstrumentDebugMixin {
    @Inject(method = "display", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$requireMeter(CallbackInfo ci) {
        if (!InstrumentHud.precision()) ci.cancel();
    }
}
