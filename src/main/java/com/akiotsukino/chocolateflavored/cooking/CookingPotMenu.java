package com.akiotsukino.chocolateflavored.cooking;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** Six ingredient stacks and six normal, extractable output stacks; no recipe book. */
public final class CookingPotMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;
    public CookingPotMenu(int id, Inventory player) {
        this(id, player, new SimpleContainer(CookingPotBlockEntity.SIZE), new SimpleContainerData(3));
    }
    public CookingPotMenu(int id, Inventory player, Container inventory, ContainerData data) {
        super(CookingContent.POT_MENU.get(), id);
        checkContainerSize(inventory, CookingPotBlockEntity.SIZE); checkContainerDataCount(data, 3);
        this.inventory = inventory; this.data = data;
        for (int slot = 0; slot < 6; slot++) addSlot(new Slot(inventory, slot, 7 + slot % 3 * 18, 19 + slot / 3 * 18));
        for (int slot = 0; slot < 6; slot++) addSlot(new Slot(inventory, 6 + slot, 115 + slot % 3 * 18, 19 + slot / 3 * 18) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake(Player player, ItemStack stack) {
                if (inventory instanceof CookingPotBlockEntity pot) pot.awardExperience(player);
                super.onTake(player, stack);
            }
        });
        addStandardInventorySlots(player, 8, 84); addDataSlots(data);
    }
    public int progressWidth() { return data.get(1) <= 0 ? 0 : Math.min(24, data.get(0) * 24 / data.get(1)); }
    public boolean isHeated() { return data.get(2) != 0; }
    @Override public boolean stillValid(Player player) { return inventory.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index); if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var stack = slot.getItem(); var original = stack.copy();
        if (index < 12) { if (!moveItemStackTo(stack, 12, slots.size(), true)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, 0, 6, false)) return ItemStack.EMPTY;
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        var taken = original.copyWithCount(original.getCount() - stack.getCount());
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, taken); return original;
    }
}
