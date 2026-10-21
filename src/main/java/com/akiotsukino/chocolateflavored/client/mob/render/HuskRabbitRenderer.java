// SPDX-License-Identifier: LGPL-3.0-only
package com.akiotsukino.chocolateflavored.client.mob.render;


import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import com.akiotsukino.chocolateflavored.mob.entity.HuskRabbit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.rabbit.AdultRabbitModel;
import net.minecraft.client.model.animal.rabbit.BabyRabbitModel;
import net.minecraft.client.model.animal.rabbit.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;


public class HuskRabbitRenderer<T extends HuskRabbit> extends MobRenderer<T, RabbitRenderState, RabbitModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/zombified_rabbit/husk_rabbit.png");
    private static final Identifier BABY_TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/zombified_rabbit/husk_rabbit_baby.png");
    private final AdultAndBabyModelPair<RabbitModel> models;

    public HuskRabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultRabbitModel(context.bakeLayer(ModelLayers.RABBIT)), 0.3F);
        this.models = bakeModels(context);
    }

    private static AdultAndBabyModelPair<RabbitModel> bakeModels(EntityRendererProvider.Context context) {
        return new AdultAndBabyModelPair<>(
                new AdultRabbitModel(context.bakeLayer(ModelLayers.RABBIT)), new BabyRabbitModel(context.bakeLayer(ModelLayers.RABBIT_BABY))
        );
    }


    @Override
    public void submit(RabbitRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.model = this.models.getModel(state.isBaby);
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public Identifier getTextureLocation(RabbitRenderState renderState) {
        if (renderState.isBaby) {
            return BABY_TEXTURE;
        }
        return TEXTURE;
    }

    @Override
    public RabbitRenderState createRenderState() {
        return new RabbitRenderState();
    }

    @Override
    public void extractRenderState(T entity, RabbitRenderState state, float p_365470_) {
        super.extractRenderState(entity, state, p_365470_);
        state.jumpCompletion = entity.getJumpCompletion(p_365470_);
        state.isToast = "Toast".equals(ChatFormatting.stripFormatting(entity.getName().getString()));
        state.variant = entity.getVariant();
        state.hopAnimationState.copyFrom(entity.hopAnimationState);
        state.idleHeadTiltAnimationState.copyFrom(entity.idleHeadTiltAnimationState);
    }

    @Override
    protected boolean isShaking(RabbitRenderState p_115304_) {
        return super.isShaking(p_115304_);
    }
}