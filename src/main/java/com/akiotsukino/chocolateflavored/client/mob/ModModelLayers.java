package com.akiotsukino.chocolateflavored.client.mob;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.client.model.geom.ModelLayerLocation;
public final class ModModelLayers {
 public static final ModelLayerLocation BOULDERING_ZOMBIE=new ModelLayerLocation(MobContent.id("bouldering_zombie"),"main");
 public static final ModelLayerLocation BOULDERING_ZOMBIE_BABY=new ModelLayerLocation(MobContent.id("bouldering_zombie_baby"),"main");
 public static final ModelLayerLocation SWAMPED=new ModelLayerLocation(MobContent.id("swamped"),"main");
 public static final ModelLayerLocation SWAMPED_BABY=new ModelLayerLocation(MobContent.id("swamped_baby"),"main");
 public static final ModelLayerLocation BONE_SPIDER=new ModelLayerLocation(MobContent.id("bone_spider"),"main");
 public static final ModelLayerLocation SKELETON_WOLF=new ModelLayerLocation(MobContent.id("skeleton_wolf"),"main");
 public static final ModelLayerLocation SKELETON_WOLF_BABY=new ModelLayerLocation(MobContent.id("skeleton_wolf_baby"),"main");
 public static final ModelLayerLocation TROPICAL_SLIME=new ModelLayerLocation(MobContent.id("tropical_slime"),"main");
 public static final ModelLayerLocation TROPICAL_SLIME_STABLE=new ModelLayerLocation(MobContent.id("tropical_slime_stable"),"main");
 public static final ModelLayerLocation TROPICAL_SLIME_OUTER=new ModelLayerLocation(MobContent.id("tropical_slime_outer"),"main");
 public static final net.minecraft.client.renderer.entity.ArmorModelSet<ModelLayerLocation> BABY_ARMOR=new net.minecraft.client.renderer.entity.ArmorModelSet<>(armor("head"),armor("chest"),armor("legs"),armor("feet"));
 private static ModelLayerLocation armor(String slot){return new ModelLayerLocation(MobContent.id("legacy_baby_armor_"+slot),"main");}
 private ModModelLayers(){}
}
