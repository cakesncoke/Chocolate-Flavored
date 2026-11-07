package com.akiotsukino.chocolateflavored.lighting;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;

final class UnlitTorchItem extends StandingAndWallBlockItem {
    UnlitTorchItem(Properties properties) {
        super(LightingContent.UNLIT_TORCH.get(), LightingContent.UNLIT_WALL_TORCH.get(), Direction.DOWN, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        boolean batch = player.isShiftKeyDown();
        boolean campfire = state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT);
        boolean vanillaTorch = state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH);
        boolean timedTorch = state.is(LightingContent.LIT_TORCH.get()) || state.is(LightingContent.LIT_WALL_TORCH.get());
        if (!campfire && !vanillaTorch && !(batch && timedTorch)) return InteractionResult.PASS;
        if (!LightingIgnition.permitted(player, level, pos, context.getClickedFace(), stack)) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        int count = batch ? stack.getCount() : 1;
        stack.shrink(count);
        if (stack.isEmpty()) {
            player.setItemInHand(context.getHand(), new ItemStack(LightingContent.LIT_TORCH_ITEM.get()));
            count--;
        }
        // Lit torches carry individual fuel timers and use one inventory slot apiece.
        for (int i = 0; i < count; i++) {
            var lit = new ItemStack(LightingContent.LIT_TORCH_ITEM.get());
            int slot = player.getInventory().getFreeSlot();
            if (slot < 0) player.drop(lit, false, Prediction.SERVER_ONLY);
            else player.getInventory().setItem(slot, lit);
        }
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
        return InteractionResult.SUCCESS;
    }
}
