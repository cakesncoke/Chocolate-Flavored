package com.akiotsukino.chocolateflavored.cake;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

/** Vanilla candle-cake interactions with a flavored base; never overwrites vanilla's candle lookup. */
public final class FlavoredCandleCakeBlock extends AbstractCandleBlock {
    public static final MapCodec<FlavoredCandleCakeBlock> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("cake").forGetter((FlavoredCandleCakeBlock c) -> c.cake),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("candle").forGetter((FlavoredCandleCakeBlock c) -> c.candle),
            propertiesCodec()).apply(b, FlavoredCandleCakeBlock::new));
    private static final VoxelShape SHAPE = Shapes.or(Block.column(2, 8, 14), Block.column(14, 0, 8));
    private static final List<Vec3> PARTICLES = List.of(new Vec3(.5, 1, .5));
    private final Block cake;
    private final Block candle;
    public FlavoredCandleCakeBlock(Block cake, Block candle, Properties properties) {
        super(properties);
        if (!(cake instanceof FlavoredCakeBlock) || !(candle instanceof CandleBlock)) throw new IllegalArgumentException("Expected flavored cake and candle");
        this.cake = cake; this.candle = candle;
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }
    @Override public MapCodec<FlavoredCandleCakeBlock> codec() { return CODEC; }
    @Override protected Iterable<Vec3> getParticleOffsets(BlockState state) { return PARTICLES; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) return InteractionResult.PASS;
        if (hit.getLocation().y - pos.getY() > .5 && stack.isEmpty() && state.getValue(LIT)) {
            extinguish(player, state, level, pos);
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        var result = FlavoredCakeBlock.eatSlice(level, pos, cake.defaultBlockState(), player);
        if (result.consumesAction()) dropResources(state, level, pos);
        return result;
    }
    @Override protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) { return new ItemStack(cake); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(LIT); }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).isSolid(); }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbour, random);
    }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) { return CakeBlock.FULL_CAKE_SIGNAL; }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected boolean isPathfindable(BlockState state, PathComputationType type) { return false; }
}
