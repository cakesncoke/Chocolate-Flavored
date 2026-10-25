package com.akiotsukino.chocolateflavored.verification;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.*;
import com.akiotsukino.chocolateflavored.content.ModContent;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import java.util.*;
import java.util.function.Consumer;
import static net.minecraft.world.entity.EquipmentSlot.*;
final class TrimVerification {
    private static final String[] PATTERNS={"wild","wayfinder","raiser","shaper","host","ward","vex","tide","spire","snout","silence","sentry","rib","flow","eye","dune","coast","bolt"};
    static ItemStack armor(ServerLevel l,Item item,String pattern,String material){
        var stack=new ItemStack(item);stack.set(DataComponents.TRIM,new ArmorTrim(l.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL,Identifier.withDefaultNamespace(material))),l.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN,Identifier.withDefaultNamespace(pattern)))));return stack;
    }
    static void set(ServerLevel l,FakePlayer p,String pattern){
        p.setItemSlot(HEAD,armor(l,Items.IRON_HELMET,pattern,"gold"));
        p.setItemSlot(CHEST,armor(l,Items.DIAMOND_CHESTPLATE,pattern,"iron"));
        p.setItemSlot(LEGS,armor(l,Items.NETHERITE_LEGGINGS,pattern,"redstone"));
        p.setItemSlot(FEET,armor(l,Items.LEATHER_BOOTS,pattern,"quartz"));
        TrimAttributes.update(p);
    }
    static void clear(FakePlayer p){for(var slot:TrimAbilities.ARMOR)p.setItemSlot(slot,ItemStack.EMPTY);TrimAttributes.update(p);}
    static void run(ServerLevel l,FakePlayer p){
        p.setGameMode(GameType.SURVIVAL);p.setPos(320.5,161,.5);clear(p);float base=p.getMaxHealth();
        for(String pattern:PATTERNS){
            set(l,p,pattern);check(TrimAbilities.full(p,pattern),"mixed armor/material full set: "+pattern);
            for(var slot:TrimAbilities.ARMOR){var saved=p.getItemBySlot(slot);p.setItemSlot(slot,ItemStack.EMPTY);check(!TrimAbilities.full(p,pattern),"missing slot "+pattern+slot);p.setItemSlot(slot,saved);}
        }
        set(l,p,"ward");for(int i=0;i<10;i++)TrimAttributes.update(p);check(p.getMaxHealth()==base+10,"Ward idempotence");p.setHealth(p.getMaxHealth());clear(p);check(p.getMaxHealth()==base && p.getHealth()==base,"Ward removal caps current health");
        set(l,p,"wayfinder");check(p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)==7.5,"Trail reach +3");p.setItemSlot(FEET,armor(l,Items.LEATHER_BOOTS,"host","gold"));TrimAttributes.update(p);check(!TrimAbilities.trailFull(p)&&p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)==5.5,"Mixed Trail patterns no full set");
        set(l,p,"wild");check(Math.abs(p.getAttributeValue(Attributes.STEP_HEIGHT)-1)<.00001,"Wild step height");check(EnchantmentHelper.getDamageProtection(l,p,p.damageSources().fall())==6,"Intrinsic Feather Falling II");
        p.getItemBySlot(FEET).enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING),4);check(EnchantmentHelper.getDamageProtection(l,p,p.damageSources().fall())==18,"Feather Falling stacks in native protection input");
        set(l,p,"tide");check(p.getMaxAirSupply()==2700,"Tide full air");p.setItemSlot(FEET,ItemStack.EMPTY);check(p.getMaxAirSupply()==1500,"Tide chest air");p.setAirSupply(1500);clear(p);check(p.getMaxAirSupply()==300 && p.getAirSupply()==300,"Air clamp on removal");
        set(l,p,"spire");check(LivingEntity.canGlideUsing(p.getItemBySlot(CHEST),CHEST),"Spire participates in native glider selection");
        set(l,p,"dune");check(!p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100)),"Dune rejects blindness");check(!p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,100)),"Dune rejects darkness");
        set(l,p,"coast");check(EnchantmentHelper.getFishingLuckBonus(l,new ItemStack(Items.FISHING_ROD),p)==2,"Coast native fishing luck");
        clear(p);p.setItemSlot(FEET,armor(l,Items.LEATHER_BOOTS,"ward","gold"));check(PiglinAi.isWearingSafeArmor(p),"Gold-trimmed leather is safe");clear(p);check(!PiglinAi.isWearingSafeArmor(p),"Removing gold trim restores native neutrality");
        set(l,p,"snout");check(PiglinAi.isWearingSafeArmor(p),"Snout safe armor");
        set(l,p,"vex");var damage=p.damageSources().generic();int[] costs=Arrays.stream(TrimAbilities.ARMOR).mapToInt(s->(p.getItemBySlot(s).getMaxDamage()+2)/3).toArray();check(TrimTriggers.resurrect(p,damage),"Vex resurrection");
        for(int i=0;i<4;i++)check(p.getItemBySlot(TrimAbilities.ARMOR[i]).getDamageValue()==costs[i],"Atomic exact Vex durability cost");
        check(p.getHealth()==1 && p.hasEffect(MobEffects.REGENERATION)&&p.hasEffect(MobEffects.FIRE_RESISTANCE),"Native Totem effects");
        check(!TrimTriggers.resurrect(p,p.damageSources().fellOutOfWorld()),"No void resurrection");
        p.getItemBySlot(HEAD).setDamageValue(p.getItemBySlot(HEAD).getMaxDamage()-1);int chestDamage=p.getItemBySlot(CHEST).getDamageValue();check(!TrimTriggers.resurrect(p,damage)&&p.getItemBySlot(CHEST).getDamageValue()==chestDamage,"Unaffordable resurrection has no partial costs");
        TrimTriggers.start(p,"vex",600);clear(p);set(l,p,"vex");check(!TrimTriggers.ready(p,"vex"),"Cooldown survives equipment swapping");p.removeAllEffects();p.setHealth(20);
        set(l,p,"eye");BlockPos start=new BlockPos(320,160,0);
        for(int i=0;i<20;i++)l.setBlockAndUpdate(start.east(i),Blocks.DEEPSLATE_IRON_ORE.defaultBlockState());
        p.setItemSlot(MAINHAND,new ItemStack(Items.IRON_PICKAXE));p.gameMode.destroyBlock(start);
        for(int i=0;i<20;i++)check(l.getBlockState(start.east(i)).is(i<16?Blocks.COBBLED_DEEPSLATE:Blocks.DEEPSLATE_IRON_ORE),"Vein cap and native deepslate conversion "+i);
        check(p.getMainHandItem().getDamageValue()==16,"Native wear per vein block");
        for(int i=0;i<3;i++)l.setBlockAndUpdate(start.east(i),Blocks.COAL_ORE.defaultBlockState());
        Consumer<BreakBlockEvent> protection=e->{if(e.getPos().equals(start.east()))e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,protection);
        try{p.gameMode.destroyBlock(start);check(l.getBlockState(start.east()).is(Blocks.COAL_ORE)&&l.getBlockState(start.east(2)).is(Blocks.COAL_ORE),"Protected ore stops traversal");}finally{NeoForge.EVENT_BUS.unregister(protection);}
        for(int i=0;i<20;i++)l.removeBlock(start.east(i),false);l.getEntitiesOfClass(ItemEntity.class,new AABB(start).inflate(25)).forEach(ItemEntity::discard);
        clear(p);p.setItemSlot(MAINHAND,ItemStack.EMPTY);p.setHealth(p.getMaxHealth());
        ChocolateFlavored.LOGGER.info("TRIM_VERIFICATION_PASSED: all 18 pattern/slot combinations, mixed materials, reach, Ward lifecycle, native protection and fishing, air, glider, gold neutrality, Dune immunity, Vex atomic costs, persistent cooldowns and native/protected 16-block vein mining");
    }
    private static void check(boolean b,String s){if(!b)throw new IllegalStateException("Trim verification: "+s);}
}
