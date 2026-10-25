package com.akiotsukino.chocolateflavored.client.trim;

import java.util.List;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mixin.EntityPipelineAccessor;
import com.akiotsukino.chocolateflavored.mixin.TrimRenderTypeInvoker;
import com.akiotsukino.chocolateflavored.trim.*;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class TrimOutlines {
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(EntityPipelineAccessor.chocolateflavored$linesSnippet())
            .withLocation(TrimAbilities.id("pipeline/eye_outline"))
            .withFragmentShader(TrimAbilities.id("core/eye_outline"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final RenderType LINES = TrimRenderTypeInvoker.chocolateflavored$create("chocolateflavored_eye_outline", RenderSetup.builder(PIPELINE).createRenderSetup());
    private static final ContextKey<List<Vec3>> OUTLINES = new ContextKey<>(TrimAbilities.id("eye_outlines"));
    private static List<BlockPos> positions = List.of();
    private static ClientLevel world;
    private static long until;
    private static boolean archaeology;

    @SubscribeEvent public static void pipelines(RegisterRenderPipelinesEvent event) { event.registerPipeline(PIPELINE); }
    @SubscribeEvent public static void payloads(RegisterClientPayloadHandlersEvent event) {
        event.register(TrimOutlinePayload.TYPE, (payload, context) -> {
            if (payload.positions().isEmpty() && payload.archaeology() != archaeology) return;
            archaeology = payload.archaeology();
            world = Minecraft.getInstance().level;
            positions = payload.positions();
            until = world == null ? 0 : world.getGameTime() + payload.durationTicks();
        });
    }
    @SubscribeEvent public static void extract(ExtractLevelRenderStateEvent event) {
        var player = Minecraft.getInstance().player;
        // The server invalidates unequipped sets. Client equipment packets can arrive after
        // the reveal packet; discarding it here would permanently lose a valid reveal.
        if (event.getLevel() != world || world.getGameTime() >= until || player == null || !player.isAlive()) {
            positions = List.of();
        }
        Vec3 camera = event.getRenderState().cameraRenderState.pos;
        // Rendering consumes an immutable camera-relative snapshot, never the live level.
        event.getRenderState().setRenderData(OUTLINES, positions.stream()
                .filter(pos -> archaeology ? event.getLevel().getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BrushableBlockEntity : event.getLevel().getBlockState(pos).is(TrimAbilities.ORES))
                .map(pos -> Vec3.atLowerCornerOf(pos).subtract(camera)).toList());
    }
    @SubscribeEvent public static void submit(SubmitCustomGeometryEvent event) {
        List<Vec3> snapshot = event.getLevelRenderState().getRenderData(OUTLINES);
        if (snapshot == null || snapshot.isEmpty()) return;
        event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(), LINES, (pose, vertices) -> {
            // Broad translucent halo, warm inner glow, then a bright emissive core.
            // Only line vertices are emitted: terrain models and faces remain untouched.
            int[] colors = {0x18FFD34D, 0x38FFCC33, 0x90FFD633, 0xFFFFF3AD};
            float[] widths = {12, 8, 5, 3.5f};
            for (int layer = 0; layer < colors.length; layer++) {
                final int color = colors[layer];
                final float width = widths[layer];
            for (Vec3 offset : snapshot) Shapes.block().forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                Vector3f normal = new Vector3f((float)(x2-x1), (float)(y2-y1), (float)(z2-z1)).normalize();
                vertices.addVertex(pose, (float)(x1+offset.x), (float)(y1+offset.y), (float)(z1+offset.z)).setColor(color).setNormal(pose, normal).setLineWidth(width);
                vertices.addVertex(pose, (float)(x2+offset.x), (float)(y2+offset.y), (float)(z2+offset.z)).setColor(color).setNormal(pose, normal).setLineWidth(width);
            });
            }
        });
    }
    private TrimOutlines() {}
}
