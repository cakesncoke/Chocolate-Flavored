// Adapted from Farmer's Delight (MIT); see THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.utility;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ItemUtils {
    private ItemUtils() {}
    public static void clearItems(ItemStackHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) inventory.setStackInSlot(i, ItemStack.EMPTY);
    }
    public static void spawnItemEntity(Level level, ItemStack stack, double x, double y, double z,
                                       double xMotion, double yMotion, double zMotion) {
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDeltaMovement(xMotion, yMotion, zMotion);
        level.addFreshEntity(entity);
    }
}
