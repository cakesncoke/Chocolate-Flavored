// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, 26.1.x.
package com.akiotsukino.chocolateflavored.mob.block;

import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TropicalSlimeBlock extends SlimeBlock {

    public TropicalSlimeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        if (state.getBlock() == MobContent.TROPICAL_SLIME_BLOCK.get() && other.getBlock() == Blocks.SLIME_BLOCK)
            return false;
        if (state.getBlock() == MobContent.TROPICAL_SLIME_BLOCK.get() && other.getBlock() == Blocks.HONEY_BLOCK)
            return false;
        return super.canStickTo(state, other);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }
}
