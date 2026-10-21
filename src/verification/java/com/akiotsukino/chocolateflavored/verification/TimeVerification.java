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
        verifySleep(level);
        verifyTraderInterval(level);
        ChocolateFlavored.LOGGER.info("TIME_VERIFICATION_PASSED: exact 20/15-minute phases, calendar and save/load, paused clocks, actual beds/deep sleep, multiplayer thresholds, no night skip, real 2x scheduler, dawn wake, rate restoration and daily trader interval");
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
            level.setWeatherParameters(6000, 0, false, false);
            placeBed(level, firstBed); placeBed(level, secondBed);
            first.setGameMode(GameType.CREATIVE); second.setGameMode(GameType.CREATIVE);
            first.snapTo(firstBed.getX(), firstBed.getY(), firstBed.getZ(), 0, 0);
            second.snapTo(secondBed.getX(), secondBed.getY(), secondBed.getZ(), 0, 0);
            level.addNewPlayer(first); level.addNewPlayer(second);
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 100, server);
            manager.setTickRate(20);
            check(first.startSleepInBed(firstBed.east()).right().isPresent(), "Real bed accepts sleep at night");
            SleepAcceleration.update(server);
            check(manager.tickrate() == 20, "Do not accelerate before deep sleep");
            // FakePlayer.tick is empty. doTick runs the inherited Player tick, including the real sleep counter.
            for (int i = 0; i < 100; i++) first.doTick();
            check(first.isSleepingLongEnough(), "Native 100-tick sleep entry");
            SleepAcceleration.update(server);
            check(manager.tickrate() == 20, "One of two sleepers cannot meet a 100% threshold");
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 50, server);
            SleepAcceleration.update(server);
            check(manager.tickrate() == 40 && manager.nanosecondsPerTick() == 25_000_000, "Native scheduler runs the whole simulation at 40 TPS");
            for (int i = 0; i < 20; i++) SleepAcceleration.update(server);
            check(manager.tickrate() == 40, "Multiplier never compounds");
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
            for (int i = 0; i < 100; i++) second.doTick();
            SleepAcceleration.update(server); check(manager.tickrate() == 40, "Both sleepers satisfy 100%, still only 2x");
            second.stopSleepInBed(true, true);
            SleepAcceleration.update(server); check(manager.tickrate() == 20, "Leaving bed restores rate");
            second.setGameMode(GameType.SPECTATOR);
            SleepAcceleration.update(server); check(manager.tickrate() == 40, "Spectators do not count against sleep");
            manager.setFrozen(true); SleepAcceleration.update(server);
            check(manager.isFrozen() && manager.tickrate() == 20, "Administrative freeze wins");
            manager.setFrozen(false); SleepAcceleration.update(server);
            check(manager.tickrate() == 40, "Unfreezing resumes eligible sleep acceleration");
            manager.setTickRate(30); SleepAcceleration.update(server);
            check(manager.tickrate() == 60, "Custom server baseline receives exactly 2x");
            first.stopSleepInBed(true, true); SleepAcceleration.update(server);
            check(manager.tickrate() == 30, "Restore the custom baseline");
            manager.setTickRate(20);
            check(first.startSleepInBed(firstBed.east()).right().isPresent(), "Bed can be reused");
            for (int i = 0; i < 100; i++) first.doTick();
            SleepAcceleration.update(server); check(manager.tickrate() == 40, "Sleep acceleration restarts");
            server.clockManager().setTotalTicks(clock, 0); DayNightCycle.update(server);
            first.doTick(); SleepAcceleration.update(server);
            check(!first.isSleeping() && manager.tickrate() == 20, "Natural dawn bed rule wakes player and restores speed");
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
        var state = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
        level.setBlock(foot, state.setValue(BedBlock.PART, BedPart.FOOT), 3);
        level.setBlock(foot.east(), state.setValue(BedBlock.PART, BedPart.HEAD), 3);
    }

    private static void check(boolean value, String message) {
        if (!value) throw new IllegalStateException("Time verification: " + message);
    }
    private TimeVerification() {}
}
