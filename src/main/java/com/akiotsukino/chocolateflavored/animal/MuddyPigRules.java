// SPDX-License-Identifier: LGPL-3.0-only
// EarthMobs muddy-pig behavior adapted to vanilla mud, without its unrequested mud-fluid items.
package com.akiotsukino.chocolateflavored.animal;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class MuddyPigRules {
    public static void becomeMuddy(Pig pig) {
        MuddyPig state = (MuddyPig) pig;
        state.chocolateflavored$setMuddy(true);
        state.chocolateflavored$setSheared(false);
        state.chocolateflavored$setFlowerColor(DyeColor.byId(pig.getRandom().nextInt(16)));
    }
    public static void copy(MuddyPig source, MuddyPig target) {
        target.chocolateflavored$setMuddy(source.chocolateflavored$isMuddy());
        target.chocolateflavored$setSheared(source.chocolateflavored$isSheared());
        target.chocolateflavored$setFlowerColor(source.chocolateflavored$getFlowerColor());
    }
    public static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Pig pig) || !(pig.level() instanceof ServerLevel level) || !pig.isAlive()) return;
        MuddyPig state = (MuddyPig) pig;
        boolean inMud = level.getBlockState(pig.blockPosition()).is(Blocks.MUD) || level.getBlockState(pig.getBlockPosBelowThatAffectsMyMovement()).is(Blocks.MUD);
        boolean wash = pig.isInWater() && state.chocolateflavored$isMuddy();
        boolean coat = inMud && !pig.isInWater() && (!state.chocolateflavored$isMuddy() || state.chocolateflavored$isSheared());
        int timer = state.chocolateflavored$getShakeTicks();
        // Finish the chosen shake even after stepping off the coating surface, as in EarthMobs.
        if (timer == 0) {
            if (!coat && !wash) return;
            timer = wash ? -1 : 1;
            pig.playSound(SoundEvents.WOLF_SHAKE, 1, 1);
        } else timer += Integer.signum(timer);
        state.chocolateflavored$setShakeTicks(timer);
        int progress = Math.abs(timer);
        boolean washing = timer < 0;
        if (progress > 8 && progress % 3 == 0) level.sendParticles(washing ? ParticleTypes.SPLASH : new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), pig.getX(), pig.getY() + .8, pig.getZ(), 5, .4, .1, .4, .03);
        if (progress >= 40) {
            if (washing) state.chocolateflavored$setMuddy(false); else becomeMuddy(pig);
            state.chocolateflavored$setShakeTicks(0);
        }
    }
    private MuddyPigRules() {}
}
