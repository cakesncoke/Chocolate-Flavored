// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, revision d5acab9846bd3400ac5f093bfec7439351a1bd61 (26.1.2).
package com.akiotsukino.chocolateflavored.client.animal.render;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.animal.ModModelLayers;
import com.akiotsukino.chocolateflavored.client.animal.model.MoobloomModel;
import com.akiotsukino.chocolateflavored.client.animal.render.layer.CowPlantFlowerLayer;
import com.akiotsukino.chocolateflavored.client.animal.render.state.MoobloomRenderState;
import com.akiotsukino.chocolateflavored.animal.entity.Moobloom;
import com.akiotsukino.chocolateflavored.animal.AnimalContent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.cow.BabyCowModel;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class MoobloomRenderer extends MobRenderer<Moobloom, MoobloomRenderState, CowModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/moobloom/moobloom.png");
    private static final Identifier TEXTURE_BABY = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/moobloom/moobloom_baby.png");
    private final AdultAndBabyModelPair<CowModel> models;
    private final BlockModelResolver blockModelResolver;
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    public MoobloomRenderer(EntityRendererProvider.Context context) {
        super(context, new MoobloomModel(context.bakeLayer(ModModelLayers.MOOBLOOM)), 0.5F);
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new CowPlantFlowerLayer(this));
        this.models = bakeModels(context);
    }

    private static AdultAndBabyModelPair<CowModel> bakeModels(EntityRendererProvider.Context context) {
        return new AdultAndBabyModelPair<>(
                new MoobloomModel(context.bakeLayer(ModModelLayers.MOOBLOOM)), new BabyCowModel(context.bakeLayer(ModModelLayers.MOOBLOOM_BABY))
        );
    }


    @Override
    public void extractRenderState(Moobloom entity, MoobloomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.blockModelResolver.update(state.plantBlock, AnimalContent.BUTTERCUP.get().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);

    }

    @Override
    public void submit(MoobloomRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.model = this.models.getModel(state.isBaby);
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

	@Override
    public MoobloomRenderState createRenderState() {
        return new MoobloomRenderState();
    }


    @Override
    public Identifier getTextureLocation(MoobloomRenderState renderState) {
        if (renderState.isBaby) {
            return TEXTURE_BABY;
        }
		return TEXTURE;
	}
}