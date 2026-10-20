// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, revision d5acab9846bd3400ac5f093bfec7439351a1bd61 (26.1.2).
package com.akiotsukino.chocolateflavored.client.animal.render;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.animal.ModModelLayers;
import com.akiotsukino.chocolateflavored.client.animal.model.FancyChickenModel;
import com.akiotsukino.chocolateflavored.animal.entity.FancyChicken;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.chicken.BabyChickenModel;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;


public class FancyChickenRenderer<T extends FancyChicken> extends MobRenderer<T, ChickenRenderState, ChickenModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/fancy_chicken/fancy_chicken.png");
    private static final Identifier TEXTURE_BABY = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/fancy_chicken/fancy_chicken_baby.png");
    private final AdultAndBabyModelPair<ChickenModel> models;

    public FancyChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new FancyChickenModel<>(context.bakeLayer(ModModelLayers.FANCY_CHICKEN)), 0.3F);
        this.models = bakeModels(context);
    }

    private static AdultAndBabyModelPair<ChickenModel> bakeModels(EntityRendererProvider.Context context) {
        return new AdultAndBabyModelPair<>(
                new FancyChickenModel<>(context.bakeLayer(ModModelLayers.FANCY_CHICKEN)), new BabyChickenModel(context.bakeLayer(ModModelLayers.FANCY_CHICKEN_BABY))
        );
    }


    @Override
    public void submit(ChickenRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.model = this.models.getModel(state.isBaby);
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public void extractRenderState(T p_362733_, ChickenRenderState p_360515_, float p_361157_) {
        super.extractRenderState(p_362733_, p_360515_, p_361157_);
        p_360515_.flap = Mth.lerp(p_361157_, p_362733_.oFlap, p_362733_.flap);
        p_360515_.flapSpeed = Mth.lerp(p_361157_, p_362733_.oFlapSpeed, p_362733_.flapSpeed);
    }


	@Override
    public ChickenRenderState createRenderState() {
        return new ChickenRenderState();
    }

    @Override
    public Identifier getTextureLocation(ChickenRenderState renderState) {
        if (renderState.isBaby) {
            return TEXTURE_BABY;
        }
		return TEXTURE;
	}
}