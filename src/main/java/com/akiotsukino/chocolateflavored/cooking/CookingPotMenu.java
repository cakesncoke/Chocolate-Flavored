package com.akiotsukino.chocolateflavored.cooking;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** Deliberately a plain container menu, without recipe-book state or recipe placement packets. */
public final class CookingPotMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;
    public CookingPotMenu(int id, Inventory player) {
        this(id, player, new SimpleContainer(9) { @Override public int getMaxStackSize(ItemStack stack) { return 64; } }, new SimpleContainerData(3));
    }
    public CookingPotMenu(int id, Inventory player, Container inventory, ContainerData data) {
        super(CookingContent.POT_MENU.get(), id); checkContainerSize(inventory, 9); checkContainerDataCount(data, 3);
        this.inventory = inventory; this.data = data;
        for (int slot = 0; slot < 6; slot++) addSlot(new Slot(inventory, slot, 30 + slot % 3 * 18, 17 + slot / 3 * 18));
        addSlot(new Slot(inventory, CookingPotBlockEntity.MEAL, 124, 26) {
            @Override public boolean mayPickup(Player player) { return false; }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new Slot(inventory, CookingPotBlockEntity.CONTAINER, 92, 55) {
            @Override public boolean mayPlace(ItemStack stack) { return inventory.canPlaceItem(CookingPotBlockEntity.CONTAINER, stack); }
        });
        addSlot(new Slot(inventory, CookingPotBlockEntity.OUTPUT, 124, 55) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake(Player player, ItemStack stack) { if (inventory instanceof CookingPotBlockEntity pot) pot.awardExperience(player); super.onTake(player, stack); }
        });
        addStandardInventorySlots(player, 8, 84); addDataSlots(data);
    }
    public int progressWidth() { return data.get(1) <= 0 ? 0 : Math.min(24, data.get(0) * 24 / data.get(1)); }
    public boolean isHeated() { return data.get(2) != 0; }
    @Override public boolean stillValid(Player player) { return inventory.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || index == CookingPotBlockEntity.MEAL) return ItemStack.EMPTY;
        var slot = slots.get(index); if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var stack = slot.getItem(); var original = stack.copy();
        if (index < 9) { if (!moveItemStackTo(stack, 9, slots.size(), true)) return ItemStack.EMPTY; }
        else {
            boolean serving = inventory instanceof CookingPotBlockEntity pot && pot.validContainer(stack)
                    || stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE) || stack.is(Items.BUCKET);
            if (!moveItemStackTo(stack, serving ? 7 : 0, serving ? 8 : 6, false)) return ItemStack.EMPTY;
        }
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, original); return original;
    }
}
