package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Mob.class)
public abstract class MonsterSpawnMarkerMixin {
 @Inject(method="finalizeSpawn",at=@At("TAIL"))
 private void chocolateflavored$pending(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData data,CallbackInfoReturnable<SpawnGroupData> cir){
  Mob mob=(Mob)(Object)this;
  if((reason==EntitySpawnReason.NATURAL||reason==EntitySpawnReason.CHUNK_GENERATION)&&(mob.getType()==EntityType.ZOMBIE||mob.getType()==EntityType.SPIDER))mob.getPersistentData().putBoolean("chocolateflavored:monster_spawn_pending",true);
 }
}
