package com.akiotsukino.chocolateflavored.time;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Uses the real server scheduler: entities, scheduled/random ticks and block entities all accelerate. */
@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class SleepAcceleration {
    private static final Map<MinecraftServer, Boost> BOOSTS = new WeakHashMap<>();

    @SubscribeEvent
    public static void beforeTick(ServerTickEvent.Pre event) {
        DayNightCycle.update(event.getServer());
        update(event.getServer());
    }

    @SubscribeEvent
    public static void afterTick(ServerTickEvent.Post event) {
        // Restore immediately after a player wakes, dies, disconnects or changes dimension.
        update(event.getServer());
    }

    public static boolean enoughSleepers(ServerLevel level) {
        if (!level.canSleepThroughNights()) return false;
        var status = new SleepStatus();
        status.update(level.players());
        int percentage = level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        return status.areEnoughSleeping(percentage) && status.areEnoughDeepSleeping(percentage, level.players());
    }

    public static void update(MinecraftServer server) {
        var manager = server.tickRateManager();
        boolean sleeping = false;
        // Administrative freeze/step/sprint takes precedence over sleeping.
        if (!manager.isFrozen() && !manager.isSprinting()) {
            for (var level : server.getAllLevels()) {
                if (enoughSleepers(level)) { sleeping = true; break; }
            }
        }
        var boost = BOOSTS.get(server);
        float current = manager.tickrate();
        if (!sleeping) {
            if (boost != null) {
                // Preserve an administrator/mod's explicit rate change made during sleep.
                if (Float.compare(current, boost.appliedRate()) == 0) manager.setTickRate(boost.baseRate());
                BOOSTS.remove(server);
            }
            return;
        }
        if (boost == null || Float.compare(current, boost.appliedRate()) != 0) {
            // Capture the actual baseline, including a custom /tick rate; never multiply repeatedly.
            boost = new Boost(current, current * 2);
            BOOSTS.put(server, boost);
            manager.setTickRate(boost.appliedRate());
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        var boost = BOOSTS.remove(event.getServer());
        var manager = event.getServer().tickRateManager();
        if (boost != null && Float.compare(manager.tickrate(), boost.appliedRate()) == 0)
            manager.setTickRate(boost.baseRate());
    }

    private record Boost(float baseRate, float appliedRate) {}
    private SleepAcceleration() {}
}
