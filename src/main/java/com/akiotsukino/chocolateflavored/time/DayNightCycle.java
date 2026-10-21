package com.akiotsukino.chocolateflavored.time;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.WorldClocks;

/** Slows the native, saved and network-synchronized clock, retaining vanilla's calendar coordinates. */
public final class DayNightCycle {
    public static final int CLOCK_TICKS_PER_DAY = 24_000;
    public static final int DAY_SIMULATION_TICKS = 20 * 60 * 20;
    public static final int NIGHT_SIMULATION_TICKS = 15 * 60 * 20;
    public static final int FULL_DAY_SIMULATION_TICKS = DAY_SIMULATION_TICKS + NIGHT_SIMULATION_TICKS;

    public static float clockRate(long time) {
        return Math.floorMod(time, CLOCK_TICKS_PER_DAY) < 12_000
                ? 12_000.0F / DAY_SIMULATION_TICKS
                : 12_000.0F / NIGHT_SIMULATION_TICKS;
    }

    public static void update(MinecraftServer server) {
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        var manager = server.clockManager();
        var state = manager.packState().clocks().get(clock);
        float rate = clockRate(state.totalTicks());
        // setRate preserves fractional progress, persists it, and sends the native clock packet.
        // Only send on a phase/rate change, never every tick.
        if (Float.compare(state.rate(), rate) != 0) manager.setRate(clock, rate);
    }

    private DayNightCycle() {}
}
