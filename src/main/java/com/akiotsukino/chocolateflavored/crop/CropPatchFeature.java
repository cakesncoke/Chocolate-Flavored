// Adapted from vectorwing/Farmer's Delight, MIT. See THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record CropPatchFeature(CropPatchConfiguration config) implements Feature
{
    public static final com.mojang.serialization.MapCodec<CropPatchFeature> CODEC = CropPatchConfiguration.CODEC.fieldOf("config").xmap(CropPatchFeature::new, CropPatchFeature::config);
    @Override public com.mojang.serialization.MapCodec<CropPatchFeature> codec() { return CODEC; }

    @Override public java.util.stream.Stream<Holder<Feature>> getSubFeatures() { return java.util.stream.Stream.of(config.primaryFeature(), config.secondaryFeature(), config.floorFeature()).filter(java.util.Objects::nonNull).flatMap(f -> f.value().getFeatures()); }
	@Override
	public boolean place(WorldGenLevel level, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        var placer = new net.minecraft.world.level.levelgen.placement.FeaturePlacer(level, chunkGenerator);

		int i = 0;
		int tries = config.tries();
		int xzSpread = config.xzSpread() + 1;
		int ySpread = config.ySpread() + 1;

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		Holder<PlacedFeature> floorFeature = config.floorFeature();
		if (floorFeature != null) {
			for (int j = 0; j < tries; ++j) {
				mutablePos.setWithOffset(origin, random.nextInt(xzSpread) - random.nextInt(xzSpread), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(xzSpread) - random.nextInt(xzSpread));
				if (placer.place(config.floorFeature().value(), random, mutablePos)) {
					++i;
				}
			}
		}

		for (int k = 0; k < tries; ++k) {
			int shorterXZ = xzSpread - 2;
			mutablePos.setWithOffset(origin, random.nextInt(shorterXZ) - random.nextInt(shorterXZ), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(shorterXZ) - random.nextInt(shorterXZ));
			if (placer.place(config.primaryFeature().value(), random, mutablePos)) {
				++i;
			}
		}

		for (int l = 0; l < tries; ++l) {
			mutablePos.setWithOffset(origin, random.nextInt(xzSpread) - random.nextInt(xzSpread), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(xzSpread) - random.nextInt(xzSpread));
			if (placer.place(config.secondaryFeature().value(), random, mutablePos)) {
				++i;
			}
		}

		return i > 0;
	}
}
