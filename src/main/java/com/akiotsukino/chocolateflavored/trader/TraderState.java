package com.akiotsukino.chocolateflavored.trader;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Global calendar history and kept-trader UUIDs, independent of loaded chunks or dimensions. */
public final class TraderState extends SavedData {
    public static final long FIVE_DAYS = 5L * DayNightCycle.CLOCK_TICKS_PER_DAY;
    public static final int RETRY_TICKS = 1200;
    public static final Codec<TraderState> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.optionalFieldOf("last_spawn_clock", -1L).forGetter(s -> s.lastSpawnClock),
            Codec.LONG.optionalFieldOf("next_attempt_game_time", 0L).forGetter(s -> s.nextAttemptGameTime),
            UUIDUtil.CODEC.listOf().optionalFieldOf("kept_traders", List.of()).forGetter(s -> List.copyOf(s.kept))
    ).apply(builder, TraderState::new));
    public static final SavedDataType<TraderState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "wandering_traders"),
            TraderState::new, CODEC, DataFixTypes.LEVEL);
    private long lastSpawnClock;
    private long nextAttemptGameTime;
    private final Set<UUID> kept;

    public TraderState() { this(-1, 0, List.of()); }
    private TraderState(long lastSpawnClock, long nextAttemptGameTime, List<UUID> kept) {
        this.lastSpawnClock = lastSpawnClock;
        this.nextAttemptGameTime = nextAttemptGameTime;
        this.kept = new HashSet<>(kept);
    }
    public static TraderState get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
    public static long clock(MinecraftServer server) {
        return server.clockManager().getTotalTicks(server.registryAccess().getOrThrow(WorldClocks.OVERWORLD));
    }
    public void initialize(long now) {
        if (lastSpawnClock < 0 || now < lastSpawnClock) { lastSpawnClock = now; setDirty(); }
    }
    public boolean overdue(long now) { initialize(now); return now - lastSpawnClock >= FIVE_DAYS; }
    public void spawned(long now) {
        lastSpawnClock = now;
        nextAttemptGameTime = 0;
        setDirty();
    }
    public long lastSpawnClock() { return lastSpawnClock; }
    public boolean claimAttempt(long gameTime) {
        if (gameTime < nextAttemptGameTime) return false;
        nextAttemptGameTime = gameTime + RETRY_TICKS;
        setDirty();
        return true;
    }
    public boolean blocksSpawning() { return !kept.isEmpty(); }
    public void keep(UUID id) { if (kept.add(id)) setDirty(); }
    public void removed(UUID id) { if (kept.remove(id)) setDirty(); }
}
