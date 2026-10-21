package com.akiotsukino.chocolateflavored.mob;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.mob.entity.*;
import com.akiotsukino.chocolateflavored.mob.entity.projectile.*;
import com.akiotsukino.chocolateflavored.mob.block.CarvedMelonBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import java.util.Optional;

/** Internal projectiles and conversion forms register no additional items or spawn eggs. */
public final class MobContent {
 public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ChocolateFlavored.MODID);
 public static final DeferredHolder<EntityType<?>,EntityType<MelonGolem>> MELON_GOLEM = ENTITIES.registerEntityType("melon_golem",MelonGolem::new,MobCategory.MISC,b->b.sized(.7F,1.9F).immuneTo(Blocks.POWDER_SNOW).clientTrackingRange(8));
 public static final DeferredHolder<EntityType<?>,EntityType<BoulderingZombie>> BOULDERING_ZOMBIE = ENTITIES.registerEntityType("bouldering_zombie",BoulderingZombie::new,MobCategory.MONSTER,b->b.sized(.6F,1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).ridingOffset(-.7F).notInPeaceful().clientTrackingRange(8));
 public static final DeferredHolder<EntityType<?>,EntityType<Swamped>> SWAMPED = ENTITIES.registerEntityType("swamped",Swamped::new,MobCategory.MONSTER,b->b.sized(.6F,1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).ridingOffset(-.7F).notInPeaceful().clientTrackingRange(8));
 public static final DeferredHolder<EntityType<?>,EntityType<SkeletonWolf>> SKELETON_WOLF = ENTITIES.registerEntityType("skeleton_wolf",SkeletonWolf::new,MobCategory.MONSTER,b->b.sized(.6F,.85F).notInPeaceful().clientTrackingRange(8));
 public static final DeferredHolder<EntityType<?>,EntityType<BoneSpider>> BONE_SPIDER = ENTITIES.registerEntityType("bone_spider",BoneSpider::new,MobCategory.MONSTER,b->b.sized(1.4F,.9F).notInPeaceful().clientTrackingRange(8));
 public static final DeferredHolder<EntityType<?>,EntityType<TropicalSlime>> TROPICAL_SLIME = ENTITIES.registerEntityType("tropical_slime",TropicalSlime::new,MobCategory.MONSTER,b->b.sized(.52F,.52F).eyeHeight(.325F).notInPeaceful().clientTrackingRange(10));
 public static final DeferredHolder<EntityType<?>,EntityType<MelonSeed>> MELON_SEED = ENTITIES.registerEntityType("melon_seed",MelonSeed::new,MobCategory.MISC,b->b.sized(.25F,.25F).clientTrackingRange(4).updateInterval(10));
 public static final DeferredHolder<EntityType<?>,EntityType<BoneShard>> BONE_SHARD = ENTITIES.registerEntityType("bone_shard",BoneShard::new,MobCategory.MISC,b->b.sized(.25F,.25F).clientTrackingRange(4).updateInterval(10));
 public static final DeferredBlock<CarvedMelonBlock> CARVED_MELON = ModContent.BLOCKS.registerBlock("carved_melon",CarvedMelonBlock::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.MELON).overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE,id("blocks/carved_melon")))));
 public static final DeferredItem<BlockItem> CARVED_MELON_ITEM = ModContent.ITEMS.registerSimpleBlockItem(CARVED_MELON);
 public static final DeferredItem<MobBucketItem> TROPICAL_SLIME_BUCKET = ModContent.ITEMS.registerItem("tropical_slime_bucket",p->new MobBucketItem(TROPICAL_SLIME.get(),Fluids.WATER,SoundEvents.BUCKET_EMPTY_FISH,p),()->new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET));
 private static final DeferredRegister<EntityDataSerializer<?>> DATA = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS,ChocolateFlavored.MODID);
 public static final DeferredHolder<EntityDataSerializer<?>,EntityDataSerializer<PotionContents>> POTIONS = DATA.register("bone_spider_potions",()->EntityDataSerializer.forValueType(PotionContents.STREAM_CODEC));
 private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,ChocolateFlavored.MODID);
 public static final DeferredHolder<MobEffect,MobEffect> UNDEAD_BODY = EFFECTS.register("undead_body",()->new MobEffect(MobEffectCategory.NEUTRAL,0xffffff) {});
 public static final DeferredHolder<MobEffect,ZombifiedEffect> ZOMBIFIED = EFFECTS.register("zombified",ZombifiedEffect::new);
 private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE,ChocolateFlavored.MODID);
 public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,SimpleParticleType> TROPICAL_SPLASH = PARTICLES.register("tropical_splash",()->new SimpleParticleType(false));
 private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT,ChocolateFlavored.MODID);
 public static final DeferredHolder<SoundEvent,SoundEvent> SKELETON_WOLF_AMBIENT=sound("ambient"), SKELETON_WOLF_HURT=sound("hurt"), SKELETON_WOLF_DEATH=sound("death"), SKELETON_WOLF_GROWL=sound("growl"), SKELETON_WOLF_PANT=sound("pant"), SKELETON_WOLF_WHINE=sound("whine"), SKELETON_WOLF_STEP=sound("step"), SKELETON_WOLF_SHAKE=sound("shake");
 private static DeferredHolder<SoundEvent,SoundEvent> sound(String s){return SOUNDS.register("entity.skeleton_wolf."+s,()->SoundEvent.createVariableRangeEvent(id("entity.skeleton_wolf."+s)));}
 public static Identifier id(String p){return Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID,p);}
 public static void register(IEventBus bus){
  ENTITIES.register(bus); DATA.register(bus); EFFECTS.register(bus); PARTICLES.register(bus); SOUNDS.register(bus); MonsterBiomeModifier.register(bus);
  bus.addListener(MobContent::attributes); bus.addListener(MobContent::placements);
  NeoForge.EVENT_BUS.addListener(MonsterSpawns::join); NeoForge.EVENT_BUS.addListener(MelonCarving::interact);
 }
 private static void attributes(EntityAttributeCreationEvent e){
  e.put(MELON_GOLEM.get(),MelonGolem.createAttributes().build()); e.put(BOULDERING_ZOMBIE.get(),BoulderingZombie.createAttributes().build());
  e.put(SWAMPED.get(),Swamped.createAttributes().build()); e.put(SKELETON_WOLF.get(),SkeletonWolf.createAttributes().build()); e.put(BONE_SPIDER.get(),BoneSpider.createAttributes().build()); e.put(TROPICAL_SLIME.get(),Monster.createMonsterAttributes().build());
 }
 private static void placements(RegisterSpawnPlacementsEvent e){
  e.register(SKELETON_WOLF.get(),SpawnPlacementTypes.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,SkeletonWolf::checkSkeletonWolfSpawnRules,RegisterSpawnPlacementsEvent.Operation.REPLACE);
  e.register(TROPICAL_SLIME.get(),SpawnPlacementTypes.IN_WATER,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,TropicalSlime::checkTropicalSpawnRules,RegisterSpawnPlacementsEvent.Operation.REPLACE);
  e.register(SWAMPED.get(),SpawnPlacementTypes.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Monster::checkMonsterSpawnRules,RegisterSpawnPlacementsEvent.Operation.REPLACE);
 }
 private MobContent(){}
}
