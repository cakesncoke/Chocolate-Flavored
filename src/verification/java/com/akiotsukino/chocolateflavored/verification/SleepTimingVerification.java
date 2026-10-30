package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.time.DayNightCycle;
import com.akiotsukino.chocolateflavored.time.SleepAcceleration;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Runs through the actual server loop so scheduler time and vanilla block-entity work are measured. */
@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class SleepTimingVerification {
    private static Fixture fixture;

    static void start(ServerLevel level) {
        var server = level.getServer();
        var bed = new BlockPos(80, 180, 0);
        var furnacePos = bed.offset(0, 0, 3);
        level.setChunkForced(bed.getX() >> 4, bed.getZ() >> 4, true);
        level.getChunkAt(bed);
        level.setBlock(bed.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(bed.east().below(), Blocks.STONE.defaultBlockState(), 3);
        var state = Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
        level.setBlock(bed, state.setValue(BedBlock.PART, BedPart.FOOT), 3);
        level.setBlock(bed.east(), state.setValue(BedBlock.PART, BedPart.HEAD), 3);
        level.setBlock(furnacePos, Blocks.FURNACE.defaultBlockState(), 3);
        var furnace = (FurnaceBlockEntity)level.getBlockEntity(furnacePos);
        furnace.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        furnace.setItem(1, new ItemStack(Items.COAL, 8));
        var sleeper = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TimedSleeper"));
        sleeper.setGameMode(GameType.CREATIVE);
        sleeper.snapTo(bed.getX(), bed.getY(), bed.getZ(), 0, 0);
        level.addNewPlayer(sleeper);
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        // A short remaining night exercises dawn, 1,050 full simulation ticks and five furnace recipes.
        // Enter at midnight, then position the fixture near dawn before starting its session.
        server.clockManager().setTotalTicks(clock, 18_000);
        DayNightCycle.update(server); level.updateSkyBrightness();
        check(sleeper.startSleepInBed(bed.east()).right().isPresent(), "Live fixture enters a real bed");
        server.clockManager().setTotalTicks(clock, 23_300); level.updateSkyBrightness();
        SleepAcceleration.update(server);
        check(server.tickRateManager().tickrate() == 52.5F, "Live session targets 52.5 TPS");
        fixture = new Fixture(level, sleeper, bed, furnacePos, level.getGameTime(), System.nanoTime());
        ChocolateFlavored.LOGGER.info("SLEEP_TIMING_VERIFICATION_STARTED: live scheduler at 52.5 TPS for 1,050 ticks");
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var f = fixture;
        if (f == null || event.getServer() != f.level().getServer()) return;
        // FakePlayer has no client tick packets; run the actual inherited Player sleep lifecycle.
        if (f.sleeper().isSleeping()) f.sleeper().doTick();
        var server = event.getServer();
        SleepAcceleration.update(server);
        var clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        long now = server.clockManager().getTotalTicks(clock);
        double seconds = (System.nanoTime() - f.started()) / 1_000_000_000.0;
        check(seconds < 45, "Live sleep finishes within the runner's timing budget");
        if (now < 24_000) return;
        check(now == 24_000 && !f.sleeper().isSleeping(), "Live sleep finishes at morning and wakes the player");
        check(server.tickRateManager().tickrate() == 20, "Live scheduler returns to 20 TPS");
        check(f.level().getGameTime() - f.gameTime() == 1_050, "All 1,050 world ticks actually ran");
        var furnace = (FurnaceBlockEntity)f.level().getBlockEntity(f.furnace());
        check(furnace.getItem(2).is(Items.STONE) && furnace.getItem(2).getCount() >= 4,
                "Vanilla furnace smelts during the simulated night");
        // Allow CI scheduling jitter, but catch an accidental old five-second entry or 2x speed.
        check(seconds >= 17 && seconds <= 29, "Live wall time remains close to the twenty-second target: " + seconds);
        check(HornUnloadedVerification.completed, "Unloaded horn recall fixture completed");
        fixture = null;
        f.level().removePlayerImmediately(f.sleeper(), Entity.RemovalReason.DISCARDED);
        for (var pos : new BlockPos[]{f.bed(), f.bed().east(), f.furnace()}) f.level().removeBlock(pos, false);
        f.level().setChunkForced(f.bed().getX() >> 4, f.bed().getZ() >> 4, false);
        ChocolateFlavored.LOGGER.info("SLEEP_TIMING_VERIFICATION_PASSED: {} real seconds, 1,050 simulated ticks, furnace output, complete dawn and restored 20 TPS", seconds);
        ChocolateFlavored.LOGGER.info("CHOCOLATE_FLAVORED_VERIFICATION_PASSED: equipment, recipes, trims, loot, mining, trades, calendar and live sleep simulation");
        server.halt(false);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Sleep timing verification: " + message);
    }
    private record Fixture(ServerLevel level, FakePlayer sleeper, BlockPos bed, BlockPos furnace,
                           long gameTime, long started) {}
    private SleepTimingVerification() {}
}
