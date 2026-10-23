// Adapted from vectorwing/Farmer's Delight, MIT. See THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;


import java.util.Optional;

public record CropBiomeModifier(
		HolderSet<Biome> allowedBiomes,
		Optional<HolderSet<Biome>> deniedBiomes,
		Optional<Float> minimumTemperature,
		Optional<Float> maximumTemperature,
		HolderSet<PlacedFeature> features,
		GenerationStep.Decoration step
) implements BiomeModifier
{

    public static final MapCodec<CropBiomeModifier> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(builder -> builder.group(
        Biome.LIST_CODEC.fieldOf("allowed_biomes").forGetter(CropBiomeModifier::allowedBiomes),
        Biome.LIST_CODEC.optionalFieldOf("denied_biomes").forGetter(CropBiomeModifier::deniedBiomes),
        com.mojang.serialization.Codec.FLOAT.optionalFieldOf("min_temperature").forGetter(CropBiomeModifier::minimumTemperature),
        com.mojang.serialization.Codec.FLOAT.optionalFieldOf("max_temperature").forGetter(CropBiomeModifier::maximumTemperature),
        PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(CropBiomeModifier::features),
        GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(CropBiomeModifier::step)
    ).apply(builder, CropBiomeModifier::new));
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && this.allowedBiomes.contains(biome)) {
			if (deniedBiomes.isPresent() && this.deniedBiomes.get().contains(biome)) {
				return;
			}
			if (minimumTemperature.isPresent() && biome.value().getBaseTemperature() < minimumTemperature.get()) {
				return;
			}
			if (maximumTemperature.isPresent() && biome.value().getBaseTemperature() > maximumTemperature.get()) {
				return;
			}
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			this.features.forEach(holder -> generationSettings.addFeature(this.step, holder));
		}
	}

	@Override
	public MapCodec<? extends BiomeModifier> codec() {
		return CODEC;
	}
}
