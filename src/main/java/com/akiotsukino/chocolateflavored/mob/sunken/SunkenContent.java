package com.akiotsukino.chocolateflavored.mob.sunken;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
public final class SunkenContent {
    public static final DeferredHolder<EntityType<?>,EntityType<Sunken>> SUNKEN=MobContent.ENTITIES.registerEntityType("sunken",Sunken::new,MobCategory.MONSTER,b->b.sized(.6F,1.99F).eyeHeight(1.74F).notInPeaceful().clientTrackingRange(8));
    public static void register(IEventBus bus){SunkenSounds.register(bus);bus.addListener(SunkenContent::attributes);NeoForge.EVENT_BUS.addListener(SkeletonDrowning::tick);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(SUNKEN.get(),AbstractSkeleton.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,16).build());}
    private SunkenContent(){}
}
