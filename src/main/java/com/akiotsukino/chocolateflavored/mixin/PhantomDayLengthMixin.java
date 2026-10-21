package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PhantomSpawner.class)
abstract class PhantomDayLengthMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 72000))
    private int chocolateflavored$threeDaysWithoutRest(int original) {
        return 3 * DayNightCycle.FULL_DAY_SIMULATION_TICKS;
    }
}
