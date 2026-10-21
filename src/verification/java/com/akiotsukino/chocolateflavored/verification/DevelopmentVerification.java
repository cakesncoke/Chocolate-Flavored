package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.gameplay.ItemRestrictions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Runs only in the dedicated verification configuration; excluded from the published JAR. */
@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class DevelopmentVerification {
    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("chocolateflavored.verify")) return;
        ServerLevel level = event.getServer().overworld();
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        player.setGameMode(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(0, 160, 0);
        player.setPos(0.5, 161, 0.5);
        durability(Items.WOODEN_SWORD, 32);
        durability(Items.STONE_AXE, 32);
        durability(Items.COPPER_PICKAXE, 380);
        durability(Items.IRON_PICKAXE, 500);
        durability(Items.DIAMOND_PICKAXE, 3122);
        durability(Items.NETHERITE_PICKAXE, 4062);
        durability(Items.FISHING_ROD, 192);
        durability(Items.ELYTRA, 864);
        durability(Items.SHIELD, 672);
        durability(Items.SHEARS, 476);
        durability(Items.BOW, 768);
        durability(Items.GOLDEN_PICKAXE, 32);
        durability(Items.IRON_CHESTPLATE, 480);
        durability(ModContent.ROSE_GOLD_PICKAXE.get(), 576);
        durability(ModContent.KNAPPED_FLINT.get(), 4);
        durability(ModContent.FLINT_KNIFE.get(), 131);
        durability(ModContent.IRON_KNIFE.get(), 250);
        ItemStack rose = new ItemStack(ModContent.ROSE_GOLD_PICKAXE.get());
        check(rose.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 8.0F, "Rose gold mining speed");
        check(rose.isCorrectToolForDrops(Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()), "Rose gold iron-tier diamond harvesting");
        check(!rose.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "Rose gold must not have diamond tier");
        check(rose.get(DataComponents.ENCHANTABLE).value() == 22, "Rose gold enchantability");
        ItemStack copper = new ItemStack(Items.COPPER_PICKAXE);
        for (Block block : List.of(Blocks.DEEPSLATE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE)) {
            check(copper.getDestroySpeed(block.defaultBlockState()) == 1.0F, "Copper hand speed on " + block);
            check(!copper.isCorrectToolForDrops(block.defaultBlockState()), "Copper cannot harvest " + block);
        }
        for (String name : ItemRestrictions.REMOVED) {
            check(event.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,
                    Identifier.withDefaultNamespace(name))).isEmpty(), "Removed recipe " + name);
        }
        for (String name : List.of("golden_sword", "golden_pickaxe", "golden_axe", "golden_shovel", "golden_hoe",
                "golden_spear", "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots")) {
            check(event.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,
                    Identifier.withDefaultNamespace(name))).isEmpty(), "Gold equipment crafting " + name);
        }
        check(event.getServer().getRecipeManager().getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(ChocolateFlavored.MODID)).allMatch(r -> r.id().identifier().getPath().startsWith("oven/") || r.id().identifier().getPath().startsWith("cooking_pot/")), "Only requested cooking recipes added");
        var trim = new ItemStack(ModContent.ROSE_GOLD_INGOT.get()).get(DataComponents.PROVIDES_TRIM_MATERIAL);
        check(trim != null && trim.is(ModContent.ROSE_GOLD_TRIM), "Rose gold trim material component");
        check(level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).get(ModContent.ROSE_GOLD_TRIM).isPresent(), "Rose gold trim registry");

        check(count(drops(level, player, pos, Blocks.DIRT, Items.STICK), Items.DIRT) == 1, "Dirt without shovel");
        check(count(drops(level, player, pos, Blocks.DIRT, Items.IRON_SHOVEL), Items.DIRT) == 1, "Dirt with shovel");
        check(count(drops(level, player, pos, Blocks.SAND, Items.AIR), Items.SAND) == 1, "Vanilla sand");
        check(count(drops(level, player, pos, Blocks.RED_SAND, Items.STICK), Items.RED_SAND) == 1, "Vanilla red sand");
        check(drops(level, player, pos, Blocks.OAK_LOG, Items.STICK).isEmpty(), "Logs without axe");
        check(count(drops(level, player, pos, Blocks.OAK_LOG, Items.IRON_AXE), Items.OAK_LOG) == 1, "Logs with axe");
        int dirtFlint = 0, gravelFlint = 0, fibers = 0, vanillaSticks = 0, knifeSticks = 0;
        for (int i = 0; i < 1000; i++) {
            dirtFlint += count(drops(level, player, pos, Blocks.DIRT, Items.STICK), Items.FLINT);
            gravelFlint += count(drops(level, player, pos, Blocks.GRAVEL, Items.STICK), Items.FLINT);
            fibers += count(drops(level, player, pos, Blocks.SHORT_GRASS, ModContent.FLINT_KNIFE.get()), ModContent.PLANT_FIBER.get());
            vanillaSticks += count(drops(level, player, pos, Blocks.OAK_LEAVES, Items.STICK), Items.STICK);
            knifeSticks += count(drops(level, player, pos, Blocks.OAK_LEAVES, ModContent.IRON_KNIFE.get()), Items.STICK);
        }
        // Wide statistical bounds detect missing/doubled rules without fragile exact RNG expectations.
        check(dirtFlint == 0, "Dirt flint probability: " + dirtFlint);
        check(gravelFlint > 390 && gravelFlint < 610, "Gravel flint probability: " + gravelFlint);
        check(fibers > 220 && fibers < 440, "Plant fiber probability: " + fibers);
        check(knifeSticks > vanillaSticks + 100, "Knife leaves stick bonus");

        convert(level, player, pos, Blocks.STONE, ModContent.KNAPPED_FLINT.get(), Blocks.COBBLESTONE, ModContent.STONE_PEBBLE.get());
        convert(level, player, pos, Blocks.GRANITE, ModContent.KNAPPED_FLINT.get(), Blocks.COBBLESTONE, ModContent.STONE_PEBBLE.get());
        convert(level, player, pos, Blocks.COBBLESTONE, ModContent.KNAPPED_FLINT.get(), Blocks.AIR, ModContent.STONE_PEBBLE.get());
        convert(level, player, pos, Blocks.MOSSY_COBBLESTONE, ModContent.KNAPPED_FLINT.get(), Blocks.AIR, ModContent.STONE_PEBBLE.get());
        convert(level, player, pos, Blocks.COAL_ORE, ModContent.KNAPPED_FLINT.get(), Blocks.COBBLESTONE, ModContent.COAL_DUST.get());
        convert(level, player, pos, Blocks.COPPER_ORE, ModContent.KNAPPED_FLINT.get(), Blocks.COBBLESTONE, ModContent.COPPER_ORE_PILE.get());
        convert(level, player, pos, Blocks.IRON_ORE, Items.COPPER_PICKAXE, Blocks.COBBLESTONE, ModContent.IRON_ORE_PILE.get());
        convert(level, player, pos, Blocks.DEEPSLATE, Items.IRON_PICKAXE, Blocks.COBBLED_DEEPSLATE, ModContent.DEEPSLATE_PEBBLE.get());
        convert(level, player, pos, Blocks.DEEPSLATE_DIAMOND_ORE, Items.IRON_PICKAXE, Blocks.COBBLED_DEEPSLATE, Items.DIAMOND);
        convert(level, player, pos, Blocks.DEEPSLATE_COPPER_ORE, Items.IRON_PICKAXE, Blocks.COBBLED_DEEPSLATE, Items.RAW_COPPER);
        clearItems(level, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.KNAPPED_FLINT.get()));
        for (int i = 0; i < 4; i++) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            player.gameMode.destroyBlock(pos);
        }
        check(player.getMainHandItem().isEmpty(), "Knapped flint breaks after four interactions");
        clearItems(level, pos);
        level.setBlockAndUpdate(pos, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, copper);
        player.gameMode.destroyBlock(pos);
        check(level.getBlockState(pos).isAir(), "Copper still breaks deepslate ore at hand speed");
        check(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).isEmpty(), "Copper deepslate gives no drops");
        level.setBlockAndUpdate(pos, Blocks.DEEPSLATE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, rose);
        player.gameMode.destroyBlock(pos);
        check(level.getBlockState(pos).isAir(), "Rose gold retains vanilla removal");
        check(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).stream().anyMatch(i -> i.getItem().is(Items.COBBLED_DEEPSLATE)), "Rose gold retains vanilla cobbled deepslate loot");

        var villager = EntityType.VILLAGER.create(level, EntitySpawnReason.COMMAND);
        check(villager != null, "Test villager creation");
        var offers = new MerchantOffers();
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.STONE_SWORD), 10, 1, 0.0F));
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.IRON_SWORD), 10, 1, 0.0F));
        var loadedOffers = villager.getOffers();
        loadedOffers.clear();
        loadedOffers.addAll(offers);
        check(villager.getOffers().size() == 1 && villager.getOffers().getFirst().getResult().is(Items.IRON_SWORD), "Merchant mixin removes forbidden offers only");
        clearItems(level, pos);
        level.removeBlock(pos, false);
        LightingVerification.run(level, player);
        AnvilVerification.run(level, player);
        VillageVerification.run(level);
        CookingVerification.run(level, player);
        AnimalVerification.run(level, player);
        MobVerification.run(level, player);
        CleanupVerification.run(level, player);
        ExpansionVerification.run(level);
        ChocolateFlavored.LOGGER.info("CHOCOLATE_FLAVORED_VERIFICATION_PASSED: equipment, recipes, trims, loot probabilities, mining conversions, durability and trades");
        event.getServer().halt(false);
    }
    private static void durability(Item item, int value) { check(new ItemStack(item).getMaxDamage() == value, "Durability: " + item); }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException("Verification failed: " + message); }
    private static List<ItemStack> drops(ServerLevel level, FakePlayer player, BlockPos pos, Block block, Item tool) {
        return Block.getDrops(block.defaultBlockState(), level, pos, null, player, tool == Items.AIR ? ItemStack.EMPTY : new ItemStack(tool));
    }
    private static int count(List<ItemStack> drops, Item item) { return drops.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum(); }
    private static void clearItems(ServerLevel level, BlockPos pos) { level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).forEach(ItemEntity::discard); }
    private static void convert(ServerLevel level, FakePlayer player, BlockPos pos, Block from, Item tool, Block to, Item expected) {
        clearItems(level, pos);
        level.setBlockAndUpdate(pos, from.defaultBlockState());
        ItemStack held = new ItemStack(tool);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        player.gameMode.destroyBlock(pos);
        check(level.getBlockState(pos).is(to), "Replacement of " + from + " using " + tool);
        check(held.getDamageValue() == 1, "Exactly one tool wear on " + from);
        var items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3));
        check(!items.isEmpty() && items.stream().allMatch(i -> i.getItem().is(expected)), "Conversion drops from " + from);
    }
}
