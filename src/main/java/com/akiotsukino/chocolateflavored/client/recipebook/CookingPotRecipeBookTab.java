// Adapted from Farmer's Delight, Copyright (c) 2020 vectorwing (MIT).
// See THIRD_PARTY_NOTICES.md and licenses/FarmersDelight-MIT.txt.
package com.akiotsukino.chocolateflavored.client.recipebook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

import java.util.EnumSet;
import org.jetbrains.annotations.NotNull;

public enum CookingPotRecipeBookTab implements StringRepresentable
{
	MEALS("meals"),
	DRINKS("drinks"),
	MISC("misc");

	public static final Codec<CookingPotRecipeBookTab> CODEC = Codec.STRING.flatXmap(s -> {
		CookingPotRecipeBookTab tab = findByName(s);
		if (tab == null) {
			return DataResult.error(() -> "Optional field 'recipe_book_tab' does not match any valid tab. If defined, must be one of the following: " + EnumSet.allOf(CookingPotRecipeBookTab.class));
		}
		return DataResult.success(tab);
	}, tab -> DataResult.success(tab.toString()));

	public final String name;

	CookingPotRecipeBookTab(String name) {
		this.name = name;
	}

	public static CookingPotRecipeBookTab findByName(String name) {
		for (CookingPotRecipeBookTab value : values()) {
			if (value.name.equals(name)) {
				return value;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}
}
