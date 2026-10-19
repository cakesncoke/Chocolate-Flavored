package com.akiotsukino.chocolateflavored.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TorchTimerBlockEntity extends BlockEntity {
    public static final long BURN_TICKS = 20L * 60 * 20;
    private long burnUntil;
    public TorchTimerBlockEntity(BlockPos pos, BlockState state) { super(LightingContent.TORCH_TIMER.get(), pos, state); }
    public long burnUntil() { return burnUntil; }
    public void reset() {
        if (level != null) { burnUntil = level.getGameTime() + BURN_TICKS; setChanged(); }
    }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); burnUntil = input.getLongOr("BurnUntil", 0L); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putLong("BurnUntil", burnUntil); }
    public static boolean isLit(BlockState state) {
        return state.is(LightingContent.LIT_TORCH.get()) || state.is(LightingContent.LIT_WALL_TORCH.get());
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TorchTimerBlockEntity timer) {
        if (!(level instanceof ServerLevel server) || !isLit(state)) return;
        if (timer.burnUntil == 0L) timer.reset();
        if (level.getGameTime() < timer.burnUntil) return;
        BlockState unlit = state.is(LightingContent.LIT_WALL_TORCH.get())
                ? LightingContent.UNLIT_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING))
                : LightingContent.UNLIT_TORCH.get().defaultBlockState();
        server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + server.random.nextFloat() * 0.8F);
        server.setBlock(pos, unlit, Block.UPDATE_ALL);
        server.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(unlit));
    }
    static <T extends BlockEntity> BlockEntityTicker<T> ticker(Level level, BlockEntityType<T> type, boolean lit) {
        if (level.isClientSide() || !lit || type != LightingContent.TORCH_TIMER.get()) return null;
        return (world, pos, state, entity) -> { if (entity instanceof TorchTimerBlockEntity timer) serverTick(world, pos, state, timer); };
    }
}
