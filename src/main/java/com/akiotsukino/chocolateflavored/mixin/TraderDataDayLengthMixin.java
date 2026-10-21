package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import net.minecraft.world.level.saveddata.WanderingTraderData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WanderingTraderData.class)
abstract class TraderDataDayLengthMixin {
    @ModifyConstant(method = {"<init>()V", "lambda$static$0"}, constant = @Constant(intValue = 24000), require = 2)
    private static int chocolateflavored$initialDailyAttempt(int original) {
        return DayNightCycle.FULL_DAY_SIMULATION_TICKS;
    }
}
