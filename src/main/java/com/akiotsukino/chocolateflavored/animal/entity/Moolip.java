// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, 26.1.x revision d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.animal.entity;

import com.akiotsukino.chocolateflavored.animal.AnimalContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class Moolip extends Moobloom {
	public Moolip(EntityType<? extends Cow> p_28285_, Level p_28286_) {
		super(p_28285_, p_28286_);
	}

	@Override
	public Block getFlower() {
		return AnimalContent.PINK_DAISY.get();
	}

	@Override
	public Cow getBreedOffspring(ServerLevel p_148890_, AgeableMob p_148891_) {
		return AnimalContent.MOOLIP.get().create(p_148890_, EntitySpawnReason.BREEDING);
	}
}
