package com.akiotsukino.chocolateflavored.registry;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> OVEN_IGNITERS_DAMAGE = item("oven_igniters_damage");
    public static final TagKey<Item> OVEN_IGNITERS_CONSUME = item("oven_igniters_consume");

    private ModTags() {}

    private static TagKey<Item> item(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, name));
    }
}
