package com.akiotsukino.chocolateflavored.horn;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** Vanilla instruments supply playback, animation, duration, statistics and cooldown. */
public final class CopperHornItem extends InstrumentItem {
    private static final String LINK = "chocolateflavored:horn_target";
    public CopperHornItem(Properties properties) { super(properties); }

    public static UUID target(ItemStack stack) {
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr(LINK, "");
        try { return UUID.fromString(id); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public static void link(ItemStack stack, UUID target) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(LINK, target.toString()));
    }

    public static void link(ItemStack stack, net.minecraft.world.entity.Entity target) {
        link(stack, target.getUUID());
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putString("chocolateflavored:horn_target_name", target.getName().getString()));
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip,
            net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (target(stack) == null) return;
        String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .getStringOr("chocolateflavored:horn_target_name", "");
        var companion = name.isEmpty() ? net.minecraft.network.chat.Component.translatable("tooltip.chocolateflavored.horn_companion")
                : net.minecraft.network.chat.Component.literal(name);
        tooltip.accept(net.minecraft.network.chat.Component.translatable("tooltip.chocolateflavored.horn_call", companion)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override public boolean isFoil(ItemStack stack) { return target(stack) != null || super.isFoil(stack); }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        // Also enforce the native cooldown for direct callers outside the interaction manager.
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        var result = super.use(level, player, hand);
        if (result.consumesAction() && player instanceof ServerPlayer serverPlayer && target(stack) != null)
            HornRecall.request(serverPlayer, target(stack));
        return result;
    }
}
