package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Villager.class)
abstract class VillagerDayLengthMixin {
    @ModifyConstant(method = "maybeDecayGossip", constant = @Constant(longValue = 24000L))
    private long chocolateflavored$dailyGossipDecay(long original) {
        return DayNightCycle.FULL_DAY_SIMULATION_TICKS;
    }
    @ModifyConstant(method = "shouldRestock", constant = @Constant(longValue = 12000L))
    private long chocolateflavored$halfDayRestockWindow(long original) {
        return DayNightCycle.FULL_DAY_SIMULATION_TICKS / 2;
    }
}
