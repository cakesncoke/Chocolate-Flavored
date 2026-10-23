// Adapted from vectorwing/Farmer's Delight, MIT. See THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


import java.util.Optional;

public record CropPatchConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> primaryFeature, Holder<PlacedFeature> secondaryFeature, Holder<PlacedFeature> floorFeature
) implements FeatureConfiguration
{
	public static final Codec<CropPatchConfiguration> CODEC = RecordCodecBuilder.create((config) -> config.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(CropPatchConfiguration::tries),
			Codec.intRange(2, 64).fieldOf("xz_spread").orElse(4).forGetter(CropPatchConfiguration::xzSpread),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(CropPatchConfiguration::ySpread),
			PlacedFeature.CODEC.fieldOf("primary_feature").forGetter(CropPatchConfiguration::primaryFeature),
			PlacedFeature.CODEC.fieldOf("secondary_feature").forGetter(CropPatchConfiguration::secondaryFeature),
			PlacedFeature.CODEC.optionalFieldOf("floor_feature").forGetter(floorConfig -> Optional.ofNullable(floorConfig.floorFeature))
	).apply(config, (tries, xzSpread, yspread, primary, secondary, floor) -> floor.map(placedFeatureHolder -> new CropPatchConfiguration(tries, xzSpread, yspread, primary, secondary, placedFeatureHolder)).orElseGet(() -> new CropPatchConfiguration(tries, xzSpread, yspread, primary, secondary, null))));

}
