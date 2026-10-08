package com.akiotsukino.chocolateflavored.event;

import com.akiotsukino.chocolateflavored.registry.ModItems;
import com.akiotsukino.chocolateflavored.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class PrimitiveMining {
    private PrimitiveMining() {}

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer player)
                || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
                || !player.getMainHandItem().is(Items.FLINT)) return;
        var state = event.getState();
        boolean stone = state.is(Blocks.STONE) || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE);
        if (!stone && !state.is(Blocks.COBBLESTONE)) return;
        BlockPos pos = event.getPos();
        // Run after other break listeners, respect protection cancellation, then replace normal harvesting.
        event.setCanceled(true);
        if (!level.setBlock(pos, stone ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3)) return;
        level.levelEvent(2001, pos, Block.getId(state));
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
        Block.popResource(level, pos, new ItemStack(ModItems.STONE_PEBBLE.get()));
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        player.causeFoodExhaustion(0.005F);
        ItemStack before = player.getMainHandItem().copy();
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        if (player.getMainHandItem().isEmpty()) {
            net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, before, net.minecraft.world.InteractionHand.MAIN_HAND);
        }
    }

    public static void onDrops(BlockDropsEvent event) {
        if (event.isCanceled() || !(event.getBreaker() instanceof Player player) || player.isCreative() || player.isSpectator()) return;
        var state = event.getState();
        var random = event.getLevel().random;
        if (event.getTool().isEmpty()) {
            Item pile;
            float flintChance = 0.0F;
            if (state.is(ModTags.Blocks.HAND_DUG_DIRT)) {
                pile = ModItems.DIRT_PILE.get(); flintChance = 0.20F;
            } else if (state.is(Blocks.SAND)) pile = ModItems.SAND_PILE.get();
            else if (state.is(Blocks.RED_SAND)) pile = ModItems.RED_SAND_PILE.get();
            else if (state.is(Blocks.GRAVEL)) { pile = ModItems.GRAVEL_PILE.get(); flintChance = 0.50F; }
            else return;
            event.getDrops().clear();
            event.setDroppedExperience(0);
            addDrop(event, new ItemStack(pile, 3));
            if (flintChance > 0 && random.nextFloat() < flintChance) addDrop(event, new ItemStack(Items.FLINT));
        } else if (event.getTool().is(ModItems.FLINT_KNIFE.get())) {
            // Double plants invoke drops once during playerWillDestroy; do not add a second harvest handler.
            if ((state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                    || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)) && random.nextFloat() < 0.33F) {
                addDrop(event, new ItemStack(ModItems.PLANT_FIBER.get()));
            }
            if (state.is(BlockTags.LEAVES) && random.nextFloat() < 0.25F) addDrop(event, new ItemStack(Items.STICK));
        }
    }

    private static void addDrop(BlockDropsEvent event, ItemStack stack) {
        BlockPos pos = event.getPos();
        ItemEntity entity = new ItemEntity(event.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setDefaultPickUpDelay();
        event.getDrops().add(entity);
    }
}
