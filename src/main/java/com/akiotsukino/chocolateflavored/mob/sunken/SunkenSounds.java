package com.akiotsukino.chocolateflavored.mob.sunken;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Original, unmodified Murk recordings from Variants & Ventures; see third-party notices. */
public final class SunkenSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,ChocolateFlavored.MODID);
    public static final DeferredHolder<SoundEvent,SoundEvent> AMBIENT=sound("ambient");
    public static final DeferredHolder<SoundEvent,SoundEvent> AMBIENT_WATER=sound("ambient_water");
    public static final DeferredHolder<SoundEvent,SoundEvent> HURT=sound("hurt");
    public static final DeferredHolder<SoundEvent,SoundEvent> HURT_WATER=sound("hurt_water");
    public static final DeferredHolder<SoundEvent,SoundEvent> DEATH=sound("death");
    public static final DeferredHolder<SoundEvent,SoundEvent> DEATH_WATER=sound("death_water");
    public static final DeferredHolder<SoundEvent,SoundEvent> ATTACK=sound("attack");
    public static final DeferredHolder<SoundEvent,SoundEvent> STEP=sound("step");
    public static final DeferredHolder<SoundEvent,SoundEvent> SHEAR=sound("shear");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){
        String path="entity.sunken."+name;
        return SOUNDS.register(path,()->SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID,path)));
    }
    public static void register(IEventBus bus){SOUNDS.register(bus);}
    private SunkenSounds(){}
}
