package com.akiotsukino.chocolateflavored.mob;
import com.akiotsukino.chocolateflavored.mob.entity.BoneSpider;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
/** Replacement-only spawns, with one persisted roll and no duplicated equipment or jockey generation. */
public final class MonsterSpawns {
 private static final String PENDING="chocolateflavored:monster_spawn_pending", CHECKED="chocolateflavored:monster_spawn_checked";
 public static EntityType<?> select(EntityType<?> original,int y,Holder<Biome> biome,RandomSource random){
  if(original==EntityType.ZOMBIE&&(y<0||(biome.is(Tags.Biomes.IS_MOUNTAIN)&&random.nextInt(100)<10)))return MobContent.BOULDERING_ZOMBIE.get();
  if(original==EntityType.SPIDER){int chance=biome.is(Biomes.DARK_FOREST)||biome.is(Biomes.PALE_GARDEN)?33:y<0?25:0;if(chance>0&&random.nextInt(100)<chance)return MobContent.BONE_SPIDER.get();}
  return null;
 }
 public static void join(EntityJoinLevelEvent event){
  if(!(event.getLevel() instanceof ServerLevel level)||!(event.getEntity() instanceof Mob original)||(original.getType()!=EntityType.ZOMBIE&&original.getType()!=EntityType.SPIDER))return;
  if(event.loadedFromDisk()&&!original.getPersistentData().getBooleanOr(PENDING,false))return;
  original.getPersistentData().remove(PENDING);
  EntitySpawnReason reason=original.getSpawnType();
  if((reason!=EntitySpawnReason.NATURAL&&reason!=EntitySpawnReason.CHUNK_GENERATION)||original.getPersistentData().getBooleanOr(CHECKED,false))return;
  original.getPersistentData().putBoolean(CHECKED,true);
  EntityType<?> type=select(original.getType(),original.blockPosition().getY(),level.getBiome(original.blockPosition()),original.getRandom());
  if(type==null)return;
  Mob replacement=(Mob)type.create(level,reason); if(replacement==null)return;
  replacement.snapTo(original.getX(),original.getY(),original.getZ(),original.getYRot(),original.getXRot());
  replacement.getAttributes().assignPermanentModifiers(original.getAttributes());
  if(original instanceof Zombie)replacement.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(original.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.SPAWN_REINFORCEMENTS_CHANCE));
  replacement.setBaby(original.isBaby()); replacement.setCanPickUpLoot(original.canPickUpLoot());
  replacement.setCustomName(original.getCustomName()); replacement.setCustomNameVisible(original.isCustomNameVisible());
  if(original.isPersistenceRequired())replacement.setPersistenceRequired();
  replacement.setLeftHanded(original.isLeftHanded()); replacement.setInvulnerable(original.isInvulnerable());
  for(EquipmentSlot slot:EquipmentSlot.values()){replacement.setItemSlot(slot,original.getItemBySlot(slot).copy());replacement.setDropChance(slot,original.getDropChances().byEquipment(slot));}
  for(MobEffectInstance effect:original.getActiveEffects())replacement.addEffect(new MobEffectInstance(effect));
  if(original instanceof Zombie z&&replacement instanceof Zombie rz)rz.setCanBreakDoors(z.canBreakDoors());
  if(replacement instanceof BoneSpider spider)spider.rollPotion(level.getRandom());
  replacement.setHealth(replacement.getMaxHealth());
  replacement.getPersistentData().putBoolean(CHECKED,true);
  if(level.addFreshEntity(replacement)){
   Entity vehicle=original.getVehicle(); if(vehicle!=null){original.stopRiding();replacement.startRiding(vehicle,true,false);}
   for(Entity passenger:java.util.List.copyOf(original.getPassengers())){passenger.stopRiding();passenger.startRiding(replacement,true,false);}
   event.setCanceled(true);
  }
 }
 private MonsterSpawns(){}
}
