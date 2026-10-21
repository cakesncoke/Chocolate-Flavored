package com.akiotsukino.chocolateflavored.content;

import com.akiotsukino.chocolateflavored.animal.AnimalContent;
import com.akiotsukino.chocolateflavored.mob.MobContent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TypedEntityData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Every obtainable egg belongs to both the mod tab and vanilla Spawn Eggs. */
public final class SpawnEggContent {
    public static final List<DeferredItem<SpawnEggItem>> EGGS = new ArrayList<>();
    static {
        egg("sunken", com.akiotsukino.chocolateflavored.mob.sunken.SunkenContent.SUNKEN);
        egg("tuff_golem", com.akiotsukino.chocolateflavored.mob.tuff.TuffGolemContent.TUFF_GOLEM);
        egg("dairy_cow", AnimalContent.DAIRY_COW);
        egg("moobloom", AnimalContent.MOOBLOOM);
        egg("moolip", AnimalContent.MOOLIP);
        egg("pale_pig", AnimalContent.PALE_PIG);
        egg("bronzed_chicken", AnimalContent.BRONZED_CHICKEN);
        egg("fancy_chicken", AnimalContent.FANCY_CHICKEN);
        egg("fuzzy_sheep", AnimalContent.FUZZY_SHEEP);
        egg("long_nose_sheep", AnimalContent.LONG_NOSE_SHEEP);
        egg("patched_sheep", AnimalContent.PATCHED_SHEEP);
        egg("horned_sheep", AnimalContent.HORNED_SHEEP);
        egg("jumbo_rabbit", AnimalContent.JUMBO_RABBIT);
        egg("melon_golem", MobContent.MELON_GOLEM);
        egg("bouldering_zombie", MobContent.BOULDERING_ZOMBIE);
        egg("swamped", MobContent.SWAMPED);
        egg("skeleton_wolf", MobContent.SKELETON_WOLF);
        egg("bone_spider", MobContent.BONE_SPIDER);
        egg("tropical_slime", MobContent.TROPICAL_SLIME);
        egg("husk_rabbit", MobContent.HUSK_RABBIT);
        EGGS.add(ModContent.ITEMS.registerItem("muddy_pig_spawn_egg", SpawnEggItem::new, () -> {
            CompoundTag state = new CompoundTag();
            state.putInt("state", 1);
            CompoundTag attachments = new CompoundTag();
            attachments.put("chocolateflavored:muddy_pig_state", state);
            CompoundTag data = new CompoundTag();
            data.put("neoforge:attachments", attachments);
            return new Item.Properties().spawnEgg(EntityType.PIG)
                    .component(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.PIG, data));
        }));
    }
    private static void egg(String name, Supplier<? extends EntityType<?>> type) {
        EGGS.add(ModContent.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, () -> new Item.Properties().spawnEgg(type.get())));
    }
    public static void register(IEventBus bus) { bus.addListener(SpawnEggContent::creativeTab); }
    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) EGGS.forEach(egg -> event.accept(egg.get()));
    }
    private SpawnEggContent() {}
}
