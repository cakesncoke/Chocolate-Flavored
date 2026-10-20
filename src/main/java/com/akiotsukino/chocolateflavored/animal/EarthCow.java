package com.akiotsukino.chocolateflavored.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.Level;

public class EarthCow extends Cow {
    public EarthCow(EntityType<? extends Cow> type, Level level) { super(type, level); }
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Cow baby = (Cow) getType().create(level, EntitySpawnReason.BREEDING);
        return baby;
    }
}
