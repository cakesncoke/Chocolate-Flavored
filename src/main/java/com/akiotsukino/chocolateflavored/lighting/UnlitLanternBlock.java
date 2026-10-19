package com.akiotsukino.chocolateflavored.lighting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class UnlitLanternBlock extends LanternBlock {
    private static final MapCodec<UnlitLanternBlock> CODEC = simpleCodec(UnlitLanternBlock::new);
    public UnlitLanternBlock(Properties properties) { super(properties); }
    @Override public MapCodec<? extends LanternBlock> codec() { return CODEC; }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return LightingIgnition.useIgniter(stack, level, pos, player, hand, hit);
    }
}
