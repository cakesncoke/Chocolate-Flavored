package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.renderer.OvenRenderer;
import com.akiotsukino.chocolateflavored.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import com.akiotsukino.chocolateflavored.client.screen.CookingPotScreen;
import com.akiotsukino.chocolateflavored.client.screen.CookingPotTooltip;
import com.akiotsukino.chocolateflavored.client.particle.SteamParticle;
import com.akiotsukino.chocolateflavored.client.recipebook.RecipeCategories;
import com.akiotsukino.chocolateflavored.registry.ModMenuTypes;
import com.akiotsukino.chocolateflavored.registry.ModParticleTypes;

@Mod(value = ChocolateFlavored.MOD_ID, dist = Dist.CLIENT)
public final class ChocolateFlavoredClient {
    public ChocolateFlavoredClient(IEventBus bus) {
        bus.addListener(ChocolateFlavoredClient::registerRenderers);
        bus.addListener(ChocolateFlavoredClient::registerScreens);
        bus.addListener(ChocolateFlavoredClient::registerParticles);
        bus.addListener(ChocolateFlavoredClient::registerTooltips);
        bus.addListener(RecipeCategories::init);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COOKING_POT.get(), CookingPotScreen::new);
    }
    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.STEAM.get(), SteamParticle.Factory::new);
    }
    private static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CookingPotTooltip.CookingPotTooltipComponent.class, CookingPotTooltip::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.OVEN.get(), OvenRenderer::new);
    }
}
