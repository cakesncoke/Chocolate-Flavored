package com.akiotsukino.chocolateflavored;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.Tool;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Every item, block, tool material and tag that Chocolate Flavored adds. */
public final class ModContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChocolateFlavored.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChocolateFlavored.MODID);

    // ---- Tags -------------------------------------------------------------------------------
    public static final TagKey<Item> ROSE_GOLD_TOOL_MATERIALS = itemTag("rose_gold_tool_materials");
    public static final TagKey<Block> STONE_HAMMER_MINEABLE = blockTag("mineable/stone_hammer");
    public static final TagKey<Block> STONE_HAMMER_SLOW_MINEABLE = blockTag("mineable/stone_hammer_slow");
    public static final TagKey<Block> HEAVY_HAMMER_MINEABLE = blockTag("mineable/heavy_hammer");

    public static final ResourceKey<TrimMaterial> STEEL_TRIM = ResourceKey.create(Registries.TRIM_MATERIAL,
            Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "steel"));
    public static final ResourceKey<TrimMaterial> ROSE_GOLD_TRIM = ResourceKey.create(Registries.TRIM_MATERIAL,
            Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "rose_gold"));

    // ---- Tool materials ---------------------------------------------------------------------
    // Rose gold: iron's stats (steel durability 625 after the x2.5 change) + 350 durability, mining speed 7, enchantability 18.
    public static final ToolMaterial ROSE_GOLD = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 625 + 350, 7.0F, ToolMaterial.IRON.attackDamageBonus(), 18, ROSE_GOLD_TOOL_MATERIALS);
    private static final ToolMaterial STONE_HAMMER_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 131, 3.0F, 0.0F, 5, ItemTags.STONE_TOOL_MATERIALS);
    private static final ToolMaterial HEAVY_HAMMER_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 190, 5.0F, 0.0F, 5, ItemTags.STONE_TOOL_MATERIALS);
    private static final ToolMaterial FLINT_KNIFE_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 32, 1.0F, 0.0F, 5, ItemTags.WOODEN_TOOL_MATERIALS);
    private static final ToolMaterial IRON_KNIFE_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 1.0F, 0.0F, 14, ItemTags.IRON_TOOL_MATERIALS);

    // ---- Blocks -----------------------------------------------------------------------------
    private static BlockBehaviour.Properties cobbled(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(2.0F, 6.0F);
    }

    private static BlockBehaviour.Properties gravelly(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.SNARE)
                .strength(0.6F).sound(SoundType.GRAVEL);
    }

    public static final DeferredBlock<Block> COBBLED_GRANITE = block("cobbled_granite", cobbled(MapColor.DIRT));
    public static final DeferredBlock<Block> COBBLED_DIORITE = block("cobbled_diorite", cobbled(MapColor.QUARTZ));
    public static final DeferredBlock<Block> COBBLED_ANDESITE = block("cobbled_andesite", cobbled(MapColor.STONE));
    public static final DeferredBlock<Block> COBBLED_BASALT = block("cobbled_basalt", cobbled(MapColor.COLOR_BLACK).sound(SoundType.BASALT));
    public static final DeferredBlock<Block> GRAVELLY_GRANITE = block("gravelly_granite", gravelly(MapColor.DIRT));
    public static final DeferredBlock<Block> GRAVELLY_DIORITE = block("gravelly_diorite", gravelly(MapColor.QUARTZ));
    public static final DeferredBlock<Block> GRAVELLY_ANDESITE = block("gravelly_andesite", gravelly(MapColor.STONE));
    public static final DeferredBlock<Block> GRAVELLY_BASALT = block("gravelly_basalt", gravelly(MapColor.COLOR_BLACK));
    public static final DeferredBlock<Block> GRAVELLY_DEEPSLATE = block("gravelly_deepslate", gravelly(MapColor.DEEPSLATE));

    public static final DeferredBlock<Block> STEEL_BLOCK = block("steel_block", BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.IRON));
    public static final DeferredBlock<Block> ROSE_GOLD_BLOCK = block("rose_gold_block", BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.BELL)
            .requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.METAL));

    // ---- Materials --------------------------------------------------------------------------
    public static final DeferredItem<Item> STONE_PEBBLE = ITEMS.registerSimpleItem("stone_pebble");
    public static final DeferredItem<Item> ANDESITE_PEBBLE = ITEMS.registerSimpleItem("andesite_pebble");
    public static final DeferredItem<Item> DIORITE_PEBBLE = ITEMS.registerSimpleItem("diorite_pebble");
    public static final DeferredItem<Item> GRANITE_PEBBLE = ITEMS.registerSimpleItem("granite_pebble");
    public static final DeferredItem<Item> BASALT_PEBBLE = ITEMS.registerSimpleItem("basalt_pebble");
    public static final DeferredItem<Item> DEEPSLATE_PEBBLE = ITEMS.registerSimpleItem("deepslate_pebble");
    public static final DeferredItem<Item> COAL_DUST = ITEMS.registerSimpleItem("coal_dust");
    public static final DeferredItem<Item> COPPER_ORE_PILE = ITEMS.registerSimpleItem("copper_ore_pile");
    public static final DeferredItem<Item> IRON_ORE_PILE = ITEMS.registerSimpleItem("iron_ore_pile");
    public static final DeferredItem<Item> GOLD_ORE_PILE = ITEMS.registerSimpleItem("gold_ore_pile");
    public static final DeferredItem<Item> IRON_BLOOM = ITEMS.registerSimpleItem("iron_bloom");
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot", p -> p.trimMaterial(STEEL_TRIM));
    public static final DeferredItem<Item> STEEL_NUGGET = ITEMS.registerSimpleItem("steel_nugget");
    public static final DeferredItem<Item> STEEL_PLATE = ITEMS.registerSimpleItem("steel_plate");
    public static final DeferredItem<Item> ROSE_GOLD_INGOT = ITEMS.registerSimpleItem("rose_gold_ingot", p -> p.trimMaterial(ROSE_GOLD_TRIM));
    public static final DeferredItem<Item> ROSE_GOLD_NUGGET = ITEMS.registerSimpleItem("rose_gold_nugget");
    public static final DeferredItem<Item> ROSE_GOLD_UPGRADE_SMITHING_TEMPLATE = ITEMS.registerSimpleItem("rose_gold_upgrade_smithing_template");
    public static final DeferredItem<Item> DIAMOND_INGOT = ITEMS.registerSimpleItem("diamond_ingot", p -> p.trimMaterial(TrimMaterials.DIAMOND));
    public static final DeferredItem<Item> DIAMOND_PLATE = ITEMS.registerSimpleItem("diamond_plate");
    public static final DeferredItem<Item> CREEPER_OYSTERS = ITEMS.registerSimpleItem("creeper_oysters");
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.registerSimpleItem("plant_fiber");
    public static final DeferredItem<Item> PLANT_STRING = ITEMS.registerSimpleItem("plant_string");

    // ---- Tools ------------------------------------------------------------------------------
    // Rose gold uses iron's baselines. The hoe's damage is its pickaxe's minus 0.5 (see VanillaTweaks).
    public static final DeferredItem<Item> ROSE_GOLD_SWORD = ITEMS.registerSimpleItem("rose_gold_sword", p -> p.sword(ROSE_GOLD, 3.0F, -2.4F));
    public static final DeferredItem<Item> ROSE_GOLD_SHOVEL = ITEMS.registerSimpleItem("rose_gold_shovel", p -> p.shovel(ROSE_GOLD, 1.5F, -3.0F));
    public static final DeferredItem<Item> ROSE_GOLD_PICKAXE = ITEMS.registerSimpleItem("rose_gold_pickaxe", p -> p.pickaxe(ROSE_GOLD, 1.0F, -2.8F));
    public static final DeferredItem<Item> ROSE_GOLD_AXE = ITEMS.registerSimpleItem("rose_gold_axe", p -> p.axe(ROSE_GOLD, 6.0F, -3.1F));
    public static final DeferredItem<Item> ROSE_GOLD_HOE = ITEMS.registerSimpleItem("rose_gold_hoe", p -> p.hoe(ROSE_GOLD, 0.5F, -1.0F));

    // Attack damage / speed below are the values shown in the tooltip (the player's base 1.0 / 4.0 included).
    public static final DeferredItem<Item> STONE_HAMMER = ITEMS.registerSimpleItem("stone_hammer",
            p -> stoneHammerRules(p.tool(STONE_HAMMER_MATERIAL, STONE_HAMMER_MINEABLE, 10.0F - 1.0F, 0.3F - 4.0F, 0.0F)));
    public static final DeferredItem<Item> HEAVY_HAMMER = ITEMS.registerSimpleItem("heavy_hammer",
            p -> p.tool(HEAVY_HAMMER_MATERIAL, HEAVY_HAMMER_MINEABLE, 10.0F - 1.0F, 0.5F - 4.0F, 0.0F));
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.registerSimpleItem("flint_knife",
            p -> p.sword(FLINT_KNIFE_MATERIAL, 2.5F - 1.0F, 2.0F - 4.0F).repairable(Items.FLINT));
    public static final DeferredItem<Item> IRON_KNIFE = ITEMS.registerSimpleItem("iron_knife",
            p -> p.sword(IRON_KNIFE_MATERIAL, 3.5F - 1.0F, 2.0F - 4.0F));

    /** A cobbled block with its stairs, slab and wall. */
    public record Family(DeferredBlock<Block> base, DeferredBlock<StairBlock> stairs, DeferredBlock<SlabBlock> slab, DeferredBlock<WallBlock> wall) {
    }

    public static final List<Family> COBBLED_FAMILIES = List.of(
            family("cobbled_granite", COBBLED_GRANITE), family("cobbled_diorite", COBBLED_DIORITE),
            family("cobbled_andesite", COBBLED_ANDESITE), family("cobbled_basalt", COBBLED_BASALT));

    /** Block items for every block above, registered in the same order. */
    public static final List<DeferredItem<?>> BLOCK_ITEMS = List.of(
            blockItem(COBBLED_GRANITE), blockItem(COBBLED_DIORITE), blockItem(COBBLED_ANDESITE), blockItem(COBBLED_BASALT),
            blockItem(GRAVELLY_GRANITE), blockItem(GRAVELLY_DIORITE), blockItem(GRAVELLY_ANDESITE),
            blockItem(GRAVELLY_BASALT), blockItem(GRAVELLY_DEEPSLATE), blockItem(STEEL_BLOCK), blockItem(ROSE_GOLD_BLOCK));

    private static Family family(String name, DeferredBlock<Block> base) {
        var stairs = BLOCKS.registerBlock(name + "_stairs", p -> new StairBlock(base.get().defaultBlockState(), p),
                () -> BlockBehaviour.Properties.ofLegacyCopy(base.get()));
        var slab = BLOCKS.registerBlock(name + "_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(base.get()));
        var wall = BLOCKS.registerBlock(name + "_wall", WallBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(base.get()).forceSolidOn());
        ITEMS.registerSimpleBlockItem(stairs);
        ITEMS.registerSimpleBlockItem(slab);
        ITEMS.registerSimpleBlockItem(wall);
        return new Family(base, stairs, slab, wall);
    }

    /** The stone hammer also breaks deepslate and cobbled deepslate, slightly slower than its other blocks. */
    private static Item.Properties stoneHammerRules(Item.Properties properties) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return properties.component(DataComponents.TOOL, new Tool(List.of(
                Tool.Rule.deniesDrops(blocks.getOrThrow(BlockTags.INCORRECT_FOR_STONE_TOOL)),
                Tool.Rule.minesAndDrops(blocks.getOrThrow(STONE_HAMMER_SLOW_MINEABLE), 2.0F),
                Tool.Rule.minesAndDrops(blocks.getOrThrow(STONE_HAMMER_MINEABLE), 3.0F)), 1.0F, 1, true));
    }

    private static DeferredBlock<Block> block(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.registerSimpleBlock(name, (Supplier<BlockBehaviour.Properties>) () -> properties);
    }

    private static DeferredItem<?> blockItem(DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(block);
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, path));
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, path));
    }

    private ModContent() {
    }
}
