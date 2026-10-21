package com.akiotsukino.chocolateflavored.client.expansion;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.expansion.ExpansionContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class ExpansionClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(ExpansionContent.SUNKEN_SKELETON.get(),SunkenSkeletonRenderer::new);
        e.registerEntityRenderer(ExpansionContent.TUFF_GOLEM.get(),TuffGolemRenderer::new);
        e.registerEntityRenderer(ExpansionContent.BARNACLE.get(),c->new CreatureRenderer<>(c,"barnacle"));
        e.registerEntityRenderer(ExpansionContent.ICEOLOGER.get(),c->new CreatureRenderer<>(c,"iceologer"));
        e.registerEntityRenderer(ExpansionContent.WILDFIRE.get(),c->new CreatureRenderer<>(c,"wildfire"));
        e.registerEntityRenderer(ExpansionContent.ICE_CHUNK.get(),IceChunkRenderer::new);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){
        e.registerLayerDefinition(CreatureRenderer.layer("sunken_skeleton"),SunkenSkeletonMesh::createBodyLayer);
        e.registerLayerDefinition(CreatureRenderer.layer("tuff_golem"),()->TuffGolemModel.layer(0));
        e.registerLayerDefinition(CreatureRenderer.layer("tuff_golem_eyes"),()->TuffGolemModel.layer(1));
        e.registerLayerDefinition(CreatureRenderer.layer("tuff_golem_cloak"),()->TuffGolemModel.layer(2));
        e.registerLayerDefinition(CreatureRenderer.layer("iceologer"),NativeCreatureModel::iceologerLayer);
        e.registerLayerDefinition(CreatureRenderer.layer("wildfire"),NativeCreatureModel::wildfireLayer);
        e.registerLayerDefinition(CreatureRenderer.layer("barnacle"),BarnacleMesh::createBodyLayer);
    }
    private ExpansionClient(){}
}
