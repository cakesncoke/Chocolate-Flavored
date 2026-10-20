package com.akiotsukino.chocolateflavored.mixin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Preserve vanilla prey selectors and species checks for these vanilla-derived animals. */
@Mixin(Entity.class)
public abstract class VanillaAnimalIdentityMixin {
    @Inject(method = "is(Lnet/minecraft/world/entity/EntityType;)Z", at = @At("RETURN"), cancellable = true)
    private void chocolateflavored$species(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (cir.getReturnValue() || !BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals("chocolateflavored")) return;
        if (type == EntityType.COW && entity instanceof Cow || type == EntityType.PIG && entity instanceof Pig
                || type == EntityType.CHICKEN && entity instanceof Chicken || type == EntityType.SHEEP && entity instanceof Sheep
                || type == EntityType.RABBIT && entity instanceof Rabbit) cir.setReturnValue(true);
    }
}
