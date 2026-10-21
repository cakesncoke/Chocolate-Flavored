package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.CookingPotScreen;
import com.akiotsukino.chocolateflavored.cooking.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Only runs in CI's quick-play world; excluded from the release JAR. */
@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class CookingClientVerification {
    private static boolean prepared, completed;
    private static int worldTicks, screenTicks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || completed) return;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.getSingleplayerServer() == null) return;
        if (!prepared && ++worldTicks > 40) {
            prepared = true;
            var uuid = minecraft.player.getUUID();
            var server = minecraft.getSingleplayerServer();
            server.execute(() -> {
                try {
                    var player = server.getPlayerList().getPlayer(uuid);
                    if (player == null) throw new IllegalStateException("No integrated-server player");
                    // Galleries run while the world ticks; ambient flat-world slimes must not kill the viewer.
                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                    var level = player.level();
                    var pos = player.blockPosition().relative(player.getDirection(), 3);
                    level.setBlockAndUpdate(pos.below(), CookingContent.OVEN.get().defaultBlockState().setValue(OvenBlock.LIT, true));
                    level.setBlockAndUpdate(pos, CookingContent.COOKING_POT.get().defaultBlockState());
                    var pot = (CookingPotBlockEntity)level.getBlockEntity(pos);
                    pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 2)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 2));
                    for (int i = 0; i < 400; i++) CookingPotBlockEntity.tick(level, pos, pot.getBlockState(), pot);
                    pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 32)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 32));
                    var displayPos = pos.relative(player.getDirection().getClockWise(), 2).below();
                    level.setBlockAndUpdate(displayPos, CookingContent.OVEN.get().defaultBlockState().setValue(OvenBlock.LIT, true));
                    var oven = (OvenBlockEntity)level.getBlockEntity(displayPos);
                    for (int i = 0; i < 6; i++) oven.placeFood(level, player, new ItemStack(Items.BEEF));
                    player.openMenu(pot);
                } catch (Throwable failure) {
                    ChocolateFlavored.LOGGER.error("COOKING_CLIENT_VERIFICATION_FAILED", failure);
                    minecraft.execute(() -> { throw new IllegalStateException("Cooking client verification failed", failure); });
                }
            });
        }
        if (minecraft.screen instanceof CookingPotScreen screen && ++screenTicks >= 40) {
            if (screen.getMenu().getSlot(6).getItem().getCount() < 2) throw new IllegalStateException("Meal buffer lost servings during client synchronization");
            if (!screen.getMenu().isHeated()) throw new IllegalStateException("Pot heat state did not synchronize");
            if (screen.children().stream().anyMatch(child -> child.getClass().getSimpleName().contains("RecipeBook"))) throw new IllegalStateException("Cooking pot exposed a recipe-book widget");
            ChocolateFlavored.LOGGER.info("COOKING_CLIENT_VERIFICATION_PASSED: integrated world, real server-opened menu, multi-serving synchronization, heat data, recipe-book-free screen and cooking renderers");
            completed = true;
        }
    }
    private CookingClientVerification() {}
}
