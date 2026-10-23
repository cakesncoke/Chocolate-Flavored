package com.akiotsukino.chocolateflavored.crop;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class CropWorldgen {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ChocolateFlavored.MODID);
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ChocolateFlavored.MODID);
    static {
        FEATURES.register("crop_patch", () -> new CropPatchFeature(CropPatchConfiguration.CODEC));
        FEATURES.register("small_crop_patch", SmallCropPatchFeature::new);
        FEATURES.register("wild_rice_patch", WildRiceFeature::new);
        MODIFIERS.register("crop_biome_filter", () -> CropBiomeModifier.CODEC);
    }
    public static void register(IEventBus bus) { FEATURES.register(bus); MODIFIERS.register(bus); }
    private CropWorldgen() {}
}
