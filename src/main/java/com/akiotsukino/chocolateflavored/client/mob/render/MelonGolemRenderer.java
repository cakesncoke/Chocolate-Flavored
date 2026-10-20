// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.client.mob.render;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.mob.render.layer.MelonGolemHeadLayer;
import com.akiotsukino.chocolateflavored.client.mob.render.state.MelonGolemRenderState;
import com.akiotsukino.chocolateflavored.mob.entity.MelonGolem;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;


public class MelonGolemRenderer extends MobRenderer<MelonGolem, MelonGolemRenderState, SnowGolemModel> {
    private static final Identifier MELON_GOLEM_LOCATION = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/melon_golem/melon_golem.png");
    private static final Identifier ANGRY_GOLEM_LOCATION = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/melon_golem/melon_golem_angry.png");
    private final BlockModelResolver blockModelResolver;
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    public MelonGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowGolemModel(context.bakeLayer(ModelLayers.SNOW_GOLEM)), 0.5F);
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new MelonGolemHeadLayer(this));
    }

    @Override
    public MelonGolemRenderState createRenderState() {
        return new MelonGolemRenderState();
    }

    @Override
    public void extractRenderState(MelonGolem entity, MelonGolemRenderState state, float p_364064_) {
        super.extractRenderState(entity, state, p_364064_);
        if (entity.hasMelon()) {
            if (entity.isAggressive()) {
                this.blockModelResolver.update(state.headBlock, MobContent.CARVED_MELON.get().defaultBlockState().setValue(com.akiotsukino.chocolateflavored.mob.block.CarvedMelonBlock.SHOOTING,true), BLOCK_DISPLAY_CONTEXT);
            } else {
                this.blockModelResolver.update(state.headBlock, MobContent.CARVED_MELON.get().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
            }
        } else {
            state.headBlock.clear();
        }
        state.aggressive = entity.isAggressive();
	}

    @Override
    public Identifier getTextureLocation(MelonGolemRenderState renderState) {
        if (renderState.aggressive) {
            return ANGRY_GOLEM_LOCATION;
        }
        return MELON_GOLEM_LOCATION;
	}
}