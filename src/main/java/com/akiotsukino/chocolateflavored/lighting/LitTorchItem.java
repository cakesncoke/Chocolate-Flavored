package com.akiotsukino.chocolateflavored.lighting;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Fuel is a persistent component, not repairable tool damage. Its bar uses the vanilla durability presentation. */
public final class LitTorchItem extends StandingAndWallBlockItem {
    public LitTorchItem(Properties properties) {
        super(LightingContent.LIT_TORCH.get(), LightingContent.LIT_WALL_TORCH.get(), Direction.DOWN,
                properties.stacksTo(1).component(LightingContent.TORCH_FUEL.get(), (int)TorchTimerBlockEntity.BURN_TICKS));
    }
    public static int remaining(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(LightingContent.TORCH_FUEL.get(), (int)TorchTimerBlockEntity.BURN_TICKS), 0, (int)TorchTimerBlockEntity.BURN_TICKS);
    }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13.0F * remaining(stack) / TorchTimerBlockEntity.BURN_TICKS); }
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb(Math.max(0.0F, (float)remaining(stack) / TorchTimerBlockEntity.BURN_TICKS) / 3.0F, 1.0F, 1.0F); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        int seconds = (remaining(stack) + 19) / 20;
        tooltip.accept(Component.translatable("tooltip.chocolateflavored.torch_time", String.format(java.util.Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)).withStyle(ChatFormatting.GRAY));
    }
    @Override protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null || remaining(context.getItemInHand()) > 0) return state;
        return state.is(LightingContent.LIT_WALL_TORCH.get())
                ? LightingContent.UNLIT_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, state.getValue(WallTorchBlock.FACING))
                : LightingContent.UNLIT_TORCH.get().defaultBlockState();
    }
}
