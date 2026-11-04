package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Data-driven, bounded clusters for soybean and chili; no Extra Delight classes are linked. */
public record SmallCropPatchFeature(SmallCropPatchFeature.Config config) implements Feature {
    public record Config(BlockState state, int minCount, int maxCount, int xzSpread, int ySpread, int tries) {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(b -> b.group(
            BlockState.CODEC.fieldOf("state").forGetter(Config::state),
            Codec.intRange(1, 64).fieldOf("min_count").forGetter(Config::minCount),
            Codec.intRange(1, 64).fieldOf("max_count").forGetter(Config::maxCount),
            Codec.intRange(0, 64).fieldOf("xz_spread").forGetter(Config::xzSpread),
            Codec.intRange(0, 64).fieldOf("y_spread").forGetter(Config::ySpread),
            Codec.intRange(1, 1024).fieldOf("tries").forGetter(Config::tries)
        ).apply(b, Config::new));
    }
    public static final com.mojang.serialization.MapCodec<SmallCropPatchFeature> CODEC = SmallCropPatchFeature.Config.CODEC.fieldOf("config").xmap(SmallCropPatchFeature::new, SmallCropPatchFeature::config);
    @Override public com.mojang.serialization.MapCodec<SmallCropPatchFeature> codec() { return CODEC; }
    @Override public boolean place(WorldGenLevel level, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        var c = config;
        int remaining = random.nextIntBetweenInclusive(c.minCount(), Math.max(c.minCount(), c.maxCount()));
        int placed = 0;
        for (int i = 0; i < c.tries() && placed < remaining; i++) {
            BlockPos p = origin.offset(random.nextInt(c.xzSpread() + 1) - random.nextInt(c.xzSpread() + 1),
                    random.nextInt(c.ySpread() + 1) - random.nextInt(c.ySpread() + 1), random.nextInt(c.xzSpread() + 1) - random.nextInt(c.xzSpread() + 1));
            if ((level.isEmptyBlock(p) || level.getBlockState(p).is(BlockTags.SNOW)) && c.state().canSurvive(level, p)) {
                level.setBlock(p, c.state(), 2); placed++;
            }
        }
        return placed > 0;
    }
}
