package com.akiotsukino.chocolateflavored.item;

import com.akiotsukino.chocolateflavored.registry.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/** Java 1.21.9 copper statistics, with 250 durability (the original iron tier value). */
public final class ModToolTiers {
    public static final Tier COPPER = new SimpleTier(
            ModTags.Blocks.INCORRECT_FOR_COPPER_TOOL,
            Tiers.IRON.getUses(),
            5.0F,
            1.0F,
            13,
            () -> Ingredient.of(ModTags.Items.COPPER_TOOL_MATERIALS));

    public static final Tier ROSE_GOLD = new SimpleTier(
            net.minecraft.tags.BlockTags.INCORRECT_FOR_IRON_TOOL,
            Tiers.DIAMOND.getUses() * 2, 7.0F, 3.0F, 15,
            () -> Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT));

    private ModToolTiers() {}
}
