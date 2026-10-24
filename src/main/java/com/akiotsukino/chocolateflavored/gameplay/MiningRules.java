package com.akiotsukino.chocolateflavored.gameplay;

import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.content.ModTags;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public final class MiningRules {
    public record Conversion(BlockState replacement, ItemStack drop, boolean vanillaLoot) {}

    public static Conversion conversion(BlockState state, ItemStack tool) {
        if (tool.is(ModTags.HAMMERS)) {
            if (state.is(ModTags.FLINT_COBBLE)) return new Conversion(Blocks.AIR.defaultBlockState(), new ItemStack(ModContent.STONE_PEBBLE.get()), false);
            if (state.is(ModTags.FLINT_STONE)) return new Conversion(Blocks.COBBLESTONE.defaultBlockState(), new ItemStack(ModContent.STONE_PEBBLE.get()), false);
            if (state.is(Blocks.COAL_ORE)) return new Conversion(Blocks.COBBLESTONE.defaultBlockState(), new ItemStack(ModContent.COAL_DUST.get()), false);
            if (state.is(Blocks.COPPER_ORE)) return new Conversion(Blocks.COBBLESTONE.defaultBlockState(), new ItemStack(ModContent.COPPER_ORE_PILE.get()), false);
        }
        if (tool.is(Items.COPPER_PICKAXE) && state.is(Blocks.IRON_ORE)) {
            return new Conversion(Blocks.COBBLESTONE.defaultBlockState(), new ItemStack(ModContent.IRON_ORE_PILE.get()), false);
        }
        if (tool.is(Items.IRON_PICKAXE)) {
            if (state.is(Blocks.DEEPSLATE)) return new Conversion(Blocks.COBBLED_DEEPSLATE.defaultBlockState(), new ItemStack(ModContent.DEEPSLATE_PEBBLE.get()), false);
            if (state.is(ModTags.DEEPSLATE_ORES)) return new Conversion(Blocks.COBBLED_DEEPSLATE.defaultBlockState(), ItemStack.EMPTY, true);
        }
        return null;
    }

    public static void breakBlock(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative() || player.isSpectator() || event.isCanceled()) return;
        ItemStack tool = player.getMainHandItem();
        Conversion change = conversion(event.getState(), tool);
        if (change == null) return;
        // Run after protection listeners. Cancel the normal removal on both sides;
        // the authoritative server replaces the block immediately, without a delayed task.
        event.setCanceled(true);
        event.setNotifyClient(true);
        if (!(player.level() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!level.setBlock(pos, change.replacement(), Block.UPDATE_ALL)) return;
        if (change.vanillaLoot()) {
            // Preserve the ore's normal XP and enchantment behavior as well as its loot table.
            Block.dropResources(state, level, pos, null, player, tool.copy());
        } else {
            Block.popResource(level, pos, change.drop());
        }
        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        player.causeFoodExhaustion(0.005F);
        level.levelEvent(2001, pos, Block.getId(state));
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
    }

    public static void speed(PlayerEvent.BreakSpeed event) {
        if (event.getState().is(BlockTags.LOGS) && !event.getEntity().getMainHandItem().is(ItemTags.AXES)) {
            event.setNewSpeed(event.getNewSpeed() * 0.2F);
        }
    }
    public static void harvest(PlayerEvent.HarvestCheck event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        if (tool.is(Items.COPPER_PICKAXE) && event.getTargetBlock().is(ModTags.DEEPSLATE)) event.setCanHarvest(false);
    }
    private MiningRules() {}
}
