// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.client.mob.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;


public class OuterLayer<T extends LivingEntityRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final Identifier location;
	private final EntityModel<T> model;
    private final EntityModel<T> babyModel;

    public OuterLayer(RenderLayerParent<T, M> p_174490_, Identifier location, EntityModel<T> entityModel, EntityModel<T> babyModel) {
		super(p_174490_);
        this.location = location;
        this.model = entityModel;
        this.babyModel = babyModel;
	}

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, T entityRenderState, float v, float v1) {
        EntityModel<T> drownedmodel = entityRenderState.isBaby ? this.babyModel : this.model;

        coloredCutoutModelCopyLayerRender(drownedmodel, this.location, poseStack, submitNodeCollector, i, entityRenderState, -1, 1);
    }
}