package com.akiotsukino.chocolateflavored.client.dye;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Native boat rendering, including rowing, damage tilt, leashes and the water mask. */
public final class PastelBoatRenderer extends BoatRenderer {
    public final int color;
    public PastelBoatRenderer(EntityRendererProvider.Context context, int color) {
        super(context, ModelLayers.PALE_OAK_BOAT);
        this.color = color;
    }
}
