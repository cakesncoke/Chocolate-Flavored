package com.akiotsukino.chocolateflavored.block;

import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Does not extend CandleCakeBlock: its constructor would overwrite the vanilla candle-to-cake map. */
public final class ChocolateCandleCakeBlock extends AbstractCandleBlock {
    public static final MapCodec<ChocolateCandleCakeBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("candle").forGetter(block -> block.candle),
            propertiesCodec()).apply(instance, ChocolateCandleCakeBlock::new));
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 8, 15), Block.box(7, 8, 7, 9, 14, 9));
    private final Block candle;
    public ChocolateCandleCakeBlock(Block candle, Properties properties) {
        super(properties);
        this.candle = candle;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }
    @Override public MapCodec<ChocolateCandleCakeBlock> codec() { return CODEC; }
    @Override protected Iterable<Vec3> getParticleOffsets(BlockState state) { return java.util.List.of(new Vec3(.5, 1, .5)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).isSolid(); }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (stack.isEmpty() && state.getValue(LIT) && hit.getLocation().y - pos.getY() > .5) {
            extinguish(player, state, level, pos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        var result = ChocolateCakeBlock.eatChocolate(level, pos, player);
        if (result.consumesAction()) Block.popResource(level, pos, new ItemStack(candle));
        return result;
    }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) { return new ItemStack(ModItems.CHOCOLATE_CAKE.get()); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return 14; }
}
