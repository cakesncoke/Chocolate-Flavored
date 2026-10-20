package com.akiotsukino.chocolateflavored.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;

public class EarthChicken extends Chicken {
    public EarthChicken(EntityType<? extends Chicken> type, Level level) { super(type, level); }
    @Override
    public Chicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Chicken baby = (Chicken) getType().create(level, EntitySpawnReason.BREEDING);
        return baby;
    }
    /** Keep vanilla species-based predator AI through TypedInstance's supported override. */
    @Override
    public boolean is(EntityType<?> type) { return type == EntityType.CHICKEN || super.is(type); }
}
