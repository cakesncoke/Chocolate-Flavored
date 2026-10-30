package com.akiotsukino.chocolateflavored.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.Level;

public class EarthPig extends Pig {
    public EarthPig(EntityType<? extends Pig> type, Level level) { super(type, level); }
    @Override
    public Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Pig baby = (Pig) getType().create(level, EntitySpawnReason.BREEDING);
        return baby;
    }
    /** Keep vanilla species-based predator AI through TypedInstance's supported override. */
    @Override
    public boolean is(EntityType<?> type) { return type == EntityTypes.PIG || super.is(type); }
}
