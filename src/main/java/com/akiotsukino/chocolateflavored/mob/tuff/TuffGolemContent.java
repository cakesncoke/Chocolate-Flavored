package com.akiotsukino.chocolateflavored.mob.tuff;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.world.entity.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import java.util.*;

/** Egg/command only. No biome, structure, construction or loot registrations. */
public final class TuffGolemContent {
    public static final DeferredRegister.Entities ENTITIES=DeferredRegister.createEntities(ChocolateFlavored.MODID);
    public static final DeferredHolder<EntityType<?>,EntityType<TuffGolem>> TUFF_GOLEM=ENTITIES.registerEntityType("tuff_golem",TuffGolem::new,MobCategory.MISC,b->b.sized(.75F,1F).eyeHeight(.7F).clientTrackingRange(8));
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT,ChocolateFlavored.MODID);
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUND_MAP=new HashMap<>();
    static { for(String key:List.of("hurt","step","sleep","wake","repair","glue_on","glue_off","move")) SOUND_MAP.put(key,SOUNDS.register("tuff_golem."+key,()->SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID,"tuff_golem."+key)))); }
    public static SoundEvent sound(String key){return SOUND_MAP.get(key).get();}
    public static void register(IEventBus bus){ENTITIES.register(bus);SOUNDS.register(bus);bus.addListener(TuffGolemContent::attributes);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(TUFF_GOLEM.get(),TuffGolem.createAttributes().build());}
    private TuffGolemContent(){}
}
