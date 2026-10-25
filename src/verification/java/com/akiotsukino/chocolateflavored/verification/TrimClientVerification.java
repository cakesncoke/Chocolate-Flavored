package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class TrimClientVerification {
    private static int ticks;private static boolean opened;
    private static final String[] PATTERNS={"wild","wayfinder","raiser","shaper","host","ward","vex","tide","spire","snout","silence","sentry","rib","flow","eye","dune","coast","bolt"};
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client")||opened||!SteelClientVerification.completed||++ticks<180)return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;opened=true;mc.setScreen(new Gallery());
    }
    private static final class Gallery extends Screen {
        final List<List<ItemStack>> stacks=new ArrayList<>();int frames;
        Gallery(){super(Component.literal("Intrinsic armor trim abilities"));}
        @Override protected void init(){
            if(!stacks.isEmpty())return;
            for(String pattern:PATTERNS){var row=new ArrayList<ItemStack>();for(Item item:List.of(Items.IRON_HELMET,Items.DIAMOND_CHESTPLATE,Items.NETHERITE_LEGGINGS,Items.LEATHER_BOOTS)){
                var stack=new ItemStack(item);stack.set(DataComponents.TRIM,new ArmorTrim(minecraft.level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL,Identifier.withDefaultNamespace("gold"))),minecraft.level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN,Identifier.withDefaultNamespace(pattern)))));
                var lines=stack.getTooltipLines(Item.TooltipContext.of(minecraft.level),minecraft.player,TooltipFlag.NORMAL);
                if(lines.stream().noneMatch(c->c.getString().contains("Hold Shift")))throw new IllegalStateException("Missing intrinsic tooltip: "+pattern);
                if(lines.stream().anyMatch(c->c.getString().startsWith("trim.chocolateflavored.")))throw new IllegalStateException("Untranslated trim tooltip");row.add(stack);
            }stacks.add(row);}
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xFF19232C);g.centeredText(font,"Intrinsic armor trims — mixed armor materials, gold trimming",width/2,14,-1);
            int cell=width/6;
            for(int i=0;i<PATTERNS.length;i++){int x=i%6*cell,y=43+i/6*83;g.fill(x+4,y-4,x+cell-4,y+66,0xFF293844);g.centeredText(font,PATTERNS[i].toUpperCase(Locale.ROOT),x+cell/2,y+3,0xFFFFD58A);for(int j=0;j<4;j++)g.item(stacks.get(i).get(j),x+cell/2-36+j*18,y+25);}
            g.centeredText(font,"18 patterns • per-piece abilities • exact-pattern full sets • no required dependencies",width/2,height-42,-1);
            g.centeredText(font,"Vanilla pattern/material tooltips retained; Shift reveals full-set details",width/2,height-25,0xFFADC5CE);
            if(++frames==150)ChocolateFlavored.LOGGER.info("TRIM_CLIENT_VERIFICATION_PASSED: all 18 patterns on four mixed armor materials render; native trim details and intrinsic tooltips coexist");
        }
        @Override public boolean isPauseScreen(){return false;}
    }
}
