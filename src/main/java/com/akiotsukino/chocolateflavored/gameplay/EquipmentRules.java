package com.akiotsukino.chocolateflavored.gameplay;

import com.akiotsukino.chocolateflavored.content.ModTags;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public final class EquipmentRules {
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modifyMatching((item, components) -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft")
                && components.get(DataComponents.MAX_DAMAGE) != null,
                (components, context, item) -> {
                    int original = components.build().get(DataComponents.MAX_DAMAGE);
                    String path = BuiltInRegistries.ITEM.getKey(item).getPath();
                    int durability = item == Items.WOODEN_SWORD || item == Items.STONE_AXE ? 32
                            : item == Items.FISHING_ROD ? original * 3
                            : path.startsWith("golden_") ? original : original * 2;
                    components.set(DataComponents.MAX_DAMAGE, durability);
                });
        event.modify(Items.COPPER_PICKAXE, (components, context, item) -> {
            Tool previous = components.build().get(DataComponents.TOOL);
            if (previous == null) return;
            var rules = new ArrayList<Tool.Rule>();
            // First matching rule wins for speed and harvesting independently.
            rules.add(new Tool.Rule(context.lookupOrThrow(Registries.BLOCK).getOrThrow(ModTags.DEEPSLATE),
                    Optional.of(1.0F), Optional.of(false)));
            rules.addAll(previous.rules());
            components.set(DataComponents.TOOL, new Tool(rules, previous.defaultMiningSpeed(),
                    previous.damagePerBlock(), previous.canDestroyBlocksInCreative()));
        });
    }
    private EquipmentRules() {}
}
