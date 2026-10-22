package com.akiotsukino.chocolateflavored.time;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.clock.ClockState;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Fully simulates the remaining night using the native scheduler, targeting ten real seconds. */
@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class SleepAcceleration {
    public static final int SLEEP_SECONDS = 10;
    private static final Map<MinecraftServer, Session> SESSIONS = new WeakHashMap<>();

    @SubscribeEvent
    public static void beforeTick(ServerTickEvent.Pre event) {
        DayNightCycle.update(event.getServer());
        update(event.getServer());
    }

    @SubscribeEvent
    public static void afterTick(ServerTickEvent.Post event) {
        update(event.getServer());
    }

    public static boolean enoughSleepers(ServerLevel level) {
        if (!level.canSleepThroughNights()) return false;
        var clock = level.getServer().registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        if (level.dimensionType().defaultClock().filter(clock::equals).isEmpty()) return false;
        var status = new SleepStatus();
        status.update(level.players());
        int percentage = level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        // Begin at bed entry. Waiting 100 ordinary ticks first would add five seconds to the target.
        return status.areEnoughSleeping(percentage);
    }

    public static long nextMorning(long totalTicks) {
        return totalTicks + DayNightCycle.CLOCK_TICKS_PER_DAY
                - Math.floorMod(totalTicks, DayNightCycle.CLOCK_TICKS_PER_DAY);
    }

    /** Accounts for the different day/night rates, fractional clock progress and storm-time sleep. */
    public static int ticksUntilMorning(ClockState state) {
        double phase = Math.floorMod(state.totalTicks(), DayNightCycle.CLOCK_TICKS_PER_DAY) + (double)state.partialTick();
        double ticks = phase < 12_000
                ? (12_000 - phase) / DayNightCycle.clockRate(0) + DayNightCycle.NIGHT_SIMULATION_TICKS
                : (24_000 - phase) / DayNightCycle.clockRate(12_000);
        return (int)Math.ceil(ticks);
    }

    public static void update(MinecraftServer server) {
        var manager = server.tickRateManager();
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        var state = server.clockManager().packState().clocks().get(clock);
        var session = SESSIONS.get(server);
        if (session != null && state.totalTicks() >= session.morning) {
            finish(server, true);
            return;
        }
        List<ServerLevel> levels = new ArrayList<>();
        // Administrative clock pause, freeze, step and sprint always take precedence.
        if (!manager.isFrozen() && !manager.isSprinting() && !state.paused()
                && server.getGlobalGameRules().get(GameRules.ADVANCE_TIME)) {
            for (var level : server.getAllLevels()) if (enoughSleepers(level)) levels.add(level);
        }
        if (levels.isEmpty()) {
            finish(server, false);
            return;
        }
        if (session != null && state.totalTicks() < session.lastTime) {
            // A backwards /time command interrupts sleep; do not carry a dawn sleeper into another full day.
            finish(server, true);
            return;
        }
        float current = manager.tickrate();
        if (session == null) {
            session = new Session(current, ticksUntilMorning(state) / (float)SLEEP_SECONDS,
                    nextMorning(state.totalTicks()), state.totalTicks());
            SESSIONS.put(server, session);
        } else if (Float.compare(current, session.appliedRate) != 0) {
            // Preserve a deliberate administrator/mod baseline change for the end of this session.
            session.baseRate = current;
        }
        session.levels = levels;
        session.lastTime = state.totalTicks();
        // Do not slow a server whose remaining night would naturally finish sooner than ten seconds.
        float desired = Math.max(session.baseRate, session.targetRate);
        session.appliedRate = desired;
        if (Float.compare(current, desired) != 0) manager.setTickRate(desired);
    }

    @SubscribeEvent
    public static void continueThroughDawn(CanContinueSleepingEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        if (!Player.BedSleepingProblem.OTHER_PROBLEM.equals(event.getProblem())) return;
        var server = player.level().getServer();
        var session = SESSIONS.get(server);
        if (session == null || !session.levels.contains(player.level())) return;
        // 26.1.2 uses OTHER_PROBLEM for both daylight and a missing bed. Only override daylight.
        boolean hasBed = player.getSleepingPos().filter(pos -> player.level().hasChunkAt(pos))
                .map(pos -> player.level().getBlockState(pos).isBed(player.level(), pos, player)).orElse(false);
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        if (hasBed && server.clockManager().getTotalTicks(clock) < session.morning)
            event.setContinueSleeping(true);
    }

    private static void finish(MinecraftServer server, boolean morning) {
        var session = SESSIONS.remove(server);
        if (session == null) return;
        var manager = server.tickRateManager();
        if (Float.compare(manager.tickrate(), session.appliedRate) == 0) manager.setTickRate(session.baseRate);
        if (morning) {
            for (var level : session.levels) {
                // Waking updates the vanilla sleep list; iterate over a snapshot.
                for (var player : List.copyOf(level.players()))
                    if (player.isSleeping()) player.stopSleepInBed(false, true);
            }
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        finish(event.getServer(), false);
    }

    private static final class Session {
        float baseRate;
        float appliedRate;
        final float targetRate;
        final long morning;
        long lastTime;
        List<ServerLevel> levels = List.of();
        Session(float baseRate, float targetRate, long morning, long time) {
            this.baseRate = baseRate;
            this.appliedRate = baseRate;
            this.targetRate = targetRate;
            this.morning = morning;
            this.lastTime = time;
        }
    }
    private SleepAcceleration() {}
}
