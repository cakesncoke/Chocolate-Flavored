package com.akiotsukino.chocolateflavored.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class WildRiceFeature extends Feature<SmallCropPatchFeature.Config> {
    public WildRiceFeature() { super(SmallCropPatchFeature.Config.CODEC); }
    @Override public boolean place(FeaturePlaceContext<SmallCropPatchFeature.Config> context) {
        var c = context.config(); var level = context.level(); var random = context.random();
        var origin = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, context.origin());
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
