// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.client.mob.render.layer;

import com.akiotsukino.chocolateflavored.client.mob.render.TropicalSlimeRenderer;
import com.akiotsukino.chocolateflavored.client.mob.render.state.TropicalSlimeRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class TropicalSlimeOuterLayer extends RenderLayer<TropicalSlimeRenderState, SlimeModel> {
    private final SlimeModel model;

    public TropicalSlimeOuterLayer(RenderLayerParent<TropicalSlimeRenderState, SlimeModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, TropicalSlimeRenderState tropicalSlimeRenderState, float v, float v1) {
        boolean flag = tropicalSlimeRenderState.appearsGlowing() && tropicalSlimeRenderState.isInvisible;
        if (!tropicalSlimeRenderState.isInvisible || flag) {
            int i2 = LivingEntityRenderer.getOverlayCoords(tropicalSlimeRenderState, 0.0F);
            if (flag) {
                submitNodeCollector.order(1)
                        .submitModel(
                                this.model,
                                tropicalSlimeRenderState,
                                poseStack,
                                RenderTypes.outline(TropicalSlimeRenderer.SLIME_OUTER_LOCATION),
                                i,
                                i2,
                                -1,
                                null,
                                tropicalSlimeRenderState.outlineColor,
                                null
                        );
            } else {
                submitNodeCollector.order(1)
                        .submitModel(
                                this.model,
                                tropicalSlimeRenderState,
                                poseStack,
                                RenderTypes.entityTranslucent(TropicalSlimeRenderer.SLIME_OUTER_LOCATION),
                                i,
                                i2,
                                -1,
                                null,
                                tropicalSlimeRenderState.outlineColor,
                                null
                        );
            }
        }
    }
}
