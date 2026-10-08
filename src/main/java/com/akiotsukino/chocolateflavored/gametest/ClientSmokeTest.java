package com.akiotsukino.chocolateflavored.gametest;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.tick.ClientTickEvent;

/** Optional CI startup check, enabled only by -PclientSmokeTest. */
public final class ClientSmokeTest {
    private static boolean done;
    private ClientSmokeTest() {}
    public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (done || minecraft.getOverlay() != null) return;
        try {
            // Force client-only spear movement mixins to transform before accepting startup.
            Class.forName("net.minecraft.client.player.LocalPlayer");
        } catch (ClassNotFoundException exception) { throw new IllegalStateException(exception); }
        var manager = minecraft.getModelManager();
        for (String item : new String[]{"iron_spear", "diamond_spear", "netherite_spear", "rose_gold_sword", "rose_gold_pickaxe",
                "rose_gold_axe", "rose_gold_shovel", "rose_gold_hoe", "rose_gold_upgrade_smithing_template", "chocolate_cake"}) {
            var model = manager.getModel(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, item), "inventory"));
            if (model == manager.getMissingModel() || !model.getParticleIcon().contents().name().getPath().equals("item/" + item)) {
                throw new IllegalStateException("Missing or incorrect item artwork: " + item);
            }
        }
        for (var state : ModBlocks.CHOCOLATE_CAKE.get().getStateDefinition().getPossibleStates()) {
            var model = manager.getBlockModelShaper().getBlockModel(state);
            if (!model.getParticleIcon().contents().name().equals(ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, "block/chocolate_cake_side"))) {
                throw new IllegalStateException("Chocolate cake bite models must use chocolate artwork");
            }
        }
        done = true;
        ChocolateFlavored.LOGGER.info("CHOCOLATE_FLAVORED_CLIENT_SMOKE_PASSED");
        minecraft.stop();
    }
}
