package com.akiotsukino.chocolateflavored.mob.expansion;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

/** Egg/command-only creatures. Deliberately registers no spawn placements or biome modifiers. */
public final class ExpansionContent {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ChocolateFlavored.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<SunkenSkeleton>> SUNKEN_SKELETON = ENTITIES.registerEntityType("sunken_skeleton", SunkenSkeleton::new, MobCategory.MONSTER, b -> b.sized(.6F,1.99F).eyeHeight(1.74F).notInPeaceful().clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<TuffGolem>> TUFF_GOLEM = ENTITIES.registerEntityType("tuff_golem", TuffGolem::new, MobCategory.MISC, b -> b.sized(.55F,.9F).eyeHeight(.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Barnacle>> BARNACLE = ENTITIES.registerEntityType("barnacle", Barnacle::new, MobCategory.MONSTER, b -> b.sized(.9F,1.2F).eyeHeight(.8F).notInPeaceful().clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Iceologer>> ICEOLOGER = ENTITIES.registerEntityType("iceologer", Iceologer::new, MobCategory.MONSTER, b -> b.sized(.6F,1.95F).eyeHeight(1.62F).notInPeaceful().clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Wildfire>> WILDFIRE = ENTITIES.registerEntityType("wildfire", Wildfire::new, MobCategory.MONSTER, b -> b.sized(1.3F,2.2F).eyeHeight(1.9F).fireImmune().notInPeaceful().clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<IceChunk>> ICE_CHUNK = ENTITIES.registerEntityType("ice_chunk", IceChunk::new, MobCategory.MISC, b -> b.sized(1.5F,1.5F).clientTrackingRange(10).updateInterval(1));
    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ExpansionContent::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent e) {
        e.put(SUNKEN_SKELETON.get(), AbstractSkeleton.createAttributes().build());
        e.put(TUFF_GOLEM.get(), TuffGolem.createAttributes().build());
        e.put(BARNACLE.get(), Barnacle.createAttributes().build());
        e.put(ICEOLOGER.get(), Iceologer.createAttributes().build());
        e.put(WILDFIRE.get(), Wildfire.createAttributes().build());
    }
    private ExpansionContent() {}
}
