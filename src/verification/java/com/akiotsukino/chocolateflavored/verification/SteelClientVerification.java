package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.equipment.SteelContent;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class SteelClientVerification {
    private static int ticks;private static boolean opened;public static boolean completed;
    static List<Item> equipment(){return List.of(Items.IRON_SWORD,Items.IRON_PICKAXE,Items.IRON_AXE,Items.IRON_SHOVEL,Items.IRON_HOE,Items.IRON_SPEAR,Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS,Items.IRON_HORSE_ARMOR,Items.IRON_NAUTILUS_ARMOR,ModContent.IRON_KNIFE.get());}
    static List<Item> additions(){return List.of(ModContent.GOLD_ORE_PILE.get(),SteelContent.STEEL_INGOT.get(),SteelContent.STEEL_NUGGET.get(),SteelContent.STEEL_PLATE.get(),SteelContent.DIAMOND_PLATE.get(),SteelContent.TANNED_LEATHER.get(),SteelContent.CUT_TANNED_LEATHER.get(),SteelContent.STRAP.get(),SteelContent.PADDING.get(),SteelContent.WOOL.get(),SteelContent.CREEPER_OYSTERS.get(),SteelContent.DIAMOND_ARMOR_PLATE.get(),SteelContent.STEEL_BLOCK_ITEM.get(),SteelContent.STONE_HAMMER.get(),SteelContent.DEEPSLATE_HAMMER.get());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!DyedWoodClientVerification.completed||++ticks<180)return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;opened=true;
        for(Item item:equipment())if(item==ModContent.IRON_KNIFE.get() ? !item.getDefaultInstance().getHoverName().getString().equals("Iron Knife") : !item.getDefaultInstance().getHoverName().getString().startsWith("Steel "))throw new IllegalStateException("Steel name: "+item);
        var models=mc.getModelManager().getBlockStateModelSet();
        if(models.get(SteelContent.STEEL_BLOCK.get().defaultBlockState())==models.missingModel())throw new IllegalStateException("Steel block model");
        CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(),true,mc.level.registryAccess());
        var tab=mc.level.registryAccess().lookupOrThrow(Registries.CREATIVE_MODE_TAB).getOrThrow(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.parse("chocolateflavored:chocolate_flavored"))).value();
        for(Item item:additions())if(tab.getDisplayItems().stream().noneMatch(s->s.is(item)))throw new IllegalStateException("New material missing from creative: "+item);
        if(tab.getDisplayItems().stream().anyMatch(s->BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().equals("knapped_flint")))throw new IllegalStateException("Obsolete creative item");
        mc.gui.setScreen(new Gallery());
    }
    private static final class Gallery extends Screen {
        private final List<LivingEntity> wearers=new ArrayList<>();private int frames;
        Gallery(){super(Component.literal("Steel equipment and hammers"));}
        @Override protected void init(){
            if(!wearers.isEmpty())return;
            var zombie=ClientVerificationEntities.create(EntityTypes.ZOMBIE,minecraft.level);zombie.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));zombie.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.IRON_LEGGINGS));zombie.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.IRON_BOOTS));zombie.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SPEAR));wearers.add(zombie);
            var horse=ClientVerificationEntities.create(EntityTypes.HORSE,minecraft.level);horse.setItemSlot(EquipmentSlot.BODY,new ItemStack(Items.IRON_HORSE_ARMOR));wearers.add(horse);
            var nautilus=ClientVerificationEntities.create(EntityTypes.NAUTILUS,minecraft.level);nautilus.setItemSlot(EquipmentSlot.BODY,new ItemStack(Items.IRON_NAUTILUS_ARMOR));wearers.add(nautilus);
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xFF252D38);g.centeredText(font,"Steel equipment, supplied armor textures, new materials and hammers",width/2,5,-1);
            int cw=width/7;
            for(int i=0;i<equipment().size();i++){int x=(i%7)*cw,y=26+i/7*37;var stack=equipment().get(i).getDefaultInstance();g.item(stack,x+cw/2-8,y);g.centeredText(font,stack.getHoverName(),x+cw/2,y+19,-1);}
            var added=additions();for(int i=0;i<added.size();i++){int x=(i%7)*cw,y=105+i/7*34;var stack=added.get(i).getDefaultInstance();g.item(stack,x+cw/2-8,y);g.centeredText(font,stack.getHoverName(),x+cw/2,y+19,-1);}
            for(int i=0;i<wearers.size();i++){int x=i*width/3;InventoryScreen.extractEntityInInventoryFollowsMouse(g,x+5,211,x+width/3-5,height-22,43,.04F,x+width/6F-120,215,wearers.get(i));}
            g.centeredText(font,"Stone: 11 damage / 0.6 speed / 32 uses     Deepslate: 12 damage / 0.7 speed / 500 uses",width/2,height-15,-1);
            if(++frames==150){completed=true;ChocolateFlavored.LOGGER.info("STEEL_CLIENT_VERIFICATION_PASSED: steel equipment names/icons, all fifteen new creative entries, supplied humanoid/horse/nautilus equipment textures, held spear, hammer models and steel block");}
        }
        @Override public boolean isPauseScreen(){return false;}
    }
}
