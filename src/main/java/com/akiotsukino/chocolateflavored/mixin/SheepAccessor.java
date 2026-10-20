// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, 26.1.x revision d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.mixin;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sheep.class)
public interface SheepAccessor {
    @Accessor("eatBlockGoal")
    public EatBlockGoal eatBlockGoal();

    @Accessor("eatBlockGoal")
    public void setEatBlockGoal(EatBlockGoal eatBlockGoal);
}
