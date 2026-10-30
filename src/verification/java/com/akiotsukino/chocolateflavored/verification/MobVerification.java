package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.mob.*;
import com.akiotsukino.chocolateflavored.mob.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import java.util.*;
public final class MobVerification {
 public static void run(ServerLevel level,FakePlayer player){
  spawning(level); replacement(level);
  FakePlayer combat=new CombatPlayer(level);interactions(level,combat);projectilesAndConversion(level,combat);
  ChocolateFlavored.LOGGER.info("MOB_VERIFICATION_PASSED: replacement boundaries and state, 1.5% wolf weights, golem shearing/building, climbing, poison, bone payloads, bucket persistence and fish release");
 }
 private static void spawning(ServerLevel level){
  check(!MobContent.MELON_GOLEM.get().isBlockDangerous(Blocks.POWDER_SNOW.defaultBlockState()),"Golem powder-snow immunity tag loads");
  check(MobContent.MELON_GOLEM.get().isBlockDangerous(Blocks.CACTUS.defaultBlockState()),"Golem immunity does not include unrelated hazards");
  check(MobContent.MELON_GOLEM.get().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.FALL_DAMAGE_IMMUNE)&&MobContent.MELON_GOLEM.get().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES),"Golem retains reference fall/freeze immunity");
  var registry=level.registryAccess().lookupOrThrow(Registries.BIOME);var plains=registry.getOrThrow(Biomes.PLAINS);var peaks=registry.getOrThrow(Biomes.JAGGED_PEAKS);
  check(MonsterSpawns.select(EntityTypes.ZOMBIE,-1,plains,new Roll(99))==MobContent.BOULDERING_ZOMBIE.get(),"Every zombie below zero");
  check(MonsterSpawns.select(EntityTypes.ZOMBIE,0,plains,new Roll(0))==null,"No plains zombies at zero");
  check(MonsterSpawns.select(EntityTypes.ZOMBIE,0,peaks,new Roll(9))==MobContent.BOULDERING_ZOMBIE.get(),"Mountain roll 9");
  check(MonsterSpawns.select(EntityTypes.ZOMBIE,64,peaks,new Roll(10))==null,"Mountain roll 10");
  check(MonsterSpawns.select(EntityTypes.ZOMBIE_VILLAGER,-1,peaks,new Roll(0))==null,"Do not replace zombie villagers");
  for(var biome:List.of(plains,registry.getOrThrow(Biomes.DARK_FOREST),registry.getOrThrow(Biomes.PALE_GARDEN))){
   int chance=biome==plains?25:33;
   check(MonsterSpawns.select(EntityTypes.SPIDER,-1,biome,new Roll(chance-1))==MobContent.BONE_SPIDER.get(),"Spider threshold included");
   check(MonsterSpawns.select(EntityTypes.SPIDER,-1,biome,new Roll(chance))==null,"Spider threshold excluded; overlapping conditions don't stack");
   check(MonsterSpawns.select(EntityTypes.SPIDER,0,biome,new Roll(0))==(biome==plains?null:MobContent.BONE_SPIDER.get()),"Spider surface habitats");
  }
  var entries=List.of(new Weighted<>(new SpawnerData(EntityTypes.ZOMBIE,4,4),99),new Weighted<>(new SpawnerData(EntityTypes.ZOMBIE_VILLAGER,1,1),1),new Weighted<>(new SpawnerData(EntityTypes.SPIDER,4,4),100));
  var adjusted=MonsterBiomeModifier.withSkeletonWolves(entries,3,4);long total=adjusted.stream().mapToLong(Weighted::weight).sum();long wolf=adjusted.stream().filter(e->e.value().type()==MobContent.SKELETON_WOLF.get()).mapToLong(Weighted::weight).sum();
  check(wolf*1000==total*15,"Exact 1.5 percent wolves");
  long z=adjusted.stream().filter(e->e.value().type()==EntityTypes.ZOMBIE).mapToLong(Weighted::weight).sum();long zv=adjusted.stream().filter(e->e.value().type()==EntityTypes.ZOMBIE_VILLAGER).mapToLong(Weighted::weight).sum();check(z==99*zv,"Existing zombie-villager ratio preserved");
  var actual=registry.getOrThrow(Biomes.FOREST).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
  long at=actual.stream().mapToLong(Weighted::weight).sum(),aw=actual.stream().filter(e->e.value().type()==MobContent.SKELETON_WOLF.get()).mapToLong(Weighted::weight).sum();check(aw*1000==at*15,"Actual loaded forest biome weights");
 }
 private static void replacement(ServerLevel level){
  BlockPos pos=new BlockPos(80,-20,80);level.getChunkAt(pos);
  List<BoulderingZombie> joined=new ArrayList<>();java.util.function.Consumer<EntityJoinLevelEvent> observer=e->{if(e.getEntity() instanceof BoulderingZombie z&&e.getLevel()==level)joined.add(z);};NeoForge.EVENT_BUS.addListener(observer);
  try{
   for(var reason:List.of(EntitySpawnReason.NATURAL,EntitySpawnReason.CHUNK_GENERATION)){
    joined.clear();Zombie zombie=EntityTypes.ZOMBIE.create(level,reason);zombie.snapTo(80.5,-20,80.5,30,0);zombie.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),reason,null);zombie.setBaby(true);zombie.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_AXE));zombie.setDropChance(EquipmentSlot.MAINHAND,.75F);zombie.setPersistenceRequired();
    var chicken=EntityTypes.CHICKEN.create(level,EntitySpawnReason.JOCKEY);chicken.snapTo(80.5,-20,80.5,0,0);zombie.stopRiding();zombie.startRiding(chicken,true,false);
    if(reason==EntitySpawnReason.NATURAL)check(!level.addFreshEntity(zombie),"Natural spawn replaced");
    else {var loaded=(Mob)restore(level,zombie);var event=new EntityJoinLevelEvent(loaded,level,true);MonsterSpawns.join(event);check(event.isCanceled(),"Protochunk persisted spawn marker");}
    check(joined.size()==1,"Exactly one replacement");var result=joined.getFirst();check(result.isBaby()&&result.getMainHandItem().is(Items.IRON_AXE)&&result.getDropChances().byEquipment(EquipmentSlot.MAINHAND)==.75F&&result.isPersistenceRequired(),"Age, gear, drop chances and persistence copied");
    if(reason==EntitySpawnReason.NATURAL)check(result.getVehicle()==chicken,"Jockey vehicle transferred");
    result.setClimbing(true);check(result.onClimbable(),"Wall climbing flag");result.discard();chicken.discard();
   }
   Zombie commanded=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);commanded.snapTo(80.5,-20,80.5,0,0);commanded.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.COMMAND,null);var event=new EntityJoinLevelEvent(commanded,level,false);MonsterSpawns.join(event);check(!event.isCanceled(),"Commanded zombies unchanged");
   var loaded=restore(level,commanded);var reload=new EntityJoinLevelEvent(loaded,level,true);MonsterSpawns.join(reload);check(!reload.isCanceled(),"Ordinary reload does not reroll");
  }finally{NeoForge.EVENT_BUS.unregister(observer);}
 }
 private static void interactions(ServerLevel level,FakePlayer player){
  level.getServer().setDifficulty(Difficulty.HARD,true);
  for(boolean angry:new boolean[]{false,true}){
   var golem=MobContent.MELON_GOLEM.get().create(level,EntitySpawnReason.COMMAND);golem.setAggressive(angry);var drops=golem.onSheared(player,new ItemStack(Items.SHEARS),level,golem.blockPosition());
   check(drops.size()==1&&drops.getFirst().is(MobContent.CARVED_MELON_ITEM.get())&&!golem.hasMelon(),"Always shear one carved melon including angry golem");check(golem.onSheared(player,new ItemStack(Items.SHEARS),level,golem.blockPosition()).isEmpty(),"No repeat shearing");
  }
  var pos=new BlockPos(90,170,90);level.getChunkAt(pos);List<MelonGolem> golems=new ArrayList<>();java.util.function.Consumer<EntityJoinLevelEvent> observer=e->{if(e.getEntity() instanceof MelonGolem g)golems.add(g);};NeoForge.EVENT_BUS.addListener(observer);
  try{level.setBlock(pos.below(2),Blocks.SNOW_BLOCK.defaultBlockState(),3);level.setBlock(pos.below(),Blocks.SNOW_BLOCK.defaultBlockState(),3);level.setBlock(pos,MobContent.CARVED_MELON.get().defaultBlockState(),3);check(golems.size()==1&&level.getBlockState(pos).isAir()&&level.getBlockState(pos.below(2)).isAir(),"Carved melon and snow create golem");golems.forEach(Entity::discard);}finally{NeoForge.EVENT_BUS.unregister(observer);}
  var swamped=MobContent.SWAMPED.get().create(level,EntitySpawnReason.COMMAND);swamped.snapTo(0,100,0,0,0);player.snapTo(0,100,1,0,0);player.setHealth(player.getMaxHealth());player.invulnerableTime=0;player.removeAllEffects();
  check(!(swamped instanceof RangedAttackMob),"Swamped has no ranged AI");check(swamped.doHurtTarget(level,player)&&player.hasEffect(MobEffects.POISON),"Unarmed player hit poisons");
  player.removeAllEffects();player.invulnerableTime=0;swamped.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_AXE));swamped.doHurtTarget(level,player);check(!player.hasEffect(MobEffects.POISON),"Armed hit follows husk restriction");player.setHealth(player.getMaxHealth());
  var rabbit=MobContent.HUSK_RABBIT.get().create(level,EntitySpawnReason.COMMAND);player.removeAllEffects();player.invulnerableTime=0;player.setHealth(player.getMaxHealth());check(rabbit.doHurtTarget(level,player)&&player.hasEffect(MobEffects.HUNGER),"Husk Rabbit melee applies hunger");player.removeAllEffects();player.setHealth(player.getMaxHealth());
  var wolf=MobContent.SKELETON_WOLF.get().create(level,EntitySpawnReason.COMMAND);wolf.tame(player);wolf.setHealth(2);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BONE,2));wolf.mobInteract(player,InteractionHand.MAIN_HAND);check(wolf.getHealth()>2&&!wolf.canBreed(),"Tame wolf eats bones and cannot breed");
  var spider=MobContent.BONE_SPIDER.get().create(level,EntitySpawnReason.COMMAND);spider.setPotionContents(PotionContents.EMPTY.withEffectAdded(new MobEffectInstance(MobContent.UNDEAD_BODY,3600)));var saved=(BoneSpider)restore(level,spider);check(saved.getPotionContents().hasEffects()&&saved.isInvertedHealAndHarm()&&!saved.canFreeze(),"Bone spider payload save/load, undead and frost immunity");
  player.addEffect(new MobEffectInstance(MobContent.UNDEAD_BODY,100));check(player.isInvertedHealAndHarm(),"Undead body inverts potion response");player.removeAllEffects();
  var slime=MobContent.TROPICAL_SLIME.get().create(level,EntitySpawnReason.COMMAND);slime.setSize(1,true);slime.setHasFish(true);slime.setFromBucket(true);var bucket=slime.getBucketItemStack();slime.saveToBucketTag(bucket);check(bucket.is(MobContent.TROPICAL_SLIME_BUCKET.get()),"Only requested bucket item");
  var released=MobContent.TROPICAL_SLIME.get().create(level,EntitySpawnReason.BUCKET);released.loadFromBucketTag(bucket.get(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA).copyTag());released.setFromBucket(true);check(released.isTiny()&&released.hasFish()&&released.requiresCustomPersistence(),"Fish survives bucket pickup/release and cannot despawn");
  var persisted=(TropicalSlime)restore(level,released);check(persisted.fromBucket()&&persisted.hasFish(),"Bucket and fish flags persist across reload");
  var fish=new ArrayList<Entity>();java.util.function.Consumer<EntityJoinLevelEvent> fishObserver=e->{if(e.getEntity().getType()==EntityTypes.TROPICAL_FISH)fish.add(e.getEntity());};NeoForge.EVENT_BUS.addListener(fishObserver);
  try{released.setHealth(0);released.remove(Entity.RemovalReason.KILLED);released.remove(Entity.RemovalReason.KILLED);check(fish.size()==1,"One fish released even if removal repeats");fish.forEach(Entity::discard);}finally{NeoForge.EVENT_BUS.unregister(fishObserver);}
 }
 private static void projectilesAndConversion(ServerLevel level,FakePlayer player){
  player.snapTo(10,100,10,0,0);player.setHealth(player.getMaxHealth());player.invulnerableTime=0;player.removeAllEffects();
  var seed=new TestSeed(level);float before=player.getHealth();seed.hit(player);check(player.getHealth()==before-1,"Seed projectile deals one damage");
  player.invulnerableTime=0;var shard=new TestShard(level);check(MobContent.id("bone_shard").equals(shard.getItem().get(net.minecraft.core.component.DataComponents.ITEM_MODEL)),"Bone shard uses its dedicated model without a new item");shard.setDeltaMovement(1,0,0);shard.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,800));shard.hit(player);check(player.hasEffect(MobEffects.SLOWNESS)&&player.getEffect(MobEffects.SLOWNESS).getDuration()==100,"Bone projectile applies one eighth payload duration");player.removeAllEffects();player.setHealth(player.getMaxHealth());
  var spider=MobContent.BONE_SPIDER.get().create(level,EntitySpawnReason.COMMAND);
  for(int index=0;index<4;index++){spider.setPotionContents(PotionContents.EMPTY);spider.rollPotion(new PotionRoll(0,index));check(spider.getPotionContents().hasEffects(),"All four rare potion payloads available");}
  spider.setPotionContents(PotionContents.EMPTY);spider.rollPotion(new PotionRoll(.01F,0));check(!spider.getPotionContents().hasEffects(),"One percent payload boundary excluded");
  List<Entity> projectiles=new ArrayList<>();java.util.function.Consumer<EntityJoinLevelEvent> shots=e->{if(e.getEntity().getType()==MobContent.MELON_SEED.get()||e.getEntity().getType()==MobContent.BONE_SHARD.get())projectiles.add(e.getEntity());};NeoForge.EVENT_BUS.addListener(shots);
  try{var golem=MobContent.MELON_GOLEM.get().create(level,EntitySpawnReason.COMMAND);golem.performRangedAttack(player,1);spider.performRangedAttack(player,1);check(projectiles.size()==2,"Both ranged attacks create registered standalone projectiles");projectiles.forEach(Entity::discard);}finally{NeoForge.EVENT_BUS.unregister(shots);}
  List<net.minecraft.world.entity.monster.zombie.Drowned> converted=new ArrayList<>();java.util.function.Consumer<EntityJoinLevelEvent> conversion=e->{if(e.getEntity() instanceof net.minecraft.world.entity.monster.zombie.Drowned d)converted.add(d);};NeoForge.EVENT_BUS.addListener(conversion);
  try{var zombie=new TestBoulder(level);zombie.snapTo(100,170,100,0,0);level.addFreshEntity(zombie);zombie.convert(level);check(converted.size()==1&&zombie.isRemoved()&&converted.getFirst().getType()==EntityTypes.DROWNED,"Bouldering zombie converts to vanilla drowned");converted.getFirst().discard();var swamped=new TestSwamped(level);swamped.snapTo(100,170,100,0,0);level.addFreshEntity(swamped);swamped.convert(level);check(converted.size()==2&&swamped.isRemoved()&&converted.getLast().getType()==EntityTypes.DROWNED,"Swamped converts to vanilla drowned");converted.getLast().discard();}finally{NeoForge.EVENT_BUS.unregister(conversion);}
  List<Entity> fish=new ArrayList<>(),children=new ArrayList<>();java.util.function.Consumer<EntityJoinLevelEvent> splitting=e->{if(e.getEntity().getType()==EntityTypes.TROPICAL_FISH)fish.add(e.getEntity());if(e.getEntity() instanceof TropicalSlime)children.add(e.getEntity());};NeoForge.EVENT_BUS.addListener(splitting);
  try{var slime=MobContent.TROPICAL_SLIME.get().create(level,EntitySpawnReason.COMMAND);slime.snapTo(110,170,110,0,0);slime.setSize(4,true);slime.setHasFish(true);slime.setHealth(0);slime.remove(Entity.RemovalReason.KILLED);check(fish.size()==1&&children.size()>=2,"Large slime splits and releases one fish");for(var child:children){check(((TropicalSlime)child).getSize()==2&&!((TropicalSlime)child).hasFish(),"Split children halve size and do not duplicate fish");child.discard();}fish.forEach(Entity::discard);}finally{NeoForge.EVENT_BUS.unregister(splitting);}
 }
 private static final class TestSeed extends com.akiotsukino.chocolateflavored.mob.entity.projectile.MelonSeed {TestSeed(ServerLevel l){super(MobContent.MELON_SEED.get(),l);}void hit(Entity e){onHitEntity(new net.minecraft.world.phys.EntityHitResult(e));}}
 private static final class TestShard extends com.akiotsukino.chocolateflavored.mob.entity.projectile.BoneShard {TestShard(ServerLevel l){super(MobContent.BONE_SHARD.get(),l);}void hit(Entity e){onHitEntity(new net.minecraft.world.phys.EntityHitResult(e));}}
 private static final class TestSwamped extends Swamped {TestSwamped(ServerLevel l){super(MobContent.SWAMPED.get(),l);}void convert(ServerLevel l){doUnderWaterConversion(l);}}
 private static final class TestBoulder extends BoulderingZombie {TestBoulder(ServerLevel l){super(MobContent.BOULDERING_ZOMBIE.get(),l);}void convert(ServerLevel l){doUnderWaterConversion(l);}}
 private static final class PotionRoll extends LegacyRandomSource {private final float roll;private final int index;PotionRoll(float roll,int index){super(0);this.roll=roll;this.index=index;}@Override public float nextFloat(){return roll;}@Override public int nextInt(int bound){return index;}}
 private static Entity restore(ServerLevel level,Entity entity){var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());check(entity.saveAsPassenger(out),"Entity can be serialized as passenger/root");return EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()),level,new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.LOAD, false)).orElseThrow();}
 private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
 /** FakePlayer intentionally blocks damage and has no loaded client; this fixture enables native combat. */
 private static final class CombatPlayer extends FakePlayer {
  CombatPlayer(ServerLevel level){super(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("4e9f65ab-cf80-4311-bbdc-612448f06644"),"MobVerification"));setInvulnerable(false);setGameMode(net.minecraft.world.level.GameType.SURVIVAL);}
  @Override public boolean isInvulnerableTo(ServerLevel level,net.minecraft.world.damagesource.DamageSource source){return false;}
 }
 private static final class Roll extends LegacyRandomSource {private final int value;Roll(int value){super(0);this.value=value;}@Override public int nextInt(int bound){return value;}}
 private MobVerification(){}
}
