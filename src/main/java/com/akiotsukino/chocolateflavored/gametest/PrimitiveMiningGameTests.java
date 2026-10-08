package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.item.FlintItem;
import com.akiotsukino.chocolateflavored.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;

@GameTestHolder(ChocolateFlavored.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PrimitiveMiningGameTests {
    @GameTest(template = "oven_test_empty")
    public static void vanillaFlintIdentityAndFourRealStoneBreaks(GameTestHelper helper) {
        helper.assertTrue(Items.FLINT instanceof FlintItem && BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:flint")) == Items.FLINT,
                "Every vanilla flint reference must resolve to the replacement tool");
        var player = player(helper);
        var stack = new ItemStack(Items.FLINT);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(stack.getMaxDamage() == 4 && stack.getMaxStackSize() == 1
                && stack.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 1.0F
                && !stack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()), "Flint must use hand mining speed and have four charges");
        Block[] stones = {Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE};
        for (int i = 0; i < stones.length; i++) {
            BlockPos local = new BlockPos(i + 1, 2, 1);
            helper.setBlock(local, stones[i]);
            player.gameMode.destroyBlock(helper.absolutePos(local));
            helper.assertBlockPresent(Blocks.COBBLESTONE, local);
            if (i < 3) helper.assertTrue(stack.getDamageValue() == i + 1, "Each successful stone conversion must use exactly one charge");
        }
        helper.assertTrue(player.getMainHandItem().isEmpty(), "The fourth stone conversion must break the flint");
        var entities = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds());
        int pebbles = entities.stream().filter(e -> e.getItem().is(ModItems.STONE_PEBBLE.get())).mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(pebbles == 4, "Four conversions must drop four pebbles without cobblestone items");
        helper.assertTrue(entities.stream().noneMatch(e -> e.getItem().is(Items.COBBLESTONE)), "Conversion must not also harvest the block");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void cobblestoneHarvestAndUnrelatedMiningDoNotDuplicateOrWearFlint(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(Items.FLINT);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, Blocks.COBBLESTONE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertTrue(stack.getDamageValue() == 1, "Cobblestone harvesting must cost one charge");
        helper.setBlock(pos, Blocks.DIRT);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertTrue(stack.getDamageValue() == 1, "Non-stone mining must never wear flint");
        var entities = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds());
        helper.assertTrue(entities.stream().filter(e -> e.getItem().is(ModItems.DIRT_PILE.get())).mapToInt(e -> e.getItem().getCount()).sum() == 3, "Held flint must now yield dirt piles");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        helper.setBlock(pos, Blocks.STONE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).stream()
                .anyMatch(e -> e.getItem().is(Items.COBBLESTONE)), "Normal pickaxes must still harvest stone normally");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void handDropsReplaceOriginalLootAndRespectToolsAndNonPlayers(GameTestHelper helper) {
        var player = player(helper);
        Block[] blocks = {Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.PODZOL,
                Blocks.MYCELIUM, Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS,
                Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL};
        for (Block block : blocks) {
            Item pile = block == Blocks.SAND ? ModItems.SAND_PILE.get() : block == Blocks.RED_SAND ? ModItems.RED_SAND_PILE.get()
                    : block == Blocks.GRAVEL ? ModItems.GRAVEL_PILE.get() : ModItems.DIRT_PILE.get();
            var event = drops(helper, block, player, ItemStack.EMPTY);
            helper.assertTrue(count(event, pile) == 3 && count(event, block.asItem()) == 0,
                    "Hand mining must replace the original block loot with exactly three matching piles");
            var toolEvent = drops(helper, block, player, new ItemStack(Items.IRON_SHOVEL));
            helper.assertTrue(count(toolEvent, block.asItem()) == 1 && count(toolEvent, pile) == 0,
                    "Shovels must preserve normal drops");
            var noPlayer = drops(helper, block, null, ItemStack.EMPTY);
            helper.assertTrue(count(noPlayer, block.asItem()) == 1, "Explosions and automation must keep their loot");
        }
        player.setGameMode(GameType.CREATIVE);
        helper.assertTrue(count(drops(helper, Blocks.DIRT, player, ItemStack.EMPTY), ModItems.DIRT_PILE.get()) == 0,
                "Creative must not acquire survival pile drops");
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void flintFiberAndStickRatesAndExistingLeafLoot(GameTestHelper helper) {
        var player = player(helper);
        helper.getLevel().random.setSeed(78323L);
        Block[] blocks = {Blocks.DIRT, Blocks.GRAVEL, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN, Blocks.OAK_LEAVES};
        double[] chances = {0.20, 0.50, 0.33, 0.33, 0.33, 0.33, 0.25};
        for (int b = 0; b < blocks.length; b++) {
            int count = 0;
            Item item = b < 2 ? Items.FLINT : b == 6 ? Items.STICK : ModItems.PLANT_FIBER.get();
            for (int i = 0; i < 4000; i++) {
                var event = drops(helper, blocks[b], player, b < 2 ? ItemStack.EMPTY : new ItemStack(ModItems.FLINT_KNIFE.get()));
                count += count(event, item);
                if (b == 6) helper.assertTrue(count(event, Items.OAK_LEAVES) == 1, "Knife bonuses must preserve original leaf loot");
            }
            helper.assertTrue(Math.abs(count / 4000.0 - chances[b]) < 0.035, "Drop rate must match the requested probability for " + blocks[b]);
        }
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void doublePlantsDropFiberOnceFromEitherHalf(GameTestHelper helper) {
        var player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FLINT_KNIFE.get()));
        helper.getLevel().random.setSeed(99281L);
        var lower = new BlockPos(2, 2, 2);
        helper.setBlock(lower.below(), Blocks.DIRT);
        for (Block block : new Block[]{Blocks.TALL_GRASS, Blocks.LARGE_FERN}) {
            for (boolean upper : new boolean[]{false, true}) {
                int fibers = 0;
                for (int i = 0; i < 120; i++) {
                    for (var entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds())) entity.discard();
                    helper.setBlock(lower, block.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
                    helper.setBlock(lower.above(), block.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
                    player.gameMode.destroyBlock(helper.absolutePos(upper ? lower.above() : lower));
                    int drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds()).stream()
                            .filter(e -> e.getItem().is(ModItems.PLANT_FIBER.get())).mapToInt(e -> e.getItem().getCount()).sum();
                    helper.assertTrue(drops <= 1, "Breaking either plant half must never roll duplicate fiber drops");
                    fibers += drops;
                }
                helper.assertTrue(fibers > 15 && fibers < 65, "Both halves of both double plants must allow fiber harvesting");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "oven_test_empty")
    public static void canceledBreaksAndCreativeDoNotUseFlintCharges(GameTestHelper helper) {
        var player = player(helper);
        var stack = new ItemStack(Items.FLINT);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var local = new BlockPos(2, 2, 2);
        var pos = helper.absolutePos(local);
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.BreakEvent> protect = event -> {
            if (event.getPlayer() == player && event.getPos().equals(pos)) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGH, protect);
        try {
            helper.setBlock(local, Blocks.STONE);
            player.gameMode.destroyBlock(pos);
            helper.assertBlockPresent(Blocks.STONE, local);
            helper.assertTrue(stack.getDamageValue() == 0, "Canceled breaks must never convert blocks or consume flint");
        } finally {
            NeoForge.EVENT_BUS.unregister(protect);
        }
        player.setGameMode(GameType.CREATIVE);
        player.gameMode.destroyBlock(pos);
        helper.assertBlockPresent(Blocks.AIR, local);
        helper.assertTrue(stack.getDamageValue() == 0, "Creative mining must keep normal removal and no durability loss");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "primitive-test"));
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }
    private static BlockDropsEvent drops(GameTestHelper helper, Block block, ServerPlayer player, ItemStack tool) {
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var loot = new ArrayList<ItemEntity>();
        loot.add(new ItemEntity(helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(block.asItem())));
        var event = new BlockDropsEvent(helper.getLevel(), pos, block.defaultBlockState(), null, loot, player, tool);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }
    private static int count(BlockDropsEvent event, Item item) {
        return event.getDrops().stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }
}
