package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.cooking.MudKilnBlock;
import com.akiotsukino.chocolateflavored.cooking.MudKilnBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.CampfireRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Uses vanilla 26.1 extraction/submission, with the six-position cooking layout. */
public final class MudKilnRenderer implements BlockEntityRenderer<MudKilnBlockEntity, CampfireRenderState> {
    private final ItemModelResolver resolver;
    public MudKilnRenderer(BlockEntityRendererProvider.Context context) { resolver = context.itemModelResolver(); }
    @Override public CampfireRenderState createRenderState() { return new CampfireRenderState(); }
    @Override public void extractRenderState(MudKilnBlockEntity mud_kiln, CampfireRenderState state, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(mud_kiln, state, partialTick, camera, overlay);
        state.lightCoords = net.minecraft.client.renderer.LevelRenderer.getLightCoords(mud_kiln.getLevel(), mud_kiln.getBlockPos().above());
        state.facing = mud_kiln.getBlockState().getValue(MudKilnBlock.FACING); state.items = new ArrayList<>();
        for (int slot = 0; slot < 6; slot++) {
            var item = new ItemStackRenderState();
            resolver.updateForTopItem(item, mud_kiln.items().get(slot), ItemDisplayContext.FIXED, mud_kiln.getLevel(), null, (int)mud_kiln.getBlockPos().asLong() + slot);
            state.items.add(item);
        }
    }
    @Override public void submit(CampfireRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int slot = 0; slot < state.items.size(); slot++) {
            var item = state.items.get(slot); if (item.isEmpty()) continue;
            poses.pushPose(); poses.translate(0.5F, 1.02F, 0.5F);
            poses.mulPose(Axis.YP.rotationDegrees(-state.facing.getOpposite().toYRot()));
            poses.mulPose(Axis.XP.rotationDegrees(90));
            poses.translate(0.3F - slot % 3 * 0.3F, slot < 3 ? 0.2F : -0.2F, 0);
            poses.scale(0.375F, 0.375F, 0.375F);
            item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0); poses.popPose();
        }
    }
}
