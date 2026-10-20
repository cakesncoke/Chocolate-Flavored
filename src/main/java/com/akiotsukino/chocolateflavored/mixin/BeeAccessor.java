package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Bee.class)
public interface BeeAccessor {
    @Invoker("getCropsGrownSincePollination") int chocolateflavored$crops();
    @Invoker("isHiveValid") boolean chocolateflavored$hiveValid();
    @Invoker("incrementNumCropsGrownSincePollination") void chocolateflavored$incrementCrops();
}
