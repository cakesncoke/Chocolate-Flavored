package com.akiotsukino.chocolateflavored.gameplay;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.TriState;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Preserve vanilla outputs and material consumption; charge only for actual enchantment gains. */
public final class AnvilRules {
    public static void update(AnvilUpdateEvent event) {
        var output = event.getOutput();
        int cost = 0;
        if (!output.isEmpty()) {
            var before = EnchantmentHelper.getEnchantmentsForCrafting(event.getLeft());
            var after = EnchantmentHelper.getEnchantmentsForCrafting(output);
            for (var entry : after.entrySet()) {
                if (entry.getIntValue() > before.getLevel(entry.getKey())) {
                    cost = 3;
                    break;
                }
            }
        }
        event.setXpCost(cost);
    }

    public static void repairBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var pos = event.getPos();
        var stack = player.getItemInHand(event.getHand());
        if (!stack.is(Items.IRON_INGOT) || event.getUseBlock() == TriState.FALSE || event.getUseItem() == TriState.FALSE) return;
        var state = level.getBlockState(pos);
        Block repaired;
        if (state.is(Blocks.DAMAGED_ANVIL)) repaired = Blocks.CHIPPED_ANVIL;
        else if (state.is(Blocks.CHIPPED_ANVIL)) repaired = Blocks.ANVIL;
        else return;
        if (player.isSpectator() || !player.mayUseItemAt(pos, event.getFace(), stack)
                || !level.mayInteract(player, pos) || !player.isWithinBlockInteractionRange(pos, 0.0)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide()) return;
        var replacement = repaired.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
        if (!level.setBlock(pos, replacement, Block.UPDATE_ALL)) return;
        stack.consume(1, player);
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, replacement));
    }
    private AnvilRules() {}
}
