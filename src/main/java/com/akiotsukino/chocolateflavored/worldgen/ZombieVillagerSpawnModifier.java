package com.akiotsukino.chocolateflavored.worldgen;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Adjust selection weights once at biome loading, preserving other monsters' probabilities. */
public record ZombieVillagerSpawnModifier() implements BiomeModifier {
    public static final MapCodec<ZombieVillagerSpawnModifier> CODEC = MapCodec.unit(new ZombieVillagerSpawnModifier());
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ChocolateFlavored.MODID);
    static { SERIALIZERS.register("zombie_villager_spawns", () -> CODEC); }
    public static void register(IEventBus bus) { SERIALIZERS.register(bus); }

    @Override public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.MODIFY || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA)) return;
        var spawns = builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER);
        var original = List.copyOf(spawns.getList());
        var adjusted = rebalance(original);
        if (adjusted == original) return;
        spawns.removeIf(entry -> true);
        adjusted.forEach(entry -> spawns.add(entry.value(), entry.weight()));
    }

    public static List<Weighted<SpawnerData>> rebalance(List<Weighted<SpawnerData>> original) {
        SpawnerData zombie = null, villager = null;
        long combined = 0;
        for (var entry : original) {
            if (entry.value().type() == EntityTypes.ZOMBIE) { zombie = entry.value(); combined += entry.weight(); }
            else if (entry.value().type() == EntityTypes.ZOMBIE_VILLAGER) { villager = entry.value(); combined += entry.weight(); }
        }
        // Do not introduce either entity in biomes that don't already spawn both.
        if (zombie == null || villager == null || combined == 0) return original;
        var values = new ArrayList<SpawnerData>();
        var weights = new ArrayList<Long>();
        for (var entry : original) {
            if (entry.value().type() != EntityTypes.ZOMBIE && entry.value().type() != EntityTypes.ZOMBIE_VILLAGER) {
                values.add(entry.value()); weights.add(entry.weight() * 100L);
            }
        }
        values.add(zombie); weights.add(combined * 99);
        values.add(villager); weights.add(combined);
        long divisor = 0;
        for (long weight : weights) divisor = gcd(divisor, weight);
        var result = new ArrayList<Weighted<SpawnerData>>();
        for (int i = 0; i < values.size(); i++) result.add(new Weighted<>(values.get(i), Math.toIntExact(weights.get(i) / divisor)));
        return result;
    }
    private static long gcd(long a, long b) { while (b != 0) { long next = a % b; a = b; b = next; } return a; }
    @Override public MapCodec<? extends BiomeModifier> codec() { return CODEC; }
}
