package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.sheep.SheepColorSpawnRules;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepColorSpawnRules.class)
public abstract class SheepSpawnColorMixin {
    /** Keep vanilla's 82/100 * 1/500 pink roll; map all other natural colors to white. */
    @Inject(method = "getSheepColor", at = @At("RETURN"), cancellable = true)
    private static void chocolateflavored$white(Holder<Biome> biome, RandomSource random, CallbackInfoReturnable<DyeColor> cir) {
        if (cir.getReturnValue() != DyeColor.PINK) cir.setReturnValue(DyeColor.WHITE);
    }
}
