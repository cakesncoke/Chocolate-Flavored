package com.akiotsukino.chocolateflavored.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public record WildRiceFeature(SmallCropPatchFeature.Config config) implements Feature {
    public static final com.mojang.serialization.MapCodec<WildRiceFeature> CODEC = SmallCropPatchFeature.Config.CODEC.fieldOf("config").xmap(WildRiceFeature::new, WildRiceFeature::config);
    @Override public com.mojang.serialization.MapCodec<WildRiceFeature> codec() { return CODEC; }
    @Override public boolean place(WorldGenLevel level, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        var c = config;
        origin = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, origin);
        int placed = 0;
        for (int i = 0; i < c.tries(); i++) {
            BlockPos p = origin.offset(random.nextInt(c.xzSpread() + 1) - random.nextInt(c.xzSpread() + 1),
                random.nextInt(c.ySpread() + 1) - random.nextInt(c.ySpread() + 1), random.nextInt(c.xzSpread() + 1) - random.nextInt(c.xzSpread() + 1));
            if (level.getBlockState(p).is(Blocks.WATER) && level.getFluidState(p).isFull() && level.isEmptyBlock(p.above()) && c.state().canSurvive(level, p)) {
                DoublePlantBlock.placeAt(level, c.state(), p, 2); placed++;
            }
        }
        return placed > 0;
    }
}
