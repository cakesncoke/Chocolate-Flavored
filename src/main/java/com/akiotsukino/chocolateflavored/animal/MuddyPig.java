package com.akiotsukino.chocolateflavored.animal;
import net.minecraft.world.item.DyeColor;
/** Synced state shared by vanilla pigs and Pale Pigs, matching EarthMobs' muddy-pig state. */
public interface MuddyPig {
    boolean chocolateflavored$isMuddy();
    void chocolateflavored$setMuddy(boolean muddy);
    boolean chocolateflavored$isSheared();
    void chocolateflavored$setSheared(boolean sheared);
    DyeColor chocolateflavored$getFlowerColor();
    void chocolateflavored$setFlowerColor(DyeColor color);
    int chocolateflavored$getShakeTicks();
    void chocolateflavored$setShakeTicks(int ticks);
}
