// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, revision d5acab9846bd3400ac5f093bfec7439351a1bd61 (26.1.2).
package com.akiotsukino.chocolateflavored.client.animal.render;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.animal.ModModelLayers;
import com.akiotsukino.chocolateflavored.client.animal.model.BabyHornedSheepModel;
import com.akiotsukino.chocolateflavored.client.animal.model.HornedSheepModel;
import com.akiotsukino.chocolateflavored.client.animal.render.layer.HornedSheepWoolLayer;
import com.akiotsukino.chocolateflavored.client.animal.render.layer.HornedSheepWoolUndercoatLayer;

import com.akiotsukino.chocolateflavored.client.animal.render.state.HornedSheepRenderState;
import com.akiotsukino.chocolateflavored.animal.entity.HornedSheep;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;


public class HornedSheepRenderer extends AgeableMobRenderer<HornedSheep, HornedSheepRenderState, HornedSheepModel<HornedSheepRenderState>> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/horned_sheep/horned_sheep.png");
	private static final Identifier TEXTURE_HORNLESS = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/horned_sheep/horned_sheep_hornless.png");
	private static final Identifier TEXTURE_BABY = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/horned_sheep/horned_sheep_baby.png");
	private static final Identifier TEXTURE_BABY_HORNLESS = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/horned_sheep/horned_sheep_baby_hornless.png");



	public HornedSheepRenderer(EntityRendererProvider.Context p_173952_) {
		super(p_173952_, new HornedSheepModel<>(p_173952_.bakeLayer(ModModelLayers.HORNED_SHEEP)), new BabyHornedSheepModel<>(p_173952_.bakeLayer(ModModelLayers.HORNED_SHEEP_BABY)), 0.5F);
		this.addLayer(new HornedSheepWoolLayer(this, p_173952_.getModelSet()));
		this.addLayer(new HornedSheepWoolUndercoatLayer(this, p_173952_.getModelSet()));

	}

	@Override
	public HornedSheepRenderState createRenderState() {
		return new HornedSheepRenderState();
	}

	@Override
	public void extractRenderState(HornedSheep p_362733_, HornedSheepRenderState p_360515_, float p_361157_) {
		super.extractRenderState(p_362733_, p_360515_, p_361157_);
		p_360515_.horn = p_362733_.hasHorn();
		p_360515_.agressiveScale = p_362733_.getAggressiveAnimationScale(p_361157_);
		p_360515_.headEatAngleScale = p_362733_.getHeadEatAngleScale(p_361157_);
		p_360515_.headEatPositionScale = p_362733_.getHeadEatPositionScale(p_361157_);
		p_360515_.isSheared = p_362733_.isSheared();
		p_360515_.woolColor = p_362733_.getColor();
    }

	@Override
	public Identifier getTextureLocation(HornedSheepRenderState state) {
		if (state.isBaby) {
			if (!state.horn) {
				return TEXTURE_BABY_HORNLESS;
			}
			return TEXTURE_BABY;
		}
		if (!state.horn) {
            return TEXTURE_HORNLESS;
        }
		return TEXTURE;
	}
}