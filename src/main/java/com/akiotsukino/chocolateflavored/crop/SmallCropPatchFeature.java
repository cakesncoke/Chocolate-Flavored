package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Data-driven, bounded clusters for soybean and chili; no Extra Delight classes are linked. */
public final class SmallCropPatchFeature extends Feature<SmallCropPatchFeature.Config> {
    public record Config(BlockState state, int minCount, int maxCount, int xzSpread, int ySpread, int tries) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(b -> b.group(
            BlockState.CODEC.fieldOf("state").forGetter(Config::state),
            Codec.intRange(1, 64).fieldOf("min_count").forGetter(Config::minCount),
            Codec.intRange(1, 64).fieldOf("max_count").forGetter(Config::maxCount),
            Codec.intRange(0, 64).fieldOf("xz_spread").forGetter(Config::xzSpread),
            Codec.intRange(0, 64).fieldOf("y_spread").forGetter(Config::ySpread),
            Codec.intRange(1, 1024).fieldOf("tries").forGetter(Config::tries)
        ).apply(b, Config::new));
    }
    public SmallCropPatchFeature() { super(Config.CODEC); }
    @Override public boolean place(FeaturePlaceContext<Config> context) {
        var c = context.config(); var random = context.random(); var level = context.level();
        int remaining = random.nextIntBetweenInclusive(c.minCount(), Math.max(c.minCount(), c.maxCount()));
        int placed = 0;
        for (int i = 0; i < c.tries() && placed < remaining; i++) {
            BlockPos p = context.origin().offset(random.nextInt(c.xzSpread() + 1) - random.nextInt(c.xzSpread() + 1),
                    random.nextInt(c.ySpread() + 1) - random.nextInt(c.ySpread() + 1), random.nextInt(c.xzSpread() + 1) - random.nextInt(c.xzSpread() + 1));
            if ((level.isEmptyBlock(p) || level.getBlockState(p).is(BlockTags.SNOW)) && c.state().canSurvive(level, p)) {
                level.setBlock(p, c.state(), 2); placed++;
            }
        }
        return placed > 0;
    }
}
