package com.akiotsukino.chocolateflavored.client.sunken;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.sunken.SunkenContent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class SunkenClient {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID,"sunken"),"main");
    @SubscribeEvent public static void renderer(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(SunkenContent.SUNKEN.get(),SunkenRenderer::new);}
    @SubscribeEvent public static void layer(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(LAYER,SunkenModel::layer);}
    private SunkenClient(){}
}
