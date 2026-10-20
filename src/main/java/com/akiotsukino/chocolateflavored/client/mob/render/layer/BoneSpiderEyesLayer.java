// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.client.mob.render.layer;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.mob.render.state.BoneSpiderRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class BoneSpiderEyesLayer<T extends BoneSpiderRenderState> extends EyesLayer<T, EntityModel<T>> {
    private static final RenderType SPIDER_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/bone_spider/bone_spider_eye.png"));
    private static final RenderType COLOR_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/bone_spider/bone_spider_color_eye.png"));

	public BoneSpiderEyesLayer(RenderLayerParent<T, EntityModel<T>> p_117507_) {
		super(p_117507_);
	}

    @Override
    public void submit(PoseStack p_433452_, SubmitNodeCollector p_433171_, int p_434650_, T p_435883_, float p_433542_, float p_435619_) {
        if (p_435883_.potionColor == -1) {
            super.submit(p_433452_, p_433171_, p_434650_, p_435883_, p_433542_, p_435619_);
        } else {
            p_433171_.order(1)
                    .submitModel(
                            this.getParentModel(), p_435883_, p_433452_, COLOR_EYES, p_434650_, OverlayTexture.NO_OVERLAY, p_435883_.potionColor, null, p_435883_.outlineColor, null
                    );
        }
    }

    public RenderType renderType() {
		return SPIDER_EYES;
	}
}