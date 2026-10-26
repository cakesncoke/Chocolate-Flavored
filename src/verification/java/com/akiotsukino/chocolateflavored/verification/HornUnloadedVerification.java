package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.horn.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Lets the native entity/chunk managers save, unload, and reload a linked pet during the live server loop. */
@EventBusSubscriber(modid = ChocolateFlavored.MODID)
public final class HornUnloadedVerification {
    private static ServerLevel level;
    private static FakePlayer caller;
    private static UUID target;
    private static int ticks, stage;
    public static boolean completed;
    private static final BlockPos REMOTE = new BlockPos(2048, 180, 2048);
    static void start(ServerLevel testLevel) {
        level = testLevel;
        caller = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HornCaller"));
        caller.setGameMode(GameType.SPECTATOR);
        caller.setPos(.5, 180, .5);
        level.addNewPlayer(caller);
        level.setChunkForced(REMOTE.getX() >> 4, REMOTE.getZ() >> 4, true);
        level.getChunkAt(REMOTE);
        var wolf = EntityType.WOLF.create(level, EntitySpawnReason.COMMAND);
        wolf.setTame(true, true); wolf.setOwner(caller); wolf.setNoAi(true); wolf.setNoGravity(true); wolf.setInvulnerable(true);
        wolf.setPos(REMOTE.getCenter()); level.addFreshEntity(wolf);
        target = wolf.getUUID();
        check(HornRecall.bind(caller, new ItemStack(ModContent.COPPER_HORN.get()), wolf), "Bind fixture");
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        if (level == null || completed || event.getServer() != level.getServer()) return;
        ticks++;
        if (stage == 0 && ticks >= 5) {
            level.setChunkForced(REMOTE.getX() >> 4, REMOTE.getZ() >> 4, false);
            stage = 1;
        }
        if (stage == 1) {
            check(ticks < 600, "Remote chunk unloads within fixture budget");
            if (HornRecall.loaded(level.getServer(), target) != null) return;
            var location = HornLinks.get(level.getServer()).find(target);
            check(location != null && location.pos().equals(REMOTE), "Unload retains final location");
            HornRecall.request(caller, target);
            stage = 2; ticks = 0;
        } else if (stage == 2) {
            check(ticks < 240, "Recall reloads linked pet");
            var returned = HornRecall.loaded(level.getServer(), target);
            if (returned == null || returned.position().distanceToSqr(caller.position()) > .01) return;
            check(returned.getUUID().equals(target), "No duplicate or replacement UUID");
            returned.discard();
            level.removePlayerImmediately(caller, Entity.RemovalReason.DISCARDED);
            completed = true;
            ChocolateFlavored.LOGGER.info("HORN_UNLOADED_VERIFICATION_PASSED: native chunk save/unload, temporary ticket reload, same UUID recalled");
        }
    }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException("Unloaded horn: " + message); }
}
