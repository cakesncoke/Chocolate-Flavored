package com.akiotsukino.chocolateflavored.client;

// Adapted from Farmer's Delight, Copyright (c) 2020 vectorwing, MIT.
// Full license: META-INF/licenses/FarmersDelight-MIT.txt.
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class SteamParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private SteamParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, sprites.first()); this.sprites = sprites;
        scale(2); setSize(0.25F, 0.25F); lifetime = random.nextInt(50) + 80;
        gravity = 0.000003F; xd = dx; yd = dy + random.nextFloat() / 500.0F; zd = dz; setAlpha(0.6F);
    }
    @Override protected Layer getLayer() { return Layer.TRANSLUCENT; }
    @Override public void tick() {
        xo = x; yo = y; zo = z;
        if (age++ >= lifetime || alpha <= 0) { remove(); return; }
        xd += (random.nextFloat() - 0.5) / 2500; zd += (random.nextFloat() - 0.5) / 2500;
        yd -= gravity; move(xd, yd, zd); setSpriteFromAge(sprites);
        if (age >= lifetime - 60) alpha = Math.max(0, alpha - 0.02F);
    }
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override public SingleQuadParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
            return new SteamParticle(level, x, y + 0.3, z, dx, dy, dz, sprites);
        }
    }
}
