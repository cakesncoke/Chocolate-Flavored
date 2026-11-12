package com.akiotsukino.chocolateflavored;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Stone/heavy hammer block conversion and knife plant drops. */
public final class ToolBehavior {
    /** What a hammer turns a block into, and what it drops. */
    private record Conversion(Block result, Item drop, int count, boolean heavyOnly) {
    }

    private static final Map<Block, Conversion> CONVERSIONS = new HashMap<>();

    private static void convert(Block from, Block to, Item drop, int count, boolean heavyOnly) {
        CONVERSIONS.put(from, new Conversion(to, drop, count, heavyOnly));
    }

    private static void convert(Block from, Block to, Item drop) {
        convert(from, to, drop, 1, false);
    }

    private static void convertHeavy(Block from, Block to, Item drop, int count) {
        convert(from, to, drop, count, true);
    }

    /** Built on first use: registry objects cannot be resolved before registration finishes. */
    private static void initConversions() {
        // Stone hammer and heavy hammer: stone -> cobbled -> gravelly, one pebble per swing.
        convert(Blocks.STONE, Blocks.COBBLESTONE, ModContent.STONE_PEBBLE.get());
        convert(Blocks.GRANITE, ModContent.COBBLED_GRANITE.get(), ModContent.GRANITE_PEBBLE.get());
        convert(Blocks.DIORITE, ModContent.COBBLED_DIORITE.get(), ModContent.DIORITE_PEBBLE.get());
        convert(Blocks.ANDESITE, ModContent.COBBLED_ANDESITE.get(), ModContent.ANDESITE_PEBBLE.get());
        convert(Blocks.COBBLESTONE, Blocks.GRAVEL, ModContent.STONE_PEBBLE.get());
        convert(ModContent.COBBLED_GRANITE.get(), ModContent.GRAVELLY_GRANITE.get(), ModContent.GRANITE_PEBBLE.get());
        convert(ModContent.COBBLED_DIORITE.get(), ModContent.GRAVELLY_DIORITE.get(), ModContent.DIORITE_PEBBLE.get());
        convert(ModContent.COBBLED_ANDESITE.get(), ModContent.GRAVELLY_ANDESITE.get(), ModContent.ANDESITE_PEBBLE.get());
        // Overworld (non-deepslate) ores crush into cobblestone.
        convert(Blocks.COAL_ORE, Blocks.COBBLESTONE, ModContent.COAL_DUST.get(), 3, false);
        convert(Blocks.COPPER_ORE, Blocks.COBBLESTONE, ModContent.COPPER_ORE_PILE.get());
        convert(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, null);

        // Heavy hammer only.
        convertHeavy(Blocks.SMOOTH_BASALT, ModContent.COBBLED_BASALT.get(), ModContent.BASALT_PEBBLE.get(), 1);
        convertHeavy(ModContent.COBBLED_BASALT.get(), ModContent.GRAVELLY_BASALT.get(), ModContent.BASALT_PEBBLE.get(), 1);
        convertHeavy(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, ModContent.DEEPSLATE_PEBBLE.get(), 1);
        convertHeavy(Blocks.COBBLED_DEEPSLATE, ModContent.GRAVELLY_DEEPSLATE.get(), ModContent.DEEPSLATE_PEBBLE.get(), 1);
        convertHeavy(Blocks.IRON_ORE, Blocks.COBBLESTONE, ModContent.IRON_ORE_PILE.get(), 1);
        convertHeavy(Blocks.DEEPSLATE_COAL_ORE, Blocks.COBBLED_DEEPSLATE, ModContent.COAL_DUST.get(), 3);
        convertHeavy(Blocks.DEEPSLATE_COPPER_ORE, Blocks.COBBLED_DEEPSLATE, ModContent.COPPER_ORE_PILE.get(), 1);
        convertHeavy(Blocks.DEEPSLATE_IRON_ORE, Blocks.COBBLED_DEEPSLATE, ModContent.IRON_ORE_PILE.get(), 1);
        convertHeavy(Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES, null, 0);
        convertHeavy(Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS, null, 0);
        convertHeavy(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, null, 0);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ToolBehavior::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(ToolBehavior::onBlockDrops);
    }

    // ---- Hammers ----------------------------------------------------------------------------

    private static void onBlockBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (event.isCanceled() || player.isCreative() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        boolean stoneHammer = tool.is(ModContent.STONE_HAMMER.get());
        boolean heavyHammer = tool.is(ModContent.HEAVY_HAMMER.get());
        if (!stoneHammer && !heavyHammer) {
            return;
        }
        BlockState state = event.getState();
        if (CONVERSIONS.isEmpty()) {
            initConversions();
        }
        Conversion conversion = CONVERSIONS.get(state.getBlock());
        if (conversion == null || (conversion.heavyOnly() && !heavyHammer) || !tool.isCorrectToolForDrops(state)) {
            return;
        }

        // Replace the vanilla break: the block is converted instead of removed.
        event.setCanceled(true);
        event.setNotifyClient(true);
        BlockPos pos = event.getPos();
        level.levelEvent(2001, pos, Block.getId(state));
        level.setBlock(pos, conversion.result().defaultBlockState(), Block.UPDATE_ALL);
        if (conversion.drop() != null) {
            Block.popResource(level, pos, new ItemStack(conversion.drop(), conversion.count()));
        }
        player.causeFoodExhaustion(0.005F);
        tool.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
    }

    // ---- Knives -----------------------------------------------------------------------------

    private static void onBlockDrops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        if (!(event.getBreaker() instanceof Player) || !(tool.is(ModContent.FLINT_KNIFE.get()) || tool.is(ModContent.IRON_KNIFE.get()))) {
            return;
        }
        BlockState state = event.getState();
        ServerLevel level = event.getLevel();

        if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.BUSH) || state.is(Blocks.RED_SHRUB)) {
            addDrop(event, new ItemStack(ModContent.PLANT_FIBER.get(), 1));
        } else if (state.is(Blocks.TALL_GRASS) || state.is(Blocks.LARGE_FERN)) {
            addDrop(event, new ItemStack(ModContent.PLANT_FIBER.get(), 2));
        } else if (state.is(BlockTags.LEAVES)) {
            // Sticks drop 33% of the time (instead of vanilla's low chance), 1-2 at a time like vanilla.
            event.getDrops().removeIf(drop -> drop.getItem().is(Items.STICK));
            if (level.getRandom().nextFloat() < 0.33F) {
                addDrop(event, new ItemStack(Items.STICK, 1 + level.getRandom().nextInt(2)));
            }
        }
    }

    private static void addDrop(BlockDropsEvent event, ItemStack stack) {
        BlockPos pos = event.getPos();
        ItemEntity entity = new ItemEntity(event.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setDefaultPickUpDelay();
        event.getDrops().add(entity);
    }

    private ToolBehavior() {
    }
}
