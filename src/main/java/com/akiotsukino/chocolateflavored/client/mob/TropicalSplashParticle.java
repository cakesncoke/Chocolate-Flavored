package com.akiotsukino.chocolateflavored.client.mob;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** EarthMobs' tropical-ball fragments, using the current vanilla item-breaking physics and quarter-sprite UVs. */
public final class TropicalSplashParticle extends BreakingItemParticle {
    private TropicalSplashParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        xd = xd * .1F + dx;
        yd = yd * .1F + dy;
        zd = zd * .1F + dz;
    }
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override public SingleQuadParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
            return new TropicalSplashParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
