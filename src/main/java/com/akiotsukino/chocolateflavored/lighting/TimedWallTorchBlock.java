package com.akiotsukino.chocolateflavored.lighting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class TimedWallTorchBlock extends WallTorchBlock implements EntityBlock {
    private static final MapCodec<TimedWallTorchBlock> LIT_CODEC = simpleCodec(p -> new TimedWallTorchBlock(true, p));
    private static final MapCodec<TimedWallTorchBlock> UNLIT_CODEC = simpleCodec(p -> new TimedWallTorchBlock(false, p));
    private final boolean lit;
    public TimedWallTorchBlock(boolean lit, Properties properties) { super(ParticleTypes.FLAME, properties); this.lit = lit; }
    @Override public MapCodec<? extends WallTorchBlock> codec() { return lit ? LIT_CODEC : UNLIT_CODEC; }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (lit) super.animateTick(state, level, pos, random);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TorchTimerBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return TorchTimerBlockEntity.ticker(level, type, lit);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && lit && level.getBlockEntity(pos) instanceof TorchTimerBlockEntity timer) timer.reset();
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return lit ? InteractionResult.PASS : LightingIgnition.useIgniter(stack, level, pos, player, hand, hit);
    }
}
