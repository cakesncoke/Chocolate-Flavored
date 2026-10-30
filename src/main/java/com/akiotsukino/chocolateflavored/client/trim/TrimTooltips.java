package com.akiotsukino.chocolateflavored.client.trim;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.TrimAbilities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class TrimTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e){
        var stack=e.getItemStack();String pattern=TrimAbilities.pattern(stack);boolean template=false;
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(pattern.isEmpty() && id.getNamespace().equals("minecraft") && id.getPath().endsWith("_armor_trim_smithing_template")){pattern="minecraft:"+id.getPath().replace("_armor_trim_smithing_template","");template=true;}
        if(!pattern.startsWith("minecraft:"))return;String name=pattern.substring(10);String key="trim.chocolateflavored."+name+".";
        if(!net.minecraft.locale.Language.getInstance().has(key+"theme"))return;
        var eq=stack.get(DataComponents.EQUIPPABLE);
        if(template){e.getToolTip().add(Component.translatable(key+"theme").withStyle(ChatFormatting.GRAY));e.getToolTip().add(Component.translatable(key+"full").withStyle(ChatFormatting.DARK_AQUA));return;}
        if(eq==null)return;
        String slot=eq.slot().getName();
        e.getToolTip().add(Component.translatable(key+slot).withStyle(ChatFormatting.AQUA));
        var window=Minecraft.getInstance().getWindow();
        boolean shift=InputConstants.isKeyDown(window,340)||InputConstants.isKeyDown(window,344);
        if(shift){e.getToolTip().add(Component.translatable(key+"full").withStyle(ChatFormatting.DARK_AQUA));e.getToolTip().add(Component.translatable("trim.chocolateflavored.stacking").withStyle(ChatFormatting.GRAY));}
        else e.getToolTip().add(Component.translatable("trim.chocolateflavored.more").withStyle(ChatFormatting.DARK_GRAY));
        var trim=stack.get(DataComponents.TRIM);
        if(trim!=null && trim.material().unwrapKey().map(k->k.identifier().toString().equals("minecraft:gold")).orElse(false))e.getToolTip().add(Component.translatable("trim.chocolateflavored.gold").withStyle(ChatFormatting.GOLD));
    }
}
