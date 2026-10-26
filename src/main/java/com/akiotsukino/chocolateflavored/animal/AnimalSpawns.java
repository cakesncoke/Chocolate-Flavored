package com.akiotsukino.chocolateflavored.animal;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Replaces finalized natural animals without adding spawn-table weight or rerolling on reload/travel. */
public final class AnimalSpawns {
    private static final String CHECKED = "chocolateflavored:animal_spawn_checked";
    public static void join(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob mob)) return;
        EntityType<?> originalType = mob.getType();
        if (originalType != EntityType.COW && originalType != EntityType.PIG && originalType != EntityType.CHICKEN
                && originalType != EntityType.SHEEP && originalType != EntityType.RABBIT) return;
        if (event.loadedFromDisk() && !mob.getPersistentData().getBooleanOr("chocolateflavored:animal_spawn_pending", false)) return;
        mob.getPersistentData().remove("chocolateflavored:animal_spawn_pending");
        EntitySpawnReason reason = mob.getSpawnType();
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) return;
        if (mob.getPersistentData().getBooleanOr(CHECKED, false)) return;
        mob.getPersistentData().putBoolean(CHECKED, true);
        Holder<Biome> biome = level.getBiome(mob.blockPosition());
        if (mob instanceof Pig pig && biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP)) {
            MuddyPigRules.becomeMuddy(pig);
        }
        boolean temperate = mob instanceof Cow cow ? cow.getVariant().is(CowVariants.TEMPERATE)
                : mob instanceof Pig pig && pig.getVariant().is(PigVariants.TEMPERATE);
        EntityType<?> type = select(mob.getType(), biome, temperate, mob.getRandom());
        if (type == null) return;
        Mob replacement = (Mob) type.create(level, reason);
        if (replacement == null) return;
        replacement.snapTo(mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), mob.getXRot());
        replacement.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), reason, null);
        replacement.getPersistentData().putBoolean(CHECKED, true);
        if (mob instanceof AgeableMob original && replacement instanceof AgeableMob ageable) ageable.setAge(original.getAge());
        if (mob instanceof Sheep original && replacement instanceof Sheep sheep) sheep.setColor(original.getColor() == net.minecraft.world.item.DyeColor.PINK ? original.getColor() : type == AnimalContent.LONG_NOSE_SHEEP.get() && mob.getRandom().nextBoolean() ? net.minecraft.world.item.DyeColor.BROWN : net.minecraft.world.item.DyeColor.WHITE);
        if (mob instanceof Pig && replacement instanceof Pig pig) MuddyPigRules.copy((MuddyPig) mob, (MuddyPig) pig);
        // Add first: a third-party cancellation must not erase the original animal.
        if (level.addFreshEntity(replacement)) event.setCanceled(true);
    }

    /** Integer thresholds keep the listed unconditional probabilities exact where rules overlap. */
    public static EntityType<?> select(EntityType<?> original, Holder<Biome> biome, boolean temperate, RandomSource random) {
        int roll = random.nextInt(100_000);
        if (original == EntityType.COW) {
            boolean flowers = biome.is(Biomes.FLOWER_FOREST);
            if (flowers && roll < 500) return random.nextInt(100) < 33 ? AnimalContent.MOOLIP.get() : AnimalContent.MOOBLOOM.get();
            if (temperate && roll >= (flowers ? 500 : 0) && roll < (flowers ? 5500 : 5000)) return AnimalContent.DAIRY_COW.get();
        } else if (original == EntityType.PIG) {
            if (temperate && roll < 10_000) return AnimalContent.PALE_PIG.get();
        } else if (original == EntityType.CHICKEN) {
            if (roll < 250) return AnimalContent.FANCY_CHICKEN.get();
            if (biome.is(BiomeTags.IS_JUNGLE) && roll >= 250 && roll < 5250) return AnimalContent.BRONZED_CHICKEN.get();
        } else if (original == EntityType.SHEEP) {
            if (biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_MOUNTAIN) && roll < 3000) return AnimalContent.HORNED_SHEEP.get();
            if (biome.is(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS)) return AnimalContent.LONG_NOSE_SHEEP.get();
            if (biome.is(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS)) return random.nextInt(100) < 5 ? AnimalContent.PATCHED_SHEEP.get() : AnimalContent.FUZZY_SHEEP.get();
        } else if (original == EntityType.RABBIT && biome.is(Biomes.FLOWER_FOREST) && roll < 250) return AnimalContent.JUMBO_RABBIT.get();
        return null;
    }
    private AnimalSpawns() {}
}
