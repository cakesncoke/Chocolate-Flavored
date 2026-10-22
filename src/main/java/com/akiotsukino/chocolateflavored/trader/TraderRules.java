package com.akiotsukino.chocolateflavored.trader;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class TraderRules {
    private static final String RECORDED = "chocolateflavored:trader_spawn_recorded";
    private static final Map<MinecraftServer, Map<UUID, WanderingTrader>> LOADED = new WeakHashMap<>();

    public static boolean protectedFromDespawn(WanderingTrader trader) {
        return trader.hasCustomName() || trader.isPersistenceRequired() || trader.getDespawnDelay() <= 0
                || trader.getVehicle() instanceof AbstractBoat || trader.getVehicle() instanceof AbstractMinecart;
    }

    /** Called only after the native entity manager confirms a successful addition. */
    public static void added(WanderingTrader trader, boolean loadedFromDisk) {
        if (!(trader.level() instanceof ServerLevel level)) return;
        boolean fresh = !loadedFromDisk && trader.getSpawnType() != EntitySpawnReason.DIMENSION_TRAVEL
                && trader.getSpawnType() != EntitySpawnReason.LOAD && !trader.getPersistentData().getBooleanOr(RECORDED, false);
        trader.getPersistentData().putBoolean(RECORDED, true);
        if (fresh) TraderState.get(level.getServer()).spawned(TraderState.clock(level.getServer()));
        LOADED.computeIfAbsent(level.getServer(), s -> new HashMap<>()).put(trader.getUUID(), trader);
        // A new native spawn still needs its despawn delay assigned; loaded NBT is already complete.
        if (loadedFromDisk) observe(trader);
    }

    @SubscribeEvent
    public static void beforeTick(ServerTickEvent.Pre event) { synchronize(event.getServer()); }

    public static void synchronize(MinecraftServer server) {
        var state = TraderState.get(server);
        state.initialize(TraderState.clock(server));
        var loaded = LOADED.get(server);
        if (loaded == null) return;
        var iterator = loaded.values().iterator();
        while (iterator.hasNext()) {
            var trader = iterator.next();
            if (trader.isRemoved()) {
                var reason = trader.getRemovalReason();
                if (reason != null && reason.shouldDestroy()) state.removed(trader.getUUID());
                iterator.remove();
            } else observe(trader);
        }
    }

    public static void observe(WanderingTrader trader) {
        if (!(trader.level() instanceof ServerLevel level)) return;
        var state = TraderState.get(level.getServer());
        if (!trader.isAlive()) state.removed(trader.getUUID());
        else if (protectedFromDespawn(trader)) state.keep(trader.getUUID());
        // Once kept, a living trader blocks spawning until it actually dies or leaves permanently.
    }

    public static void mounted(WanderingTrader trader) {
        if (trader.level() instanceof ServerLevel level) {
            var loaded = LOADED.get(level.getServer());
            if (loaded != null && loaded.get(trader.getUUID()) == trader) observe(trader);
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof WanderingTrader trader)) return;
        var server = level.getServer();
        var loaded = LOADED.get(server);
        Entity.RemovalReason reason = trader.getRemovalReason();
        if (reason != null && loaded != null) loaded.remove(trader.getUUID(), trader);
        if (reason != null && reason.shouldDestroy()) TraderState.get(server).removed(trader.getUUID());
        else if (protectedFromDespawn(trader)) TraderState.get(server).keep(trader.getUUID());
        // Chunk unload and dimension travel must never clear a kept UUID, even if unloading detached a boat.
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { LOADED.remove(event.getServer()); }

    private TraderRules() {}
}
