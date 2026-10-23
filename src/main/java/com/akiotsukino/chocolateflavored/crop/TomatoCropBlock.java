package com.akiotsukino.chocolateflavored.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;

/** Four seedling stages followed by four fruiting stages; picking preserves the mature bush. */
public final class TomatoCropBlock extends FieldCropBlock {
    public static final MapCodec<TomatoCropBlock> CODEC = simpleCodec(TomatoCropBlock::new);
    public TomatoCropBlock(Properties properties) { super(properties); }
    @Override public MapCodec<? extends CropBlock> codec() { return CODEC; }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (getAge(state) < 4) { super.randomTick(state, level, pos, random); return; }
        if (getAge(state) < 7 && level.getRawBrightness(pos, 0) >= 9
                && CommonHooks.canCropGrow(level, pos, state, random.nextInt(6) == 0)) {
            level.setBlock(pos, getStateForAge(getAge(state) + 1), 2);
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }
    @Override protected int getBonemealAgeIncrease(Level level) { return Math.max(1, super.getBonemealAgeIncrease(level) / 2); }
    @Override public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!isMaxAge(state)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            popResource(level, pos, new ItemStack(CropContent.TOMATO.get(), 1 + level.random.nextInt(2)));
            level.setBlock(pos, getStateForAge(4), 2);
            server.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1, 1);
            server.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        }
        return InteractionResult.SUCCESS;
    }
}
