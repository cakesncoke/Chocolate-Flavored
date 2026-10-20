package com.akiotsukino.chocolateflavored.mob;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.*;
import net.neoforged.neoforge.registries.*;
import java.util.*;
public record MonsterBiomeModifier() implements BiomeModifier {
 public static final MapCodec<MonsterBiomeModifier> CODEC=MapCodec.unit(new MonsterBiomeModifier());
 private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS=DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,ChocolateFlavored.MODID);
 static{SERIALIZERS.register("earth_monsters",()->CODEC);}
 public static void register(IEventBus bus){SERIALIZERS.register(bus);}
 @Override public void modify(Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){
  boolean forest=biome.is(BiomeTags.IS_FOREST), soul=biome.is(Biomes.SOUL_SAND_VALLEY);
  if(phase==Phase.ADD){
   var spawns=builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER);
   if(forest)spawns.add(new SpawnerData(MobContent.SWAMPED.get(),3,4),20);
   if(biome.is(BiomeTags.IS_OCEAN)||biome.is(BiomeTags.IS_DEEP_OCEAN))spawns.add(new SpawnerData(MobContent.TROPICAL_SLIME.get(),3,4),3);
   if(soul)builder.getMobSpawnSettings().addMobCharge(MobContent.SKELETON_WOLF.get(),.7,.15);
  }
  // Later than MODIFY, where the existing zombie-villager rule preserves its own 1% ratio.
  if(phase==Phase.AFTER_EVERYTHING&&(forest||soul)){
   var spawns=builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER);
   var adjusted=withSkeletonWolves(List.copyOf(spawns.getList()),soul?2:3,soul?3:4);
   spawns.removeIf(e->true);adjusted.forEach(e->spawns.add(e.value(),e.weight()));
  }
 }
 /** Exact 1.5% selection weight, preserving all other relative weights. */
 public static List<Weighted<SpawnerData>> withSkeletonWolves(List<Weighted<SpawnerData>> entries,int min,int max){
  long total=entries.stream().filter(e->e.value().type()!=MobContent.SKELETON_WOLF.get()).mapToLong(Weighted::weight).sum();
  if(total==0)return entries;
  List<SpawnerData> values=new ArrayList<>();List<Long> weights=new ArrayList<>();
  for(var e:entries)if(e.value().type()!=MobContent.SKELETON_WOLF.get()){values.add(e.value());weights.add(e.weight()*985L);}
  values.add(new SpawnerData(MobContent.SKELETON_WOLF.get(),min,max));weights.add(total*15);
  long divisor=0;for(long weight:weights)divisor=gcd(divisor,weight);
  List<Weighted<SpawnerData>> result=new ArrayList<>();for(int i=0;i<values.size();i++)result.add(new Weighted<>(values.get(i),Math.toIntExact(weights.get(i)/divisor)));return result;
 }
 private static long gcd(long a,long b){while(b!=0){long t=a%b;a=b;b=t;}return a;}
 @Override public MapCodec<? extends BiomeModifier> codec(){return CODEC;}
}
