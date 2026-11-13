package com.akiotsukino.chocolateflavored;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only setup. */
@Mod(value = ChocolateFlavored.MODID, dist = Dist.CLIENT)
public class ChocolateFlavoredClient {
    public ChocolateFlavoredClient(IEventBus modEventBus) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerBlockEntityRenderer(ModContent.EARTHEN_KILN_ENTITY.get(), EarthenKilnRenderer::new));
    }
}
