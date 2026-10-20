package com.akiotsukino.chocolateflavored.client.mob;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
public final class TropicalSplashParticle extends SingleQuadParticle {
 private TropicalSplashParticle(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites){super(level,x,y,z,sprites.first());scale(.5F);lifetime=20+random.nextInt(20);gravity=.04F;xd=dx;yd=dy;zd=dz;}
 @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
 public static final class Provider implements ParticleProvider<SimpleParticleType>{
  private final SpriteSet sprites;public Provider(SpriteSet sprites){this.sprites=sprites;}
  @Override public SingleQuadParticle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double dx,double dy,double dz,RandomSource random){return new TropicalSplashParticle(level,x,y,z,dx,dy,dz,sprites);}
 }
}
