package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.cooking.CookingContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class CookingClient {
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent event) { event.register(CookingContent.POT_MENU.get(), CookingPotScreen::new); }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerBlockEntityRenderer(CookingContent.OVEN_ENTITY.get(), OvenRenderer::new); }
    @SubscribeEvent public static void particles(RegisterParticleProvidersEvent event) { event.registerSpriteSet(CookingContent.STEAM.get(), SteamParticle.Provider::new); }
    private CookingClient() {}
}
