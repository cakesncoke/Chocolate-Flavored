package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.worldgen.ZombieVillagerSpawnModifier;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

final class VillageVerification {
    static void run(ServerLevel level) {
        try {
            poolsAndResidents(level);
            spawnWeights(level);
            breedingAndGolems(level);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Village verification reflection failed", error); }
        ChocolateFlavored.LOGGER.info("VILLAGE_VERIFICATION_PASSED: all five abandoned village pools, ten persistent resident templates and entity save/load, actual daylight exposure, exact spawn weights and pine exception, actual breeding timers, villager summons blocked, player-built and outpost golems retained");
    }
    private static void poolsAndResidents(ServerLevel level) throws ReflectiveOperationException {
        var pools = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var normal = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        check(normal.is(EntityTypeTags.BURN_IN_DAYLIGHT), "Normal zombies retain daylight burning tag");
        BlockPos pos = new BlockPos(48, 160, 0);
        var bounds = new AABB(pos).inflate(12);
        for (String style : List.of("plains", "desert", "savanna", "snowy", "taiga")) {
            var pool = pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("village/" + style + "/town_centers"))).value();
            var json = StructureTemplatePool.DIRECT_CODEC.encodeStart(ops, pool).getOrThrow().getAsJsonObject();
            check(!json.getAsJsonArray("elements").isEmpty(), "Village has starting pieces: " + style);
            for (var entry : json.getAsJsonArray("elements")) {
                check(entry.getAsJsonObject().getAsJsonObject("element").get("location").getAsString().contains("/zombie/town_centers/"), "Every village start is abandoned: " + style);
            }
            for (String profession : List.of("nitwit", "unemployed")) {
                var template = level.getStructureManager().getOrCreate(Identifier.withDefaultNamespace("village/" + style + "/zombie/villagers/" + profession));
                check(template.placeInWorld(level, pos, pos, new StructurePlaceSettings().setIgnoreEntities(false).setFinalizeEntities(true), level.getRandom(), 3), "Resident template placed");
                var residents = level.getEntitiesOfClass(ZombieVillager.class, bounds);
                check(residents.size() == 1, "Abandoned template spawns zombie villager: " + style + "/" + profession);
                var resident = residents.getFirst();
                check(resident.isPersistenceRequired(), "Abandoned resident cannot naturally despawn");
                check(!resident.is(EntityTypeTags.BURN_IN_DAYLIGHT), "Zombie villagers excluded from actual daylight burning tag");
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                // A finalized baby resident may be a chicken jockey; save() skips passengers.
                check(resident.saveAsPassenger(output), "Resident serialization includes mounted villagers");
                var restored = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), output.buildResult()), level, EntitySpawnReason.COMMAND).orElseThrow();
                check(restored instanceof ZombieVillager zombie && zombie.isPersistenceRequired(), "Persistence survives entity save/load");
                resident.checkDespawn();
                check(!resident.isRemoved(), "Persistent resident survives despawn check");
                level.getEntitiesOfClass(Entity.class, bounds).forEach(Entity::discard);
            }
        }
        var natural = EntityType.ZOMBIE_VILLAGER.create(level, EntitySpawnReason.NATURAL);
        check(!natural.isPersistenceRequired(), "Natural zombie villagers are not globally made persistent");
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(), "time set 6000");
        normal.setPos(48.5, 180, 0.5); natural.setPos(48.5, 180, 0.5);
        normal.setNoAi(true); natural.setNoAi(true);
        normal.setNoGravity(true); natural.setNoGravity(true);
        for (int i = 0; i < 1000; i++) { normal.aiStep(); natural.aiStep(); }
        check(normal.isOnFire(), "Normal zombie actually ignites in daytime control test");
        check(!natural.isOnFire(), "Zombie villager does not ignite in same daytime exposure");
    }
    private static void spawnWeights(ServerLevel level) {
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        int checked = 0;
        for (var biome : biomes.listElements().toList()) {
            var entries = biome.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
            int zombies = weight(entries, EntityType.ZOMBIE), villagers = weight(entries, EntityType.ZOMBIE_VILLAGER);
            if (biome.is(Biomes.OLD_GROWTH_PINE_TAIGA)) {
                check(zombies == 100 && villagers == 25, "Old growth pine taiga retains vanilla weights");
            } else if (zombies > 0 && villagers > 0) {
                check((long)villagers * 100 == (long)zombies + villagers, "Exact one-percent zombie-villager selection: " + biome.key().identifier()); checked++;
            }
        }
        check(checked > 30, "Spawn modifier ran across overworld biomes");
        var original = List.of(new Weighted<>(new SpawnerData(EntityType.ZOMBIE, 4, 4), 90),
                new Weighted<>(new SpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1), 5),
                new Weighted<>(new SpawnerData(EntityType.ZOMBIE_HORSE, 1, 1), 5),
                new Weighted<>(new SpawnerData(EntityType.SPIDER, 4, 4), 100));
        var adjusted = ZombieVillagerSpawnModifier.rebalance(original);
        int total = adjusted.stream().mapToInt(Weighted::weight).sum();
        check((long)weight(adjusted, EntityType.SPIDER) * 200 == 100L * total, "Other monster probability unchanged");
        check((long)weight(adjusted, EntityType.ZOMBIE_HORSE) * 200 == 5L * total, "Zombie horse probability unchanged");
    }
    @SuppressWarnings("unchecked")
    private static void breedingAndGolems(ServerLevel level) throws ReflectiveOperationException {
        BlockPos pos = new BlockPos(80, 160, 0);
        var bounds = new AABB(pos).inflate(20);
        var first = EntityType.VILLAGER.create(level, EntitySpawnReason.COMMAND);
        var second = EntityType.VILLAGER.create(level, EntitySpawnReason.COMMAND);
        first.setPos(80.5, 160, 0.5); second.setPos(81.5, 160, 0.5);
        var breed = VillagerMakeLove.class.getDeclaredMethod("breed", ServerLevel.class, Villager.class, Villager.class); breed.setAccessible(true);
        var newborn = ((Optional<Villager>)breed.invoke(new VillagerMakeLove(), level, first, second)).orElseThrow();
        check(first.getAge() == 42000 && second.getAge() == 42000, "Both parents have 35-minute cooldown");
        check(newborn.getAge() == -72000, "Bred baby starts at one hour");
        first.setNoAi(true); newborn.setNoAi(true); first.aiStep(); newborn.aiStep();
        check(first.getAge() == 41999 && newborn.getAge() == -71999, "Timers advance by one game tick");
        newborn.setAge(-1); newborn.aiStep(); check(!newborn.isBaby(), "Baby matures at age zero");
        var eggBaby = EntityType.VILLAGER.create(level, EntitySpawnReason.SPAWN_ITEM_USE); eggBaby.setBaby(true);
        check(eggBaby.getAge() == -72000, "Other newly initialized villager babies also take one hour");
        var cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND); cow.setBaby(true);
        check(cow.getAge() == -24000, "Animal maturation remains vanilla");
        newborn.discard();
        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) level.setBlockAndUpdate(pos.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        first.getBrain().setMemory(MemoryModuleType.LAST_SLEPT, level.getGameTime());
        check(first.wantsToSpawnGolem(level.getGameTime()), "Summoning test meets vanilla sleep condition");
        level.addFreshEntity(first);
        for (int i = 0; i < 10; i++) first.spawnGolemIfNeeded(level, level.getGameTime(), 0);
        check(level.getEntitiesOfClass(IronGolem.class, bounds).isEmpty(), "Villager summoning is disabled even when otherwise eligible");
        first.discard();
        level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.IRON_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.above().east(), Blocks.IRON_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.above().west(), Blocks.IRON_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.above(2), Blocks.CARVED_PUMPKIN.defaultBlockState());
        check(level.getEntitiesOfClass(IronGolem.class, bounds).stream().anyMatch(IronGolem::isPlayerCreated), "Player construction still creates iron golems");
        level.getEntitiesOfClass(Entity.class, bounds).forEach(Entity::discard);
        var cage = level.getStructureManager().getOrCreate(Identifier.withDefaultNamespace("pillager_outpost/feature_cage1"));
        cage.placeInWorld(level, pos, pos, new StructurePlaceSettings().setIgnoreEntities(false).setFinalizeEntities(true), level.getRandom(), 3);
        check(level.getEntitiesOfClass(IronGolem.class, bounds).size() == 1, "Pillager outpost cage still spawns its golem");
        level.getEntitiesOfClass(Entity.class, bounds).forEach(Entity::discard);
    }
    private static int weight(List<Weighted<SpawnerData>> entries, EntityType<?> type) {
        return entries.stream().filter(e -> e.value().type() == type).mapToInt(Weighted::weight).sum();
    }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException("Village verification failed: " + message); }
}
