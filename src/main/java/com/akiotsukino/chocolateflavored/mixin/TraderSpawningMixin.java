package com.akiotsukino.chocolateflavored.mixin;

import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import com.akiotsukino.chocolateflavored.trader.TraderRules;
import com.akiotsukino.chocolateflavored.trader.TraderState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WanderingTraderData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTraderSpawner.class)
abstract class TraderSpawningMixin {
    @Unique private boolean chocolateflavored$soloFallback;
    @Shadow private boolean spawn(ServerLevel level) { throw new AssertionError(); }
    @Shadow private WanderingTraderData getTraderData() { throw new AssertionError(); }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$checkTrader(ServerLevel level, boolean spawnEnemies, CallbackInfo callback) {
        TraderRules.synchronize(level.getServer());
        var state = TraderState.get(level.getServer());
        if (state.blocksSpawning()) { callback.cancel(); return; }
        if (!state.overdue(TraderState.clock(level.getServer()))) return;
        callback.cancel();
        if (!level.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS) || level.players().isEmpty()) return;
        if (!state.claimAttempt(level.getGameTime())) return;
        chocolateflavored$soloFallback = true;
        try {
            // Reuse vanilla player/POI selection, terrain, space, biome checks, equipment and lifetime.
            // Only its random veto and llama companions are disabled for this fallback.
            if (spawn(level)) {
                getTraderData().setSpawnDelay(DayNightCycle.FULL_DAY_SIMULATION_TICKS);
                getTraderData().setSpawnChance(25);
            }
        } finally {
            chocolateflavored$soloFallback = false;
            TraderRules.synchronize(level.getServer());
        }
    }

    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int chocolateflavored$noFallbackLottery(RandomSource random, int bound) {
        return chocolateflavored$soloFallback ? 0 : random.nextInt(bound);
    }

    @Inject(method = "tryToSpawnLlamaFor", at = @At("HEAD"), cancellable = true)
    private void chocolateflavored$noFallbackLlamas(ServerLevel level, WanderingTrader trader, int radius, CallbackInfo callback) {
        if (chocolateflavored$soloFallback) callback.cancel();
    }
}
