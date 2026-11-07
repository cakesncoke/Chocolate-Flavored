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
import com.akiotsukino.chocolateflavored.cooking.CookingContent;
import com.akiotsukino.chocolateflavored.lighting.LightingContent;

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
        boolean campfire = state.getBlock() instanceof CampfireBlock;
        boolean vanillaTorch = state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH);
        boolean timedTorch = state.is(LightingContent.LIT_TORCH.get()) || state.is(LightingContent.LIT_WALL_TORCH.get());
        boolean kiln = state.is(CookingContent.MUD_KILN.get());
        if (!campfire && !vanillaTorch && !timedTorch && !kiln) return InteractionResult.PASS;
        if (!LightingIgnition.permitted(player, level, pos, context.getClickedFace(), stack)) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // One unlit torch produces one individually timed lit torch item.
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(context.getHand(), new ItemStack(LightingContent.LIT_TORCH_ITEM.get()));
        } else {
            var lit = new ItemStack(LightingContent.LIT_TORCH_ITEM.get());
            int slot = player.getInventory().getFreeSlot();
            if (slot < 0) player.drop(lit, false, Prediction.SERVER_ONLY);
            else player.getInventory().setItem(slot, lit);
        }
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
        return InteractionResult.SUCCESS;
    }
}
