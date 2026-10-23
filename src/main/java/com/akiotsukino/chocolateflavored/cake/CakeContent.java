package com.akiotsukino.chocolateflavored.cake;

import com.akiotsukino.chocolateflavored.content.ModContent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CakeContent {
    public static final List<String> CANDLES = List.of("candle", "white_candle", "orange_candle", "magenta_candle",
            "light_blue_candle", "yellow_candle", "lime_candle", "pink_candle", "gray_candle", "light_gray_candle",
            "cyan_candle", "purple_candle", "blue_candle", "brown_candle", "green_candle", "red_candle", "black_candle");
    public static final List<Flavor> FLAVORS = new ArrayList<>();
    public static final Flavor POMEGRANATE = flavor("pomegranate");
    public static final Flavor COFFEE = flavor("coffee");
    public static final Flavor CHERRY = flavor("cherry");
    public static final Flavor CHOCOLATE = flavor("chocolate");

    public record Flavor(String name, DeferredBlock<FlavoredCakeBlock> block, DeferredItem<BlockItem> item,
                         Map<String, DeferredBlock<FlavoredCandleCakeBlock>> candles) {}

    private static Flavor flavor(String name) {
        var cake = ModContent.BLOCKS.registerBlock(name + "_cake", FlavoredCakeBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).overrideLootTable(loot(name + "_cake")));
        var item = ModContent.ITEMS.registerItem(name + "_cake", p -> new BlockItem(cake.get(), p.stacksTo(1).useBlockDescriptionPrefix()));
        Map<String, DeferredBlock<FlavoredCandleCakeBlock>> candles = new LinkedHashMap<>();
        for (String candle : CANDLES) {
            String id = candle + "_" + name + "_cake";
            candles.put(candle, ModContent.BLOCKS.registerBlock(id,
                    p -> new FlavoredCandleCakeBlock(cake.get(), BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(candle)), p),
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CANDLE_CAKE).overrideLootTable(loot(id))));
        }
        var flavor = new Flavor(name, cake, item, Map.copyOf(candles));
        FLAVORS.add(flavor);
        return flavor;
    }
    public static BlockState withCandle(Block cake, Block candle) {
        for (var flavor : FLAVORS) if (flavor.block().get() == cake) {
            var form = flavor.candles().get(BuiltInRegistries.BLOCK.getKey(candle).getPath());
            if (form != null && BuiltInRegistries.BLOCK.getKey(candle).getNamespace().equals("minecraft")) return form.get().defaultBlockState();
        }
        return null;
    }
    private static Optional<ResourceKey<LootTable>> loot(String name) {
        return Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("chocolateflavored", "blocks/" + name)));
    }
    public static void register(IEventBus bus) { /* Shared ModContent registers the blocks and items. */ }
    private CakeContent() {}
}
