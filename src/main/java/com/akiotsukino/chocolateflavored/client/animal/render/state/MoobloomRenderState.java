// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, revision d5acab9846bd3400ac5f093bfec7439351a1bd61 (26.1.2).
package com.akiotsukino.chocolateflavored.client.animal.render.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MoobloomRenderState extends LivingEntityRenderState {
    public final BlockModelRenderState plantBlock = new BlockModelRenderState();
}
