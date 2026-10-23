package com.akiotsukino.chocolateflavored.cake;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/** All eating, shapes, signals, support checks, and slice statistics come directly from vanilla. */
public final class FlavoredCakeBlock extends CakeBlock {
    public static final MapCodec<CakeBlock> CODEC = simpleCodec(FlavoredCakeBlock::new);
    public FlavoredCakeBlock(Properties properties) { super(properties); }
    @Override public MapCodec<CakeBlock> codec() { return CODEC; }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ItemTags.CANDLES) && state.getValue(BITES) == 0 && Block.byItem(stack.getItem()) instanceof CandleBlock candle) {
            var decorated = CakeContent.withCandle(this, candle);
            if (decorated != null) {
                var item = stack.getItem();
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1, 1);
                level.setBlockAndUpdate(pos, decorated);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.awardStat(Stats.ITEM_USED.get(item));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    public static InteractionResult eatSlice(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        return CakeBlock.eat(level, pos, state, player);
    }
}
