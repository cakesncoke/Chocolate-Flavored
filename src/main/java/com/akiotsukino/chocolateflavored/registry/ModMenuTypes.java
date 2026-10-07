// Adapted from Farmer's Delight (MIT); see THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.menu.CookingPotMenu;

import java.util.function.Supplier;

public class ModMenuTypes
{
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ChocolateFlavored.MOD_ID);

	public static final Supplier<MenuType<CookingPotMenu>> COOKING_POT = MENU_TYPES
			.register("cooking_pot", () -> IMenuTypeExtension.create(CookingPotMenu::new));
}
