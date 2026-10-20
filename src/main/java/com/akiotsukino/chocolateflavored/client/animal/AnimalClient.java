package com.akiotsukino.chocolateflavored.client.animal;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.animal.*;
import com.akiotsukino.chocolateflavored.animal.entity.*;
import com.akiotsukino.chocolateflavored.client.animal.model.*;
import com.akiotsukino.chocolateflavored.client.animal.render.*;
import com.akiotsukino.chocolateflavored.client.animal.render.layer.*;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.animal.chicken.*;
import net.minecraft.client.model.animal.cow.*;
import net.minecraft.client.model.animal.pig.*;
import net.minecraft.client.model.animal.sheep.*;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.util.context.ContextKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class AnimalClient {
    public static final ContextKey<Float> SHAKE = new ContextKey<>(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "pig_shake"));
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimalContent.DAIRY_COW.get(), EarthCowRenderer::new);
        event.registerEntityRenderer(AnimalContent.MOOBLOOM.get(), MoobloomRenderer::new);
        event.registerEntityRenderer(AnimalContent.MOOLIP.get(), MoolipRenderer::new);
        event.registerEntityRenderer(AnimalContent.PALE_PIG.get(), EarthPigRenderer::new);
        event.registerEntityRenderer(AnimalContent.BRONZED_CHICKEN.get(), EarthChickenRenderer::new);
        event.registerEntityRenderer(AnimalContent.FANCY_CHICKEN.get(), FancyChickenRenderer::new);
        event.registerEntityRenderer(AnimalContent.FUZZY_SHEEP.get(), c -> new EarthSheepRenderer(c,"fuzzy_sheep"));
        event.registerEntityRenderer(AnimalContent.LONG_NOSE_SHEEP.get(), c -> new EarthSheepRenderer(c,"long_nosed_sheep"));
        event.registerEntityRenderer(AnimalContent.PATCHED_SHEEP.get(), c -> new EarthSheepRenderer(c,"patched_sheep"));
        event.registerEntityRenderer(AnimalContent.HORNED_SHEEP.get(), HornedSheepRenderer::new);
        event.registerEntityRenderer(AnimalContent.JUMBO_RABBIT.get(), JumboRabbitRenderer::new);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.MOOBLOOM, MoobloomModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MOOBLOOM_BABY, BabyCowModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.FANCY_CHICKEN, FancyChickenModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.FANCY_CHICKEN_BABY, BabyChickenModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HORNED_SHEEP, HornedSheepModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HORNED_SHEEP_BABY, BabyHornedSheepModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.JUMBO_RABBIT, AdultJumboRabbitModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.JUMBO_RABBIT_BABY, BabyJumboRabbitModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MUDDY_PIG, () -> MuddyPigModel.createBodyLayer(CubeDeformation.NONE));
        event.registerLayerDefinition(ModModelLayers.MUDDY_PIG_BABY, () -> BabyPigModel.createBodyLayer(CubeDeformation.NONE));
        event.registerLayerDefinition(ModModelLayers.LEGACY_COW, LegacyAnimalModels::cow);
        event.registerLayerDefinition(ModModelLayers.LEGACY_COW_BABY, () -> LegacyAnimalModels.cow().apply(CowModel.BABY_TRANSFORMER));
        event.registerLayerDefinition(ModModelLayers.LEGACY_PIG, LegacyAnimalModels::pig);
        event.registerLayerDefinition(ModModelLayers.LEGACY_PIG_BABY, () -> LegacyAnimalModels.pig().apply(PigModel.BABY_TRANSFORMER));
        event.registerLayerDefinition(ModModelLayers.BRONZED_CHICKEN, AdultChickenModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BRONZED_CHICKEN_BABY, () -> AdultChickenModel.createBodyLayer().apply(new BabyModelTransform(java.util.Set.of("head"))));
        event.registerLayerDefinition(ModModelLayers.LEGACY_SHEEP, SheepModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.LEGACY_SHEEP_BABY, () -> SheepModel.createBodyLayer().apply(new BabyModelTransform(java.util.Set.of("head"))));
        event.registerLayerDefinition(ModModelLayers.LEGACY_WOOL, SheepFurModel::createFurLayer);
        event.registerLayerDefinition(ModModelLayers.LEGACY_WOOL_BABY, () -> SheepFurModel.createFurLayer().apply(new BabyModelTransform(java.util.Set.of("head"))));
    }
    @SubscribeEvent public static void addLayers(EntityRenderersEvent.AddLayers event) {
        PigRenderer pig = event.getRenderer(EntityType.PIG);
        if (pig != null) { pig.addLayer(new MuddyPigMudLayer<>(pig,event.getEntityModels())); pig.addLayer(new MuddyPigFlowerLayer<>(pig,event.getEntityModels())); }
        EarthPigRenderer pale = event.getRenderer(AnimalContent.PALE_PIG.get());
        if (pale != null) { pale.addLayer(new MuddyPigMudLayer<>(pale,event.getEntityModels())); pale.addLayer(new MuddyPigFlowerLayer<>(pale,event.getEntityModels())); }
    }
    @SubscribeEvent public static void state(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(PigRenderer.class, (pig, state) -> copy((MuddyPig) pig, state));
        event.registerEntityModifier(EarthPigRenderer.class, (pig, state) -> copy((MuddyPig) pig, state));
    }
    private static void copy(MuddyPig pig, net.minecraft.client.renderer.entity.state.PigRenderState state) {
        state.setRenderData(MuddyPigMudLayer.IS_MUD, pig.chocolateflavored$isMuddy());
        state.setRenderData(MuddyPigMudLayer.IS_SHEARED, pig.chocolateflavored$isSheared());
        state.setRenderData(MuddyPigFlowerLayer.FLOWER_DYE, pig.chocolateflavored$getFlowerColor());
        state.setRenderData(SHAKE, pig.chocolateflavored$getShakeTicks() / 20F);
    }
    @SubscribeEvent public static void camera(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        float strength = 0F;
        for (var sheep : mc.level.getEntitiesOfClass(HornedSheep.class, mc.player.getBoundingBox().inflate(30)))
            strength = Math.max(strength, sheep.chocolateflavored$cameraShake() * Math.max(0F, 1F - sheep.distanceTo(mc.player) / 30F));
        if (strength > 0F) {
            event.setPitch(event.getPitch() + net.minecraft.util.Mth.sin(mc.player.tickCount * 1.9F) * strength * 3F);
            event.setRoll(event.getRoll() + net.minecraft.util.Mth.sin(mc.player.tickCount * 2.3F) * strength * 2F);
        }
    }
    private AnimalClient() {}
}
