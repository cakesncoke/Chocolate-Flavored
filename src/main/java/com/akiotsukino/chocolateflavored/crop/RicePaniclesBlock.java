package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class RicePaniclesBlock extends FieldCropBlock {
    public static final MapCodec<RicePaniclesBlock> CODEC = simpleCodec(RicePaniclesBlock::new);
    public RicePaniclesBlock(Properties p) { super(p); }
    @Override public MapCodec<RicePaniclesBlock> codec() { return CODEC; }
    @Override public IntegerProperty getAgeProperty() { return BlockStateProperties.AGE_3; }
    @Override public int getMaxAge() { return 3; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BlockStateProperties.AGE_3); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(CropContent.RICE_CROP.get()); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) >= 8 && mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }
    @Override protected int getBonemealAgeIncrease(Level level) { return 1; }
}
