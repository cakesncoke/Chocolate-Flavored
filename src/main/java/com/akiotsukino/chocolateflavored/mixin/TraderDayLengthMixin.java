package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WanderingTraderSpawner.class)
abstract class TraderDayLengthMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 24000))
    private int chocolateflavored$dailyAttempt(int original) {
        return DayNightCycle.FULL_DAY_SIMULATION_TICKS;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 48000))
    private int chocolateflavored$twoDayVisit(int original) {
        return 2 * DayNightCycle.FULL_DAY_SIMULATION_TICKS;
    }
}
