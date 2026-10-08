package com.akiotsukino.chocolateflavored.block;

import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class ChocolateCakeBlock extends CakeBlock {
    public static final MapCodec<CakeBlock> CODEC = simpleCodec(ChocolateCakeBlock::new);
    public ChocolateCakeBlock(Properties properties) { super(properties); }
    @Override public MapCodec<CakeBlock> codec() { return CODEC; }

    public static net.minecraft.world.InteractionResult eatChocolate(net.minecraft.world.level.LevelAccessor level, BlockPos pos, Player player) {
        return CakeBlock.eat(level, pos, ModBlocks.CHOCOLATE_CAKE.get().defaultBlockState(), player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(BITES) == 0 && stack.is(ItemTags.CANDLES)
                && stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CandleBlock candle) {
            var chocolate = ModBlocks.CHOCOLATE_CANDLES.get(candle);
            if (chocolate != null) {
                stack.consume(1, player);
                level.setBlockAndUpdate(pos, chocolate.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1, 1);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.awardStat(Stats.ITEM_USED.get(item));
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
