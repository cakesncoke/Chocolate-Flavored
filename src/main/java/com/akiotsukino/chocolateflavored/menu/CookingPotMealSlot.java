// Adapted from Farmer's Delight, Copyright (c) 2020 vectorwing (MIT).
// See THIRD_PARTY_NOTICES.md and licenses/FarmersDelight-MIT.txt.
package com.akiotsukino.chocolateflavored.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CookingPotMealSlot extends SlotItemHandler
{
	public CookingPotMealSlot(IItemHandler inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return false;
	}
}
