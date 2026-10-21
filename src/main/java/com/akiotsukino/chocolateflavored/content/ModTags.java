package com.akiotsukino.chocolateflavored.content;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Item> KNIVES = item("knives");
    public static final TagKey<Item> ROSE_GOLD_REPAIR = item("rose_gold_tool_materials");
    public static final TagKey<Item> FLINT_REPAIR = item("flint_tool_materials");
    public static final TagKey<Block> KNIFE_MINEABLE = block("mineable/knife");
    public static final TagKey<Block> FLINT_STONE = block("flint_to_cobblestone");
    public static final TagKey<Block> FLINT_COBBLE = block("flint_pebbles");
    public static final TagKey<Block> DEEPSLATE = block("deepslate_and_ores");
    public static final TagKey<Block> DEEPSLATE_ORES = block("deepslate_ores");
    public static final TagKey<Block> FIBER_PLANTS = block("fiber_plants");

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, path));
    }
    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, path));
    }
    private ModTags() {}
}
