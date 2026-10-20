// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.client.mob.model;

import com.akiotsukino.chocolateflavored.client.mob.render.state.BoulderingZombieRenderState;
import net.minecraft.client.model.geom.ModelPart;


public class BoulderingZombieModel<T extends BoulderingZombieRenderState> extends AbstractBoulderingZombieModel<T> {

	public BoulderingZombieModel(ModelPart root) {
		super(root);
	}

}
