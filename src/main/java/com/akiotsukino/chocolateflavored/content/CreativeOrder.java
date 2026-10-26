package com.akiotsukino.chocolateflavored.content;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
public final class CreativeOrder {
    public static int group(Item item) {
        var stack=item.getDefaultInstance();
        if(item instanceof SpawnEggItem)return 5;
        if(item instanceof BlockItem)return 0;
        if(stack.has(DataComponents.TOOL)||stack.has(DataComponents.WEAPON)||stack.has(DataComponents.EQUIPPABLE)||item instanceof SmithingTemplateItem)return 1;
        if(stack.has(DataComponents.FOOD))return 3;
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        if(id.contains("seed")||id.contains("crop")||id.contains("fiber")||id.contains("cotton"))return 4;
        return 2;
    }
    private CreativeOrder(){}
}
