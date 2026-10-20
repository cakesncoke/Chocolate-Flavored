package com.akiotsukino.chocolateflavored.cooking;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class CookingPotBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public enum Support implements StringRepresentable {
        NONE("none"), TRAY("tray"), HANDLE("handle");
        private final String name; Support(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
    public static final MapCodec<CookingPotBlock> CODEC = simpleCodec(CookingPotBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Support> SUPPORT = EnumProperty.create("support", Support.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);
    public CookingPotBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SUPPORT, Support.NONE).setValue(WATERLOGGED, false)); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, SUPPORT, WATERLOGGED); }
    private static Support tray(LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).is(CookingPotBlockEntity.TRAY_HEAT_SOURCES) ? Support.TRAY : Support.NONE; }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER))
                .setValue(SUPPORT, context.getClickedFace() == Direction.DOWN ? Support.HANDLE : tray(context.getLevel(), context.getClickedPos()));
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return direction.getAxis() == Direction.Axis.Y && state.getValue(SUPPORT) != Support.HANDLE ? state.setValue(SUPPORT, tray(level, pos)) : state;
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return state.getValue(SUPPORT) == Support.TRAY ? Shapes.or(SHAPE, Block.box(0, -1, 0, 16, 0, 16)) : SHAPE; }
    @Override protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
            if (!pot.canOpen(player)) return InteractionResult.TRY_WITH_EMPTY_HAND;
            if (!level.isClientSide()) {
                var serving = pot.serve(player, held);
                if (!serving.isEmpty()) {
                    if (!player.getInventory().add(serving)) player.drop(serving, false);
                    level.playSound(null, pos, CookingContent.TAKE_PORTION.get(), SoundSource.BLOCKS, 1, 1);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
            if (player.isShiftKeyDown()) {
                level.setBlockAndUpdate(pos, state.setValue(SUPPORT, state.getValue(SUPPORT) == Support.HANDLE ? tray(level, pos) : Support.HANDLE));
                level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1);
            } else player.openMenu(pot);
        }
        return InteractionResult.SUCCESS;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CookingPotBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel) || type != CookingContent.POT_ENTITY.get()) return null;
        return (world, pos, block, entity) -> { if (entity instanceof CookingPotBlockEntity pot) CookingPotBlockEntity.tick((ServerLevel)world, pos, block, pot); };
    }
    @Override public FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) { return level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot ? AbstractContainerMenu.getRedstoneSignalFromContainer(pot) : 0; }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.isHeated()) {
            if (random.nextInt(10) == 0) level.playLocalSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, pot.meal().isEmpty() ? CookingContent.POT_BOIL.get() : CookingContent.POT_SOUP.get(), SoundSource.BLOCKS, 0.5F, 0.9F + random.nextFloat() * 0.2F, false);
            if (random.nextInt(5) == 0) level.addParticle(CookingContent.STEAM.get(), pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.7, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.015, 0);
        }
    }
}
