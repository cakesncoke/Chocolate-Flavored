package com.akiotsukino.chocolateflavored.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;

public class EarthSheep extends Sheep {
    public EarthSheep(EntityType<? extends Sheep> type, Level level) { super(type, level); }
    @Override
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Sheep baby = (Sheep) getType().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && partner instanceof Sheep sheep) baby.setColor(net.minecraft.world.item.DyeColor.getMixedColor(level, getColor(), sheep.getColor()));
        return baby;
    }
}
