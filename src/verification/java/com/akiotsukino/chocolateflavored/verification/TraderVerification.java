package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import com.akiotsukino.chocolateflavored.trader.TraderRules;
import com.akiotsukino.chocolateflavored.trader.TraderState;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WanderingTraderData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.FakePlayer;

final class TraderVerification {
    private static final BlockPos POS = new BlockPos(48, 180, 0);

    static void run(ServerLevel level) {
        var server = level.getServer();
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        long oldTime = server.clockManager().getTotalTicks(clock);
        var saved = TraderState.CODEC.encodeStart(JsonOps.INSTANCE, TraderState.get(server)).getOrThrow();
        var vanilla = server.getDataStorage().computeIfAbsent(WanderingTraderData.TYPE);
        int oldDelay = vanilla.spawnDelay(), oldChance = vanilla.spawnChance();
        boolean oldRule = level.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS);
        var previous = new HashSet<UUID>();
        level.getAllEntities().forEach(e -> previous.add(e.getUUID()));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TraderCheck"));
        try {
            level.setChunkForced(POS.getX() >> 4, POS.getZ() >> 4, true);
            level.getChunkAt(POS);
            server.getDataStorage().set(TraderState.TYPE, new TraderState());
            player.setGameMode(GameType.CREATIVE);
            level.addNewPlayer(player);
            verifyCalendar();
            verifyProtection(level, player);
            verifyFallback(level, player);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Trader verification reflection failed", error);
        } finally {
            var cleanup = new ArrayList<Entity>();
            level.getAllEntities().forEach(e -> {
                if (!previous.contains(e.getUUID()) && (e instanceof WanderingTrader || e instanceof TraderLlama
                        || e instanceof AbstractBoat || e instanceof AbstractMinecart || e == player)) cleanup.add(e);
            });
            cleanup.forEach(Entity::discard);
            server.getDataStorage().set(TraderState.TYPE, TraderState.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow());
            vanilla.setSpawnDelay(oldDelay); vanilla.setSpawnChance(oldChance);
            level.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, oldRule, server);
            server.clockManager().setTotalTicks(clock, oldTime); DayNightCycle.update(server); level.updateSkyBrightness();
            level.setChunkForced(POS.getX() >> 4, POS.getZ() >> 4, false);
        }
        ChocolateFlavored.LOGGER.info("TRADER_VERIFICATION_PASSED: exact five-calendar-day fallback, solo native placement, normal llama companions, real name tags, boats/minecarts, paused despawn, saved unloaded lock, death/removal release, actual spawn receipts and game-rule handling");
    }

    private static void verifyCalendar() {
        var state = new TraderState();
        state.initialize(6000);
        check(!state.overdue(6000 + TraderState.FIVE_DAYS - 1), "No early fallback");
        check(state.overdue(6000 + TraderState.FIVE_DAYS), "Fallback at exactly five calendar days");
        state.spawned(100_000);
        check(!state.overdue(100_000 + TraderState.FIVE_DAYS - 1), "Every successful spawn resets the clock");
        check(state.claimAttempt(100) && !state.claimAttempt(1299) && state.claimAttempt(1300), "Failed placement retries once per 1,200 simulation ticks");
        state.overdue(0);
        check(!state.overdue(TraderState.FIVE_DAYS - 1), "Backward clock before last spawn starts a new waiting period");
    }

    private static void verifyProtection(ServerLevel level, FakePlayer player) {
        var server = level.getServer();
        var ordinary = trader(level, 1);
        ordinary.aiStep(); check(ordinary.isRemoved(), "Unprotected trader still despawns normally");
        var named = trader(level, 1);
        var tag = new ItemStack(Items.NAME_TAG);
        tag.set(DataComponents.CUSTOM_NAME, Component.literal("Kept merchant"));
        Items.NAME_TAG.interactLivingEntity(tag, player, named, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 10; i++) named.aiStep();
        TraderRules.synchronize(server);
        check(named.hasCustomName() && named.isPersistenceRequired() && !named.isRemoved() && named.getDespawnDelay() == 1,
                "Actual name tag freezes the despawn countdown");
        check(TraderState.get(server).blocksSpawning(), "Named trader locks spawning");
        var vanilla = server.getDataStorage().computeIfAbsent(WanderingTraderData.TYPE);
        vanilla.setSpawnDelay(1200);
        var spawner = new WanderingTraderSpawner(server.getDataStorage());
        for (int i = 0; i < 1200; i++) spawner.tick(level, true);
        check(vanilla.spawnDelay() == 1200, "Kept trader stops even vanilla spawn attempts/countdowns");

        var boat = EntityTypes.OAK_BOAT.create(level, EntitySpawnReason.COMMAND);
        boat.snapTo(POS.getX(), POS.getY(), POS.getZ(), 0, 0); level.addFreshEntity(boat);
        var passenger = trader(level, 3);
        check(passenger.startRiding(boat, true, false), "Trader boards boat");
        for (int i = 0; i < 5; i++) passenger.aiStep();
        check(!passenger.isRemoved() && passenger.getDespawnDelay() == 3, "Boat freezes the timer");
        named.discard();
        check(TraderState.get(server).blocksSpawning(), "Second kept trader still holds the lock");
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        check(passenger.saveAsPassenger(out), "Trader entity serializes");
        long lastSpawn = TraderState.get(server).lastSpawnClock();
        boat.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        passenger.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        check(TraderState.get(server).blocksSpawning(), "Unloading and forced boat detachment do not unlock spawning");
        var encoded = TraderState.CODEC.encodeStart(JsonOps.INSTANCE, TraderState.get(server)).getOrThrow();
        server.getDataStorage().set(TraderState.TYPE, TraderState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        check(TraderState.get(server).blocksSpawning(), "Kept UUID survives saved-data roundtrip");
        var restored = (WanderingTrader)EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()), level, new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.LOAD, false)).orElseThrow();
        level.addFreshEntity(restored); TraderRules.synchronize(server);
        check(TraderState.get(server).lastSpawnClock() == lastSpawn, "Reload does not count as a new spawn");
        // Unnamed trader has left its vehicle: its original timer resumes, but the lock lasts until removal.
        check(TraderState.get(server).blocksSpawning(), "Released kept trader blocks until it actually leaves");
        for (int i = 0; i < 3; i++) restored.aiStep();
        check(restored.isRemoved() && !TraderState.get(server).blocksSpawning(), "Actual despawn releases the last kept UUID");

        var minecart = EntityTypes.MINECART.create(level, EntitySpawnReason.COMMAND);
        minecart.snapTo(POS.getX(), POS.getY(), POS.getZ(), 0, 0); level.addFreshEntity(minecart);
        var cartTrader = trader(level, 1);
        check(cartTrader.startRiding(minecart, true, false), "Trader boards minecart");
        cartTrader.aiStep();
        check(!cartTrader.isRemoved() && cartTrader.getDespawnDelay() == 1 && TraderState.get(server).blocksSpawning(), "Minecart protects and locks spawning");
        cartTrader.setHealth(0); TraderRules.synchronize(server);
        check(!TraderState.get(server).blocksSpawning(), "Death releases the lock before corpse removal");
        cartTrader.discard(); minecart.discard();
    }

    private static void verifyFallback(ServerLevel level, FakePlayer player) throws ReflectiveOperationException {
        var server = level.getServer();
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        server.clockManager().setTotalTicks(clock, 200_000);
        player.snapTo(48, 180, 0, 0, 0);
        // Flat verification world supplies native safe surface positions around the player.
        for (int x = 0; x <= 6; x++) for (int z = -3; z <= 3; z++) level.getChunk(x, z);
        var state = TraderState.get(server);
        state.spawned(200_000 - TraderState.FIVE_DAYS);
        var spawner = new WanderingTraderSpawner(server.getDataStorage());
        level.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
        spawner.tick(level, true);
        check(count(level, WanderingTrader.class) == 0, "Fallback respects disabled trader spawning");
        level.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, true, server);
        level.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
        spawner.tick(level, true);
        check(state.lastSpawnClock() == 80_000, "No-player attempt cannot count as a successful spawn");
        // Recreate the driver because a removed ServerPlayer cannot be re-added.
        var driver = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FallbackDriver"));
        driver.setGameMode(GameType.CREATIVE); driver.snapTo(48, 180, 0, 0, 0); level.addNewPlayer(driver);
        try {
            spawner.tick(level, true); TraderRules.synchronize(server);
            check(count(level, WanderingTrader.class) == 1, "Five-day fallback creates one real trader without a lottery");
            check(count(level, TraderLlama.class) == 0, "Fallback trader has no llama companions");
            check(TraderState.get(server).lastSpawnClock() == 200_000, "Actual fallback join resets calendar history");
            spawner.tick(level, true);
            check(count(level, WanderingTrader.class) == 1, "No duplicate fallback on the next tick");
            discardTraders(level);
            // Prove the ordinary native path still has both companions. Seed the native private RNG.
            var randomField = WanderingTraderSpawner.class.getDeclaredField("random"); randomField.setAccessible(true);
            ((RandomSource)randomField.get(spawner)).setSeed(987654L);
            var spawn = WanderingTraderSpawner.class.getDeclaredMethod("spawn", ServerLevel.class); spawn.setAccessible(true);
            boolean spawned = false;
            for (int i = 0; i < 100 && !spawned; i++) spawned = (boolean)spawn.invoke(spawner, level);
            TraderRules.synchronize(server);
            check(spawned && count(level, WanderingTrader.class) == 1 && count(level, TraderLlama.class) == 2,
                    "Normal native spawns retain their two llamas");
        } finally { discardTraders(level); level.removePlayerImmediately(driver, Entity.RemovalReason.DISCARDED); }
    }

    private static WanderingTrader trader(ServerLevel level, int delay) {
        var trader = EntityTypes.WANDERING_TRADER.create(level, EntitySpawnReason.COMMAND);
        trader.snapTo(POS.getX(), POS.getY(), POS.getZ(), 0, 0);
        trader.setNoAi(true); trader.setNoGravity(true); trader.setDespawnDelay(delay);
        level.addFreshEntity(trader); TraderRules.synchronize(level.getServer());
        return trader;
    }
    private static long count(ServerLevel level, Class<?> type) {
        long result = 0; for (var e : level.getAllEntities()) if (type.isInstance(e) && !e.isRemoved()) result++; return result;
    }
    private static void discardTraders(ServerLevel level) {
        var entities = new ArrayList<Entity>();
        level.getAllEntities().forEach(e -> { if (e instanceof WanderingTrader || e instanceof TraderLlama) entities.add(e); });
        entities.forEach(Entity::discard);
    }
    private static void check(boolean value, String message) {
        if (!value) throw new IllegalStateException("Trader verification: " + message);
    }
    private TraderVerification() {}
}
