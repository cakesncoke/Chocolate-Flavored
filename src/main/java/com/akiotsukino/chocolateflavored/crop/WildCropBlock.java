package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WildCropBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<WildCropBlock> CODEC = simpleCodec(WildCropBlock::new);
    public WildCropBlock(Properties p) { super(p); }
    @Override public MapCodec<WildCropBlock> codec() { return CODEC; }
    @Override public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) { return false; }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(CropContent.CROP_SOIL) || state.is(BlockTags.SAND); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Block.column(12, 0, 13); }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return random.nextFloat() < .8F; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int nearby = 0;
        for (var p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) if (level.getBlockState(p).is(this) && ++nearby >= 10) return;
        for (int i = 0; i < 4; i++) {
            var p = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            if (level.isEmptyBlock(p) && state.canSurvive(level, p)) { level.setBlockAndUpdate(p, state); return; }
        }
    }
}
