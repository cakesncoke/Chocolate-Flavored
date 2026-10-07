// Adapted from Farmer's Delight, Copyright (c) 2020 vectorwing (MIT).
// See THIRD_PARTY_NOTICES.md and licenses/FarmersDelight-MIT.txt.
package com.akiotsukino.chocolateflavored.block.state;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CookingPotSupport implements StringRepresentable
{
	NONE("none"),
	TRAY("tray"),
	HANDLE("handle");

	private final String supportName;

	CookingPotSupport(String name) {
		this.supportName = name;
	}

	@Override
	public String toString() {
		return this.getSerializedName();
	}

	@Override
	public @NotNull String getSerializedName() {
		return this.supportName;
	}
}
