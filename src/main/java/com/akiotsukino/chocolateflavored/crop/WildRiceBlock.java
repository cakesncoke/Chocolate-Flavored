package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.*;

public final class WildRiceBlock extends DoublePlantBlock implements SimpleWaterloggedBlock, BonemealableBlock {
    public static final MapCodec<WildRiceBlock> CODEC = simpleCodec(WildRiceBlock::new);
    public WildRiceBlock(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true)); }
    @Override public MapCodec<WildRiceBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(HALF, BlockStateProperties.WATERLOGGED); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(CropContent.CROP_SOIL) || state.is(BlockTags.SAND); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return super.canSurvive(state, level, pos);
        return super.canSurvive(state, level, pos) && state.getValue(BlockStateProperties.WATERLOGGED);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var fluid = level.getFluidState(pos);
        if (!fluid.is(FluidTags.WATER) || !fluid.isFull() || !level.isEmptyBlock(pos.above())) return null;
        var state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(BlockStateProperties.WATERLOGGED, true);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return random.nextFloat() < .3F; }
    @Override public void performBonemeal(net.minecraft.server.level.ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new net.minecraft.world.item.ItemStack(this));
    }
    @Override public boolean canPlaceLiquid(net.minecraft.world.entity.LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && SimpleWaterloggedBlock.super.canPlaceLiquid(user, level, pos, state, fluid);
    }
    @Override protected FluidState getFluidState(BlockState state) { return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState(); }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbour, random);
    }
}
