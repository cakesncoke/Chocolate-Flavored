package com.akiotsukino.chocolateflavored.crop;

import com.akiotsukino.chocolateflavored.content.ModContent;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Only crop plants and raw harvests: no meals, storage blocks, or external registrations. */
public final class CropContent {
    public static final DeferredBlock<FieldCropBlock> SOYBEAN_CROP = field("soybean_crop", FieldCropBlock::new);
    public static final DeferredBlock<FieldCropBlock.Chili> CHILI_CROP = field("chili_crop", FieldCropBlock.Chili::new);
    public static final DeferredBlock<FieldCropBlock.Cotton> COTTON = field("cotton", FieldCropBlock.Cotton::new);
    public static final DeferredBlock<FieldCropBlock.Coffee> COFFEE = field("coffee", FieldCropBlock.Coffee::new);
    public static final DeferredBlock<FieldCropBlock> CABBAGES = field("cabbages", FieldCropBlock::new);
    public static final DeferredBlock<FieldCropBlock> ONIONS = field("onions", FieldCropBlock::new);
    public static final DeferredBlock<TomatoCropBlock> TOMATOES = field("tomatoes", TomatoCropBlock::new);
    public static final DeferredBlock<RiceCropBlock> RICE_CROP = field("rice_crop", RiceCropBlock::new);
    public static final DeferredBlock<RicePaniclesBlock> RICE_PANICLES = field("rice_panicles", RicePaniclesBlock::new);
    public static final DeferredItem<BlockItem> SOYBEANS = seed("soybeans", SOYBEAN_CROP);
    public static final DeferredItem<Item> SOYBEAN_POD = ModContent.ITEMS.registerSimpleItem("soybean_pod");
    public static final DeferredItem<BlockItem> CHILI_SEEDS = seed("chili_seeds", CHILI_CROP);
    public static final DeferredItem<Item> CHILI = ModContent.ITEMS.registerSimpleItem("chili");
    public static final DeferredItem<BlockItem> COTTON_SEEDS = seed("cotton_seeds", COTTON);
    public static final DeferredItem<Item> COTTON_BOLL = ModContent.ITEMS.registerSimpleItem("cotton_boll");
    public static final DeferredItem<BlockItem> COFFEE_BEANS = seed("coffee_beans", COFFEE);
    public static final DeferredItem<BlockItem> CABBAGE_SEEDS = seed("cabbage_seeds", CABBAGES);
    public static final DeferredItem<Item> CABBAGE = ModContent.ITEMS.registerSimpleItem("cabbage");
    public static final DeferredItem<BlockItem> ONION = seed("onion", ONIONS);
    public static final DeferredItem<BlockItem> TOMATO_SEEDS = seed("tomato_seeds", TOMATOES);
    public static final DeferredItem<Item> TOMATO = ModContent.ITEMS.registerSimpleItem("tomato");
    public static final DeferredItem<BlockItem> RICE = seed("rice", RICE_CROP);
    public static final DeferredItem<Item> RICE_PANICLE = ModContent.ITEMS.registerSimpleItem("rice_panicle");
    public static final DeferredBlock<WildCropBlock> WILD_SOYBEAN = wild("wild_soybean");
    public static final DeferredBlock<WildCropBlock> WILD_CHILI = wild("wild_chili");
    public static final DeferredBlock<WildCropBlock> WILD_COTTON = wild("wild_cotton");
    public static final DeferredBlock<WildCropBlock> WILD_COFFEE = wild("wild_coffee");
    public static final DeferredBlock<WildCropBlock> WILD_CABBAGES = wild("wild_cabbages");
    public static final DeferredBlock<WildCropBlock> WILD_ONIONS = wild("wild_onions");
    public static final DeferredBlock<WildCropBlock> WILD_TOMATOES = wild("wild_tomatoes");
    public static final DeferredBlock<WildCropBlock> WILD_CARROTS = wild("wild_carrots");
    public static final DeferredBlock<WildCropBlock> WILD_POTATOES = wild("wild_potatoes");
    public static final DeferredBlock<WildCropBlock> WILD_BEETROOTS = wild("wild_beetroots");
    public static final DeferredBlock<WildRiceBlock> WILD_RICE = field("wild_rice", WildRiceBlock::new);
    public static final DeferredItem<BlockItem> WILD_RICE_ITEM = ModContent.ITEMS.registerSimpleBlockItem(WILD_RICE);
    public static final List<DeferredBlock<? extends FieldCropBlock>> FIELDS = List.of(SOYBEAN_CROP, CHILI_CROP, COTTON, COFFEE, CABBAGES, ONIONS, TOMATOES);

    private static <T extends Block> DeferredBlock<T> field(String name, Function<BlockBehaviour.Properties, T> factory) {
        return ModContent.BLOCKS.registerBlock(name, factory, () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                .overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("chocolateflavored", "blocks/" + name)))));
    }
    private static DeferredBlock<WildCropBlock> wild(String name) {
        var block = ModContent.BLOCKS.registerBlock(name, WildCropBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS)
                .sound(SoundType.CROP).overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("chocolateflavored", "blocks/" + name)))));
        ModContent.ITEMS.registerSimpleBlockItem(block);
        return block;
    }
    private static DeferredItem<BlockItem> seed(String name, DeferredBlock<? extends Block> block) {
        // Deliberately no FOOD or CONSUMABLE components, including raw produce used as seed.
        return ModContent.ITEMS.registerItem(name, p -> new BlockItem(block.get(), p));
    }
    public static Item seedFor(Block block) {
        if (block == SOYBEAN_CROP.get()) return SOYBEANS.get();
        if (block == CHILI_CROP.get()) return CHILI_SEEDS.get();
        if (block == COTTON.get()) return COTTON_SEEDS.get();
        if (block == COFFEE.get()) return COFFEE_BEANS.get();
        if (block == CABBAGES.get()) return CABBAGE_SEEDS.get();
        if (block == ONIONS.get()) return ONION.get();
        if (block == TOMATOES.get()) return TOMATO_SEEDS.get();
        return RICE.get();
    }
    public static void register(IEventBus bus) { CropWorldgen.register(bus); }
    private CropContent() {}
}
