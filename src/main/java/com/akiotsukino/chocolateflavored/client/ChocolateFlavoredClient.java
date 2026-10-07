package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.renderer.OvenRenderer;
import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = ChocolateFlavored.MOD_ID, dist = Dist.CLIENT)
public final class ChocolateFlavoredClient {
    public ChocolateFlavoredClient(IEventBus bus) {
        bus.addListener(ChocolateFlavoredClient::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.OVEN.get(), OvenRenderer::new);
    }
}
