package com.akiotsukino.chocolateflavored.client.tuff;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.tuff.TuffGolemContent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class TuffGolemClient {
    static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.fromNamespaceAndPath("chocolateflavored","tuff_golem"),"main");
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(TuffGolemContent.TUFF_GOLEM.get(),TuffGolemRenderer::new);}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(LAYER,TuffGolemModel::layer);}
    private TuffGolemClient(){}
}
