package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(WindCharge.class)
public interface TrimWindInvoker { @Invoker("explode") void chocolateflavored$explode(Vec3 position); }
