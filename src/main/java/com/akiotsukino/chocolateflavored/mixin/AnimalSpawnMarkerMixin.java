package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class AnimalSpawnMarkerMixin {
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void chocolateflavored$prepare(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob mob = (Mob) (Object) this;
        if ((reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION)
                && (mob.getType() == EntityTypes.COW || mob.getType() == EntityTypes.PIG || mob.getType() == EntityTypes.SHEEP || mob.getType() == EntityTypes.CHICKEN || mob.getType() == EntityTypes.RABBIT))
            mob.getPersistentData().putBoolean("chocolateflavored:animal_spawn_pending", true);
    }
}
