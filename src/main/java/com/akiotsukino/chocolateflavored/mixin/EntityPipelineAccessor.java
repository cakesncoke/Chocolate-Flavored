package com.akiotsukino.chocolateflavored.mixin;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(RenderPipelines.class)
public interface EntityPipelineAccessor {
 @Accessor("ENTITY_SNIPPET") static RenderPipeline.Snippet chocolateflavored$entitySnippet(){throw new AssertionError();}
}
