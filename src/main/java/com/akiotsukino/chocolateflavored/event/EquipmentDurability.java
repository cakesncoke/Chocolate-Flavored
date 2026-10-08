package com.akiotsukino.chocolateflavored.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** Change vanilla item defaults without changing IDs, repair materials, enchantments or copper's tier. */
public final class EquipmentDurability {
    private EquipmentDurability() {}

    public static void modifyDefaults(ModifyDefaultComponentsEvent event) {
        multiply(event, 1.5, Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        multiply(event, 2.0, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE,
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        multiply(event, 3.0, Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
    }

    private static void multiply(ModifyDefaultComponentsEvent event, double multiplier, Item... items) {
        for (Item item : items) {
            Integer durability = item.components().get(DataComponents.MAX_DAMAGE);
            if (durability != null) {
                int maximum = (int) Math.ceil(durability * multiplier);
                event.modify(item, patch -> patch.set(DataComponents.MAX_DAMAGE, maximum));
            }
        }
    }
}
