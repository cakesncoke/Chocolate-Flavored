package com.akiotsukino.chocolateflavored.client.mob;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import com.akiotsukino.chocolateflavored.client.mob.model.*;
import com.akiotsukino.chocolateflavored.client.mob.render.*;
import com.akiotsukino.chocolateflavored.client.mob.render.zombie.*;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class MobClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
  e.registerEntityRenderer(MobContent.MELON_GOLEM.get(),MelonGolemRenderer::new);
  e.registerEntityRenderer(MobContent.BOULDERING_ZOMBIE.get(),BoulderingZombieRenderer::new);
  e.registerEntityRenderer(MobContent.BOULDERING_DROWNED.get(),BoulderingDrownedRenderer::new);
  e.registerEntityRenderer(MobContent.SWAMPED.get(),SwampedRenderer::new);
  e.registerEntityRenderer(MobContent.BONE_SPIDER.get(),BoneSpiderRender::new);
  e.registerEntityRenderer(MobContent.SKELETON_WOLF.get(),SkeletonWolfRenderer::new);
  e.registerEntityRenderer(MobContent.TROPICAL_SLIME.get(),TropicalSlimeRenderer::new);
  e.registerEntityRenderer(MobContent.MELON_SEED.get(),ThrownItemRenderer::new);
  e.registerEntityRenderer(MobContent.BONE_SHARD.get(),ThrownItemRenderer::new);
 }
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){
  var baby=net.minecraft.client.model.HumanoidModel.BABY_TRANSFORMER;
  var armor=net.minecraft.client.model.HumanoidModel.createArmorMeshSet(new CubeDeformation(.5F),new CubeDeformation(1));
  e.registerLayerDefinition(ModModelLayers.BABY_ARMOR.head(),()->net.minecraft.client.model.geom.builders.LayerDefinition.create(armor.head(),64,32).apply(baby));
  e.registerLayerDefinition(ModModelLayers.BABY_ARMOR.chest(),()->net.minecraft.client.model.geom.builders.LayerDefinition.create(armor.chest(),64,32).apply(baby));
  e.registerLayerDefinition(ModModelLayers.BABY_ARMOR.legs(),()->net.minecraft.client.model.geom.builders.LayerDefinition.create(armor.legs(),64,32).apply(baby));
  e.registerLayerDefinition(ModModelLayers.BABY_ARMOR.feet(),()->net.minecraft.client.model.geom.builders.LayerDefinition.create(armor.feet(),64,32).apply(baby));
  e.registerLayerDefinition(ModModelLayers.BOULDERING_ZOMBIE,AbstractBoulderingZombieModel::createBodyLayer);
  e.registerLayerDefinition(ModModelLayers.BOULDERING_ZOMBIE_BABY,()->AbstractBoulderingZombieModel.createBodyLayer().apply(baby));
  e.registerLayerDefinition(ModModelLayers.BOULDERING_DROWNED,()->BoulderingDrownedModel.createBodyLayer(CubeDeformation.NONE));
  e.registerLayerDefinition(ModModelLayers.BOULDERING_DROWNED_BABY,()->BoulderingDrownedModel.createBodyLayer(CubeDeformation.NONE).apply(baby));
  e.registerLayerDefinition(ModModelLayers.BOULDERING_DROWNED_OUTER,()->BoulderingDrownedModel.createBodyLayer(new CubeDeformation(.25F)));
  e.registerLayerDefinition(ModModelLayers.BOULDERING_DROWNED_OUTER_BABY,()->BoulderingDrownedModel.createBodyLayer(new CubeDeformation(.25F)).apply(baby));
  e.registerLayerDefinition(ModModelLayers.SWAMPED,AbstractLobberZombieModel::createBodyLayer);
  e.registerLayerDefinition(ModModelLayers.SWAMPED_BABY,()->AbstractLobberZombieModel.createBodyLayer().apply(baby));
  e.registerLayerDefinition(ModModelLayers.BONE_SPIDER,BoneSpiderModel::createBodyLayer);
  e.registerLayerDefinition(ModModelLayers.SKELETON_WOLF,SkeletonWolfRenderer::createBodyLayer);
  e.registerLayerDefinition(ModModelLayers.SKELETON_WOLF_BABY,()->SkeletonWolfRenderer.createBodyLayer().apply(SkeletonWolfRenderer.BABY_TRANSFORMER));
  e.registerLayerDefinition(ModModelLayers.TROPICAL_SLIME,TropicalSlimeRenderer::createInnerBodyLayer);
  e.registerLayerDefinition(ModModelLayers.TROPICAL_SLIME_STABLE,TropicalSlimeRenderer::createStableInnerBodyLayer);
 }
 @SubscribeEvent public static void pipeline(RegisterRenderPipelinesEvent e){e.registerPipeline(SlimeRenderTypes.PIPELINE);}
 @SubscribeEvent public static void particles(RegisterParticleProvidersEvent e){e.registerSpriteSet(MobContent.TROPICAL_SPLASH.get(),TropicalSplashParticle.Provider::new);}
 private MobClient(){}
}
