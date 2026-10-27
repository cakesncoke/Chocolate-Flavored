package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Native 26.1.2 farmland, light, hydration, crop events, and bonemeal behavior. */
public class FieldCropBlock extends CropBlock {
    public static final MapCodec<FieldCropBlock> CODEC = simpleCodec(FieldCropBlock::new);
    public FieldCropBlock(Properties properties) { super(properties); }
    @Override public MapCodec<? extends CropBlock> codec() { return CODEC; }
    @Override protected ItemLike getBaseSeedId() { return CropContent.seedFor(this); }
    private boolean isSlowCrop() { return this == CropContent.SOYBEAN_CROP.get() || this == CropContent.CHILI_CROP.get(); }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isSlowCrop() || random.nextInt(3) != 0) super.randomTick(state, level, pos, random);
    }
    @Override protected int getBonemealAgeIncrease(Level level) {
        return isSlowCrop() ? Math.max(1, super.getBonemealAgeIncrease(level) / 3) : super.getBonemealAgeIncrease(level);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int maxHeight = this == CropContent.CABBAGES.get() ? 10 : this == CropContent.ONIONS.get() ? 9 : 16;
        return Block.column(16, 0, 2 + (maxHeight - 2) * getAge(state) / (double)getMaxAge());
    }
    public static final class Chili extends FieldCropBlock {
        public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 6);
        public static final MapCodec<Chili> CODEC = simpleCodec(Chili::new);
        public Chili(Properties p) { super(p); }
        @Override public MapCodec<Chili> codec() { return CODEC; }
        @Override public IntegerProperty getAgeProperty() { return AGE; }
        @Override public int getMaxAge() { return 6; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AGE); }
    }
    public static final class Cotton extends FieldCropBlock {
        public static final MapCodec<Cotton> CODEC = simpleCodec(Cotton::new);
        public Cotton(Properties p) { super(p); }
        @Override public MapCodec<Cotton> codec() { return CODEC; }
        @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            int age = getAge(state);
            return Block.column(16, 0, age < 2 ? 5 : age < 4 ? 9 : 14);
        }
        @Override public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
            if (!isMaxAge(state)) return super.useWithoutItem(state, level, pos, player, hit);
            if (level instanceof ServerLevel server) {
                popResource(level, pos, new net.minecraft.world.item.ItemStack(CropContent.COTTON_BOLL.get(), 1 + server.random.nextInt(2)));
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CROP_BREAK, net.minecraft.sounds.SoundSource.BLOCKS,
                        1, .8F + server.random.nextFloat() * .4F);
                level.setBlock(pos, getStateForAge(5), 2);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
    }
    public static final class Coffee extends FieldCropBlock {
        public static final MapCodec<Coffee> CODEC = simpleCodec(Coffee::new);
        public Coffee(Properties p) { super(p); }
        @Override public MapCodec<Coffee> codec() { return CODEC; }
        @Override public IntegerProperty getAgeProperty() { return BlockStateProperties.AGE_5; }
        @Override public int getMaxAge() { return 5; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BlockStateProperties.AGE_5); }
    }
}
