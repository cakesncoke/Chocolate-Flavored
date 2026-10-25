package com.akiotsukino.chocolateflavored.trim;

import com.akiotsukino.chocolateflavored.content.ModContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Frost Walker-style crust. Unlike ice, it ages without sunlight and always restores lava. */
public final class TrimBasalt extends Block {
    public static final MapCodec<TrimBasalt> CODEC = simpleCodec(TrimBasalt::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final DeferredBlock<TrimBasalt> BLOCK = ModContent.BLOCKS.registerBlock("temporary_trim_basalt", TrimBasalt::new,
            () -> BlockBehaviour.Properties.of().strength(.5f, 3600000).sound(SoundType.BASALT).noLootTable().pushReaction(PushReaction.BLOCK));

    public TrimBasalt(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LEVEL, 0).setValue(AGE, 0));
    }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BlockStateProperties.LEVEL, AGE); }
    @Override public void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        if (!previous.is(this)) level.scheduleTick(pos, this, Mth.nextInt(level.getRandom(), 60, 120));
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if ((random.nextInt(3) == 0 || fewerNeighbors(level, pos, 4)) && age(state, level, pos)) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                BlockState next = level.getBlockState(neighbor);
                if (next.is(this) && !age(next, level, neighbor)) level.scheduleTick(neighbor, this, Mth.nextInt(random, 20, 40));
            }
            return;
        }
        level.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
    }
    private boolean age(BlockState state, Level level, BlockPos pos) {
        int age = state.getValue(AGE);
        if (age < 3) { level.setBlock(pos, state.setValue(AGE, age + 1), 2); return false; }
        melt(state, level, pos);
        return true;
    }
    private void melt(BlockState state, Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, state.getValue(BlockStateProperties.LEVEL)), 3);
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean moved) {
        if (block == this && fewerNeighbors(level, pos, 2)) melt(state, level, pos);
        super.neighborChanged(state, level, pos, block, orientation, moved);
    }
    private boolean fewerNeighbors(BlockGetter level, BlockPos pos, int limit) {
        int count = 0;
        for (Direction direction : Direction.values()) if (level.getBlockState(pos.relative(direction)).is(this) && ++count >= limit) return false;
        return true;
    }
    @Override public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity entity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, entity, tool);
        melt(state, level, pos);
    }
    @Override protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) { return ItemStack.EMPTY; }
    private record Step(ServerLevel level, BlockPos pos) {}
    private static final java.util.Map<ServerPlayer, Step> STEPS = new java.util.WeakHashMap<>();
    public static void reset(ServerPlayer player) { STEPS.remove(player); }
    public static void register() {}
    public static void walk(ServerPlayer player) {
        // Same disk and source-fluid/air/ground requirements as native Frost Walker I.
        if (!player.onGround() || player.isPassenger()) { reset(player); return; }
        Step step = new Step(player.level(), player.blockPosition());
        if (step.equals(STEPS.put(player, step))) return;
        var level = player.level();
        BlockPos center = player.blockPosition().below();
        BlockState crust = BLOCK.get().defaultBlockState();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
            if (pos.distToCenterSqr(player.getX(), pos.getY() + .5, player.getZ()) >= 9
                    || !level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !level.getBlockState(pos.above()).isAir()) continue;
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.LAVA) || state.getValue(LiquidBlock.LEVEL) != 0 || !level.isUnobstructed(crust, pos, CollisionContext.empty())) continue;
            if (level.setBlock(pos, crust, 3)) level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, crust));
        }
    }
}
