// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs, revision d5acab9846bd3400ac5f093bfec7439351a1bd61 (26.1.2).
package com.akiotsukino.chocolateflavored.client.animal.render.layer;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.animal.ModModelLayers;
import com.akiotsukino.chocolateflavored.client.animal.model.MuddyPigModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.pig.BabyPigModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;


public class MuddyPigMudLayer<T extends LivingEntityRenderState, S extends EntityModel<T>> extends RenderLayer<T, S> {
    private static final Identifier MUD_LOCATION = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/muddypig/muddy_pig.png");
    private static final Identifier MUD_BABY_LOCATION = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/muddypig/muddy_pig_baby.png");
    private static final Identifier DRY_MUD_LOCATION = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/muddypig/dry_muddy_pig.png");

    public static final ContextKey<Boolean> IS_MUD = new ContextKey<>(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "mud"));
    public static final ContextKey<Boolean> IS_SHEARED = new ContextKey<>(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "sheared"));

    private final PigModel model;
    private final PigModel babyModel;

    public MuddyPigMudLayer(RenderLayerParent<T, S> p_174533_, EntityModelSet p_174534_) {
		super(p_174533_);
        this.model = new MuddyPigModel(p_174534_.bakeLayer(ModModelLayers.MUDDY_PIG));
        this.babyModel = new BabyPigModel(p_174534_.bakeLayer(ModModelLayers.MUDDY_PIG_BABY));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, T entityRenderState, float v, float v1) {
        boolean mud = entityRenderState.getRenderDataOrDefault(IS_MUD, false);
        PigModel pigModel = entityRenderState.isBaby ? this.babyModel : this.model;

        if (mud) {
            if (entityRenderState.isInvisible) {
                if (entityRenderState.appearsGlowing()) {
                    submitNodeCollector.submitModel(pigModel, entityRenderState, poseStack, RenderTypes.outline(entityRenderState.isBaby ? MUD_BABY_LOCATION : MUD_LOCATION), i, LivingEntityRenderer.getOverlayCoords(entityRenderState, 0.0F), -16777216, (TextureAtlasSprite) null, entityRenderState.outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);
                }
            } else {
                coloredCutoutModelCopyLayerRender(pigModel, entityRenderState.isBaby ? MUD_BABY_LOCATION : MUD_LOCATION, poseStack, submitNodeCollector, i, entityRenderState, -1, 1);
            }
        }
    }
}