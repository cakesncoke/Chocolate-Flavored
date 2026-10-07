// Adapted from Farmer's Delight (MIT); see THIRD_PARTY_NOTICES.md.
package com.akiotsukino.chocolateflavored.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;

import java.util.function.Supplier;

public class ModParticleTypes
{
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, ChocolateFlavored.MOD_ID);

	public static final Supplier<SimpleParticleType> STEAM = PARTICLE_TYPES.register("steam",
			() -> new SimpleParticleType(true));
}
