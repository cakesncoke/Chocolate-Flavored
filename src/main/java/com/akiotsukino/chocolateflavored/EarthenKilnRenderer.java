package com.akiotsukino.chocolateflavored;

import java.util.ArrayList;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.CampfireRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** Draws the kiln's six cooking items in two rows of three, reusing the campfire's render state. */
public final class EarthenKilnRenderer implements BlockEntityRenderer<EarthenKilnBlockEntity, CampfireRenderState> {
    private final ItemModelResolver resolver;

    public EarthenKilnRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.itemModelResolver();
    }

    @Override
    public CampfireRenderState createRenderState() {
        return new CampfireRenderState();
    }

    @Override
    public void extractRenderState(EarthenKilnBlockEntity kiln, CampfireRenderState state, float partialTick, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(kiln, state, partialTick, camera, overlay);
        state.lightCoords = LightCoordsUtil.getLightCoords(kiln.getLevel(), kiln.getBlockPos().above());
        state.facing = kiln.getBlockState().getValue(EarthenKilnBlock.FACING);
        state.items = new ArrayList<>();
        for (int slot = 0; slot < 6; slot++) {
            var item = new ItemStackRenderState();
            resolver.updateForTopItem(item, kiln.items().get(slot), ItemDisplayContext.FIXED, kiln.getLevel(), null,
                    (int) kiln.getBlockPos().asLong() + slot);
            state.items.add(item);
        }
    }

    @Override
    public void submit(CampfireRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int slot = 0; slot < state.items.size(); slot++) {
            var item = state.items.get(slot);
            if (item.isEmpty()) {
                continue;
            }
            poses.pushPose();
            poses.translate(0.5F, 1.02F, 0.5F);
            poses.rotate(Axis.YP.rotationDegrees(-state.facing.getOpposite().toYRot()));
            poses.rotate(Axis.XP.rotationDegrees(90));
            poses.translate(0.3F - slot % 3 * 0.3F, slot < 3 ? 0.2F : -0.2F, 0);
            poses.scale(0.375F, 0.375F, 0.375F);
            item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
    }
}
