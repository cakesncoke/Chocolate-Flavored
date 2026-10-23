package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.common.CommonHooks;

/** A source-water root, with a separately harvestable and regrowing panicle above water. */
public final class RiceCropBlock extends FieldCropBlock implements LiquidBlockContainer {
    public static final MapCodec<RiceCropBlock> CODEC = simpleCodec(RiceCropBlock::new);
    public static final BooleanProperty SUPPORTING = BooleanProperty.create("supporting");
    public RiceCropBlock(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(SUPPORTING, false)); }
    @Override public MapCodec<RiceCropBlock> codec() { return CODEC; }
    @Override public IntegerProperty getAgeProperty() { return BlockStateProperties.AGE_3; }
    @Override public int getMaxAge() { return 3; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BlockStateProperties.AGE_3, SUPPORTING); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(CropContent.CROP_SOIL); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return mayPlaceOn(level.getBlockState(pos.below()), level, pos.below())
                && level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos).isFull();
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var fluid = context.getLevel().getFluidState(context.getClickedPos());
        return fluid.is(FluidTags.WATER) && fluid.isFull() ? super.getStateForPlacement(context) : null;
    }
    @Override protected FluidState getFluidState(BlockState state) { return Fluids.WATER.getSource(false); }
    @Override public boolean canPlaceLiquid(LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) { return false; }
    @Override public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) { return false; }
    @Override protected boolean isRandomlyTicking(BlockState state) { return true; }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos.above(), 0) >= 6 && canAdvance(level, pos, state)
                && CommonHooks.canCropGrow(level, pos, state, random.nextInt(3) == 0)) {
            advance(level, pos, state);
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }
    private boolean canAdvance(LevelReader level, BlockPos pos, BlockState state) {
        return getAge(state) < 3 || level.isEmptyBlock(pos.above()) && CropContent.RICE_PANICLES.get().defaultBlockState().canSurvive(level, pos.above());
    }
    private void advance(ServerLevel level, BlockPos pos, BlockState state) {
        if (getAge(state) < 3) level.setBlock(pos, state.setValue(getAgeProperty(), getAge(state) + 1), 2);
        else if (canAdvance(level, pos, state)) level.setBlockAndUpdate(pos.above(), CropContent.RICE_PANICLES.get().defaultBlockState());
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        var above = level.getBlockState(pos.above());
        return canAdvance(level, pos, state) || above.is(CropContent.RICE_PANICLES.get()) && !CropContent.RICE_PANICLES.get().isMaxAge(above);
    }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        for (int i = 0, count = 1 + random.nextInt(4); i < count; i++) {
            state = level.getBlockState(pos);
            if (canAdvance(level, pos, state)) advance(level, pos, state);
            else {
                var top = level.getBlockState(pos.above());
                if (top.is(CropContent.RICE_PANICLES.get())) CropContent.RICE_PANICLES.get().performBonemeal(level, random, pos.above(), top);
                break;
            }
        }
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        if (!state.canSurvive(level, pos)) return Blocks.WATER.defaultBlockState();
        return state.setValue(SUPPORTING, level.getBlockState(pos.above()).is(CropContent.RICE_PANICLES.get()));
    }
}
