// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.mob.entity.projectile;

import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class MelonSeed extends ThrowableItemProjectile {
	public MelonSeed(EntityType<? extends MelonSeed> p_37391_, Level p_37392_) {
		super(p_37391_, p_37392_);
	}

	public MelonSeed(Level p_37399_, LivingEntity p_37400_, ItemStack p_363259_) {
		super(MobContent.MELON_SEED.get(), p_37400_, p_37399_, p_363259_);
	}

	public MelonSeed(Level p_37394_, double p_37395_, double p_37396_, double p_37397_, ItemStack p_363259_) {
		super(MobContent.MELON_SEED.get(), p_37395_, p_37396_, p_37397_, p_37394_, p_363259_);
	}

	protected Item getDefaultItem() {
		return Items.MELON_SEEDS;
	}

	private ParticleOptions getParticle() {
		return new ItemParticleOption(ParticleTypes.ITEM, Items.MELON_SEEDS);
	}

	public void handleEntityEvent(byte p_37402_) {
		if (p_37402_ == 3) {
			ParticleOptions particleoptions = this.getParticle();

			for (int i = 0; i < 8; ++i) {
				this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		}

	}

	protected void onHitEntity(EntityHitResult p_37404_) {
		super.onHitEntity(p_37404_);
		Entity entity = p_37404_.getEntity();
		if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) entity.hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), 1);
	}

	protected void onHit(HitResult p_37488_) {
		super.onHit(p_37488_);
        if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}

	}
}
