package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.block.OvenBlock;
import net.minecraft.world.level.block.Blocks;
import com.akiotsukino.chocolateflavored.block.CookingPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ChocolateFlavored.MOD_ID);

    public static final DeferredBlock<OvenBlock> OVEN = BLOCKS.register("oven", () ->
            new OvenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                    .lightLevel(state -> state.getValue(OvenBlock.LIT) ? 13 : 0)));

    public static final DeferredBlock<CookingPotBlock> COOKING_POT = BLOCKS.register("cooking_pot", () ->
            new CookingPotBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(0.5F, 6.0F).sound(SoundType.LANTERN)));

    public static final DeferredBlock<com.akiotsukino.chocolateflavored.block.ChocolateCakeBlock> CHOCOLATE_CAKE =
            BLOCKS.register("chocolate_cake", () -> new com.akiotsukino.chocolateflavored.block.ChocolateCakeBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));
    public static final java.util.Map<net.minecraft.world.level.block.CandleBlock,
            DeferredBlock<com.akiotsukino.chocolateflavored.block.ChocolateCandleCakeBlock>> CHOCOLATE_CANDLES = registerChocolateCandles();

    private static java.util.Map<net.minecraft.world.level.block.CandleBlock, DeferredBlock<com.akiotsukino.chocolateflavored.block.ChocolateCandleCakeBlock>> registerChocolateCandles() {
        var result = new java.util.LinkedHashMap<net.minecraft.world.level.block.CandleBlock, DeferredBlock<com.akiotsukino.chocolateflavored.block.ChocolateCandleCakeBlock>>();
        net.minecraft.world.level.block.Block[] candles = {Blocks.CANDLE, Blocks.WHITE_CANDLE, Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE,
                Blocks.LIGHT_BLUE_CANDLE, Blocks.YELLOW_CANDLE, Blocks.LIME_CANDLE, Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE,
                Blocks.LIGHT_GRAY_CANDLE, Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE, Blocks.BLUE_CANDLE, Blocks.BROWN_CANDLE,
                Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE};
        for (var block : candles) {
            var candle = (net.minecraft.world.level.block.CandleBlock) block;
            String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(candle).getPath() + "_chocolate_cake";
            result.put(candle, BLOCKS.register(name, () -> new com.akiotsukino.chocolateflavored.block.ChocolateCandleCakeBlock(
                    candle, BlockBehaviour.Properties.ofFullCopy(Blocks.CANDLE_CAKE))));
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
