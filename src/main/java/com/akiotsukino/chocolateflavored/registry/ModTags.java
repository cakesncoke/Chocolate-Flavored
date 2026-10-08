package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Item> OVEN_IGNITERS_DAMAGE = item("oven_igniters_damage");
    public static final TagKey<Item> OVEN_IGNITERS_CONSUME = item("oven_igniters_consume");

    public static final class Blocks {
        public static final TagKey<Block> INCORRECT_FOR_COPPER_TOOL = block("incorrect_for_copper_tool");
        public static final TagKey<Block> HEAT_SOURCES = block("heat_sources");
        public static final TagKey<Block> HEAT_CONDUCTORS = block("heat_conductors");
        public static final TagKey<Block> TRAY_HEAT_SOURCES = block("tray_heat_sources");
        private Blocks() {}
        private static TagKey<Block> block(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, name));
        }
    }
    public static final class Items {
        public static final TagKey<Item> COPPER_TOOL_MATERIALS = item("copper_tool_materials");
        public static final TagKey<Item> SERVING_CONTAINERS = item("serving_containers");
        private Items() {}
    }

    private ModTags() {}

    private static TagKey<Item> item(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, name));
    }
}
