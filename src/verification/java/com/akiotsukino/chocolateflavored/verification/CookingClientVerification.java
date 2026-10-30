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
    private static volatile net.minecraft.core.BlockPos unlitKilnPos;
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
                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE); // Protect the gallery viewer from ambient slimes.
                    var level = player.level();
                    var pos = player.blockPosition().relative(player.getDirection(), 3);
                    level.setBlockAndUpdate(pos.below(), CookingContent.MUD_KILN.get().defaultBlockState().setValue(MudKilnBlock.LIT, true));
                    level.setBlockAndUpdate(pos, CookingContent.COOKING_POT.get().defaultBlockState());
                    var pot = (CookingPotBlockEntity)level.getBlockEntity(pos);
                    pot.setItem(2, new ItemStack(Items.BOWL, 2));
                    pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 2)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 2));
                    for (int i = 0; i < 400; i++) CookingPotBlockEntity.tick(level, pos, pot.getBlockState(), pot);
                    pot.setItem(2, new ItemStack(Items.BOWL, 32));
                    pot.setItem(0, new ItemStack(Items.RED_MUSHROOM, 32)); pot.setItem(1, new ItemStack(Items.BROWN_MUSHROOM, 32));
                    var displayPos = pos.relative(player.getDirection().getClockWise(), 2).below();
                    level.setBlockAndUpdate(displayPos, CookingContent.MUD_KILN.get().defaultBlockState());
                    unlitKilnPos = displayPos;
                    var mud_kiln = (MudKilnBlockEntity)level.getBlockEntity(displayPos);
                    for (int i = 0; i < 6; i++) mud_kiln.placeFood(level, player, new ItemStack(Items.BEEF));
                    player.openMenu(pot);
                } catch (Throwable failure) {
                    ChocolateFlavored.LOGGER.error("COOKING_CLIENT_VERIFICATION_FAILED", failure);
                    minecraft.execute(() -> { throw new IllegalStateException("Cooking client verification failed", failure); });
                }
            });
        }
        if (minecraft.gui.screen() instanceof CookingPotScreen screen && ++screenTicks >= 40) {
            if (!screen.getMenu().getSlot(6).getItem().is(Items.MUSHROOM_STEW)
                    || !screen.getMenu().getSlot(7).getItem().is(Items.MUSHROOM_STEW)) throw new IllegalStateException("Multi-slot outputs lost during client synchronization");
            for (int i=0;i<12;i++) {
                var slot=screen.getMenu().getSlot(i); int j=i%6;
                if(slot.x!=(i<6?7:115)+j%3*18 || slot.y!=19+j/3*18)throw new IllegalStateException("Pot slot coordinate mismatch");
            }
            if (!screen.getMenu().isHeated()) throw new IllegalStateException("Pot heat state did not synchronize");
            if (screen.children().stream().anyMatch(child -> child.getClass().getSimpleName().contains("RecipeBook"))) throw new IllegalStateException("Cooking pot exposed a recipe-book widget");
            var kiln=(MudKilnBlockEntity)minecraft.level.getBlockEntity(unlitKilnPos);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderer<MudKilnBlockEntity,net.minecraft.client.renderer.blockentity.state.CampfireRenderState> renderer=minecraft.getBlockEntityRenderDispatcher().getRenderer(kiln);
            var renderState=renderer.createRenderState();renderer.extractRenderState(kiln,renderState,0,net.minecraft.world.phys.Vec3.ZERO,null);
            int expected=net.minecraft.util.LightCoordsUtil.getLightCoords(minecraft.level,unlitKilnPos.above());
            if(renderState.lightCoords!=expected || renderState.items.stream().allMatch(net.minecraft.client.renderer.item.ItemStackRenderState::isEmpty))throw new IllegalStateException("Unlit kiln food lighting or synchronization failed");
            ChocolateFlavored.LOGGER.info("UNLIT_KILN_LIGHT_VERIFICATION_PASSED: six visible food slots use above-block ambient light");
            ChocolateFlavored.LOGGER.info("COOKING_CLIENT_VERIFICATION_PASSED: integrated world, real server-opened menu, multi-serving synchronization, heat data, recipe-book-free screen and cooking renderers");
            completed = true;
        }
    }
    private CookingClientVerification() {}
}
