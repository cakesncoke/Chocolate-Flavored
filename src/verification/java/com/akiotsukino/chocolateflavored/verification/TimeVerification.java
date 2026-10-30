package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import com.akiotsukino.chocolateflavored.time.SleepAcceleration;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WanderingTraderData;
import net.minecraft.world.timeline.Timelines;
import net.neoforged.neoforge.common.util.FakePlayer;

final class TimeVerification {
    static void run(ServerLevel level) {
        verifyClock(level);
        check(SleepAcceleration.ticksUntilMorning(new net.minecraft.world.clock.ClockState(12_000, 0, 2.0F/3, false)) == 18_000, "Full night tick budget");
        check(SleepAcceleration.ticksUntilMorning(new net.minecraft.world.clock.ClockState(6_000, 0, .5F, false)) == 30_000, "Storm-time sleep includes the remaining day and full night");
        check(SleepAcceleration.ticksUntilMorning(new net.minecraft.world.clock.ClockState(18_000, 2.0F/3, 2.0F/3, false)) == 8_999, "Fractional progress retains exact remaining tick count");
        verifySleep(level);
        verifyTraderInterval(level);
        ChocolateFlavored.LOGGER.info("TIME_VERIFICATION_PASSED: exact 20/15-minute phases, calendar and save/load, paused clocks, actual beds, multiplayer thresholds, twenty-second native scheduler, full dawn simulation, morning wake, rate restoration and daily trader interval");
    }

    private static void verifyClock(ServerLevel level) {
        var server = level.getServer();
        var clocks = server.clockManager();
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        server.getGlobalGameRules().set(GameRules.ADVANCE_TIME, true, server);
        clocks.setPaused(clock, false);
        clocks.setTotalTicks(clock, 0);
        for (int i = 0; i < 24_000; i++) { DayNightCycle.update(server); clocks.tick(); }
        check(clocks.getTotalTicks(clock) == 12_000, "Day is exactly 24,000 simulation ticks / 20 minutes");
        for (int i = 0; i < 18_000; i++) { DayNightCycle.update(server); clocks.tick(); }
        check(clocks.getTotalTicks(clock) == 24_000, "Night is exactly 18,000 simulation ticks / 15 minutes");
        check(server.registryAccess().getOrThrow(Timelines.OVERWORLD_DAY).value().getPeriodCount(clocks) == 1, "Vanilla timeline counts one 35-minute day");
        // Fractional clock position is saved by vanilla, including during the slower daytime phase.
        DayNightCycle.update(server); clocks.tick();
        var before = clocks.packState();
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var encoded = ServerClockManager.TYPE.codec().encodeStart(ops, clocks).getOrThrow();
        var loaded = ServerClockManager.TYPE.codec().parse(ops, encoded).getOrThrow();
        loaded.init(server);
        check(loaded.packState().equals(before), "Native save/load retains fractional time and clock rate");
        clocks.setPaused(clock, true);
        long frozen = clocks.getTotalTicks(clock);
        for (int i = 0; i < 20; i++) { DayNightCycle.update(server); clocks.tick(); }
        check(clocks.getTotalTicks(clock) == frozen, "Time pause respected");
        clocks.setPaused(clock, false);
        server.getGlobalGameRules().set(GameRules.ADVANCE_TIME, false, server);
        for (int i = 0; i < 20; i++) { DayNightCycle.update(server); clocks.tick(); }
        check(clocks.getTotalTicks(clock) == frozen, "advance_time false respected");
        server.getGlobalGameRules().set(GameRules.ADVANCE_TIME, true, server);
        clocks.setTotalTicks(clock, 18_000); DayNightCycle.update(server);
        check(clocks.packState().clocks().get(clock).rate() == 2.0F / 3, "Time commands select the correct phase rate");
    }

    private static void verifySleep(ServerLevel level) {
        var server = level.getServer();
        var manager = server.tickRateManager();
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        var first = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SleepCheckA"));
        var second = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SleepCheckB"));
        var firstBed = new BlockPos(80, 180, 0);
        var secondBed = firstBed.offset(4, 0, 0);
        int percentage = level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        float originalRate = manager.tickrate();
        try {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "weather clear");
            level.updateSkyBrightness();
            placeBed(level, firstBed); placeBed(level, secondBed);
            first.setGameMode(GameType.CREATIVE); second.setGameMode(GameType.CREATIVE);
            first.snapTo(firstBed.getX(), firstBed.getY(), firstBed.getZ(), 0, 0);
            second.snapTo(secondBed.getX(), secondBed.getY(), secondBed.getZ(), 0, 0);
            level.addNewPlayer(first); level.addNewPlayer(second);
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 100, server);
            manager.setTickRate(20);
            check(first.startSleepInBed(firstBed.east()).right().isPresent(), "Real bed accepts sleep at night");
            SleepAcceleration.update(server);
            check(manager.tickrate() == 20, "One of two sleepers cannot meet a 100% threshold");
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 50, server);
            SleepAcceleration.update(server);
            check(manager.tickrate() == 450, "Half of the 15-minute night targets twenty seconds immediately on bed entry");
            for (int i = 0; i < 20; i++) SleepAcceleration.update(server);
            check(manager.tickrate() == 450, "Acceleration never compounds or restarts");
            long time = server.clockManager().getTotalTicks(clock);
            long gameTime = level.getGameTime();
            for (int i = 0; i < 6; i++) {
                DayNightCycle.update(server); server.clockManager().tick(); level.tick(() -> true);
            }
            check(first.isSleeping() && server.clockManager().getTotalTicks(clock) == time + 4, "Actual level ticks neither skip the night nor eject the sleeper");
            check(level.getGameTime() == gameTime + 6, "Scheduled world processes continue ticking during sleep");
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 100, server);
            SleepAcceleration.update(server); check(manager.tickrate() == 20, "Threshold change restores baseline");
            check(second.startSleepInBed(secondBed.east()).right().isPresent(), "Second real bed accepts sleep");
            SleepAcceleration.update(server);
            check(manager.tickrate() > 400 && manager.tickrate() <= 450, "Both sleepers start one rate sized to the remaining night");
            second.stopSleepInBed(true, true);
            SleepAcceleration.update(server); check(manager.tickrate() == 20, "Leaving bed restores rate");
            second.setGameMode(GameType.SPECTATOR);
            SleepAcceleration.update(server); check(manager.tickrate() > 400, "Spectators do not count against sleep");
            manager.setFrozen(true); SleepAcceleration.update(server);
            check(manager.isFrozen() && manager.tickrate() == 20, "Administrative freeze wins");
            manager.setFrozen(false); SleepAcceleration.update(server);
            check(manager.tickrate() > 400, "Unfreezing resumes a twenty-second sleep session");
            manager.setTickRate(30); SleepAcceleration.update(server);
            check(manager.tickrate() > 400, "Custom baseline does not lengthen the twenty-second target");
            first.stopSleepInBed(true, true); SleepAcceleration.update(server);
            check(manager.tickrate() == 30, "Restore the custom baseline");
            manager.setTickRate(20);
            server.clockManager().setPaused(clock, true);
            check(first.startSleepInBed(firstBed.east()).right().isPresent(), "Bed can be reused");
            SleepAcceleration.update(server); check(manager.tickrate() == 20, "Paused clock does not accelerate indefinitely");
            server.clockManager().setPaused(clock, false);
            SleepAcceleration.update(server); check(manager.tickrate() > 400, "Resuming time permits sleep acceleration");
            server.clockManager().setTotalTicks(clock, 23_900); level.updateSkyBrightness();
            first.doTick();
            check(first.isSleeping(), "Sleeper remains in bed through dawn to finish the entire night");
            server.clockManager().setTotalTicks(clock, 24_000); DayNightCycle.update(server); SleepAcceleration.update(server);
            check(!first.isSleeping() && manager.tickrate() == 20, "Complete morning cycle wakes sleepers and restores speed");

            // Run a complete half-night through the real clock and Player sleep lifecycle.
            // The sum of native scheduler periods must be twenty seconds, with no clock jumps.
            server.clockManager().setTotalTicks(clock, 18_000); DayNightCycle.update(server); level.updateSkyBrightness();
            check(first.startSleepInBed(firstBed.east()).right().isPresent(), "Full-cycle fixture starts sleeping");
            SleepAcceleration.update(server);
            long schedulerNanos = 0;
            for (int i = 0; i < 9_000; i++) {
                schedulerNanos += manager.nanosecondsPerTick();
                DayNightCycle.update(server); server.clockManager().tick(); level.updateSkyBrightness();
                first.doTick(); SleepAcceleration.update(server);
                if (i < 8_999) check(first.isSleeping(), "No premature dawn wake");
            }
            check(Math.abs(schedulerNanos - 20_000_000_000L) < 1_000_000, "Remaining night is exactly twenty scheduler seconds");
            check(server.clockManager().getTotalTicks(clock) == 24_000 && !first.isSleeping() && manager.tickrate() == 20, "All remaining simulation ticks finish at morning");
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 0, server);
            SleepAcceleration.update(server); check(manager.tickrate() == 20, "Zero-percent rule still needs an actual sleeper");
        } finally {
            first.stopSleepInBed(true, true); second.stopSleepInBed(true, true);
            level.removePlayerImmediately(first, Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(second, Entity.RemovalReason.DISCARDED);
            SleepAcceleration.update(server);
            manager.setFrozen(false); manager.setTickRate(originalRate);
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, percentage, server);
            for (var pos : new BlockPos[]{firstBed, firstBed.east(), secondBed, secondBed.east()}) level.removeBlock(pos, false);
        }
    }

    private static void verifyTraderInterval(ServerLevel level) {
        var server = level.getServer();
        var data = server.getDataStorage().computeIfAbsent(WanderingTraderData.TYPE);
        check(new WanderingTraderData().spawnDelay() == 42_000, "First trader attempt waits one extended day");
        check(WanderingTraderData.CODEC.parse(JsonOps.INSTANCE, new com.google.gson.JsonObject()).getOrThrow().spawnDelay() == 42_000, "Missing saved delay uses the extended day too");
        int oldDelay = data.spawnDelay(), oldChance = data.spawnChance();
        boolean oldRule = level.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS);
        try {
            level.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, true, server);
            data.setSpawnDelay(1200); data.setSpawnChance(0);
            var spawner = new WanderingTraderSpawner(server.getDataStorage());
            for (int i = 0; i < 1200; i++) spawner.tick(level, true);
            check(data.spawnDelay() == 42_000, "Subsequent trader attempts also wait one extended day");
        } finally {
            data.setSpawnDelay(oldDelay); data.setSpawnChance(oldChance);
            level.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, oldRule, server);
        }
    }

    private static void placeBed(ServerLevel level, BlockPos foot) {
        level.getChunkAt(foot);
        level.setBlock(foot.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(foot.east().below(), Blocks.STONE.defaultBlockState(), 3);
        var state = Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
        level.setBlock(foot, state.setValue(BedBlock.PART, BedPart.FOOT), 3);
        level.setBlock(foot.east(), state.setValue(BedBlock.PART, BedPart.HEAD), 3);
    }

    private static void check(boolean value, String message) {
        if (!value) throw new IllegalStateException("Time verification: " + message);
    }
    private TimeVerification() {}
}
