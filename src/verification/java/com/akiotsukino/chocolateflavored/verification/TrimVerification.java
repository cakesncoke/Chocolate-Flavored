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
        set(l,p,"ward");for(int i=0;i<10;i++)TrimAttributes.update(p);check(p.getMaxHealth()==base+10,"Ward idempotence");p.setHealth(p.getMaxHealth());
        var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,l.registryAccess());p.saveWithoutId(out);
        var copy=new FakePlayer(l,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"TrimReload"));copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,l.registryAccess(),out.buildResult()));
        check(copy.getMaxHealth()==30 && copy.getHealth()==30,"Ward health survives actual save/load without duplicate attributes");
        clear(p);check(p.getMaxHealth()==base && p.getHealth()==base,"Ward removal caps current health");
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
        TrimTriggers.start(p,"vex",600);check(p.getCooldowns().getCooldownPercent(p.getItemBySlot(FEET),0)>0 && !p.getCooldowns().isOnCooldown(p.getItemBySlot(FEET)),"Trim overlay never blocks ordinary armor equipping");clear(p);set(l,p,"vex");check(!TrimTriggers.ready(p,"vex"),"Cooldown survives equipment swapping");p.removeAllEffects();p.setHealth(20);
        set(l,p,"eye");BlockPos start=new BlockPos(320,160,0);
        for(int i=0;i<20;i++)l.setBlockAndUpdate(start.east(i),Blocks.DEEPSLATE_IRON_ORE.defaultBlockState());
        p.setItemSlot(MAINHAND,new ItemStack(Items.IRON_PICKAXE));p.gameMode.destroyBlock(start);
        for(int i=0;i<20;i++)check(l.getBlockState(start.east(i)).is(i<16?Blocks.COBBLED_DEEPSLATE:Blocks.DEEPSLATE_IRON_ORE),"Vein cap and native deepslate conversion "+i);
        check(p.getMainHandItem().getDamageValue()==16,"Native wear per vein block");
        for(int i=0;i<3;i++)l.setBlockAndUpdate(start.east(i),Blocks.COAL_ORE.defaultBlockState());
        Consumer<BreakBlockEvent> protection=e->{if(e.getPos().equals(start.east()))e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,protection);
        try{p.gameMode.destroyBlock(start);check(l.getBlockState(start.east()).is(Blocks.COAL_ORE)&&l.getBlockState(start.east(2)).is(Blocks.COAL_ORE),"Protected ore stops traversal");}finally{NeoForge.EVENT_BUS.unregister(protection);}
        for(int i=0;i<20;i++)l.removeBlock(start.east(i),false);l.getEntitiesOfClass(ItemEntity.class,new AABB(start).inflate(25)).forEach(ItemEntity::discard);
        mechanics(l,p);
        clear(p);p.setItemSlot(MAINHAND,ItemStack.EMPTY);p.setHealth(p.getMaxHealth());
        ChocolateFlavored.LOGGER.info("TRIM_VERIFICATION_PASSED: all 18 pattern/slot combinations, mixed materials, reach, Ward lifecycle, native protection and fishing, air, glider, gold neutrality, Dune immunity, Vex atomic costs, persistent cooldowns and native/protected 16-block vein mining");
    }
    private static float incoming(FakePlayer target,net.minecraft.world.damagesource.DamageSource source){
        var event=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(target,new net.neoforged.neoforge.common.damagesource.DamageContainer(source,20));TrimCombat.incoming(event);return event.getAmount();
    }
    private static void mechanics(ServerLevel l,FakePlayer p){
        for(String pattern:List.of("sentry","dune")){set(l,p,pattern);var arrow=EntityType.ARROW.create(l,EntitySpawnReason.COMMAND);check(EnchantmentHelper.getDamageProtection(l,p,p.damageSources().arrow(arrow,p))==4,"Projectile protection II "+pattern);}
        set(l,p,"rib");check(EnchantmentHelper.getDamageProtection(l,p,p.damageSources().inFire())==8,"Fire protection IV");check(Math.abs(p.getAttributeValue(Attributes.BURNING_TIME)-.4)<.00001,"Fire protection burn duration");
        check(incoming(p,p.damageSources().lava())==0,"First lava hit protected");p.getPersistentData().putLong(TrimTriggers.key("lava"),0);check(incoming(p,p.damageSources().lava())==20,"Lava cooldown cannot reset on reentry");
        set(l,p,"flow");check(p.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE)==.3,"Blast knockback resistance");p.fallDistance=10;check(incoming(p,p.damageSources().fall())==15,"Flow fall reduction");
        set(l,p,"spire");check(incoming(p,p.damageSources().flyIntoWall())==5,"Spire wall reduction");check(incoming(p,p.damageSources().fall())==10,"Spire fall reduction");
        p.setOnGround(false);p.setShiftKeyDown(true);check(p.hasEffect(MobEffects.LEVITATION),"Infinite crouch levitation available");p.setShiftKeyDown(false);check(!p.hasEffect(MobEffects.LEVITATION),"Levitation stops on release");
        set(l,p,"eye");p.setPos(320.5,-1,.5);check(p.hasEffect(MobEffects.NIGHT_VISION),"Eye low-altitude night vision");p.setPos(320.5,161,.5);check(!p.hasEffect(MobEffects.NIGHT_VISION),"Eye vision stops above zero");
        set(l,p,"bolt");check(incoming(p,p.damageSources().lightningBolt())==0,"Bolt lightning immunity");
        var illager=EntityType.VINDICATOR.create(l,EntitySpawnReason.COMMAND);var drowned=EntityType.DROWNED.create(l,EntitySpawnReason.COMMAND);var zombie=EntityType.ZOMBIE.create(l,EntitySpawnReason.COMMAND);var warden=EntityType.WARDEN.create(l,EntitySpawnReason.COMMAND);var brute=EntityType.PIGLIN_BRUTE.create(l,EntitySpawnReason.COMMAND);
        set(l,p,"vex");check(p.getVisibilityPercent(illager)==.5,"Vex detection");set(l,p,"snout");check(p.getVisibilityPercent(brute)==.5,"Snout brute detection");set(l,p,"coast");check(p.getVisibilityPercent(drowned)==.5,"Coast detection");set(l,p,"silence");check(p.getVisibilityPercent(zombie)==.6&&p.getVisibilityPercent(warden)==1,"Silence detection / Warden exception");
        clear(p);int load=CrossbowItem.getChargeDuration(new ItemStack(Items.CROSSBOW),p);set(l,p,"sentry");check(CrossbowItem.getChargeDuration(new ItemStack(Items.CROSSBOW),p)==(int)Math.ceil(load*.8),"Crossbow load duration");
        set(l,p,"wild");var leaf=net.minecraft.world.phys.shapes.CollisionContext.of(p);BlockPos leaves=p.blockPosition();check(leaf.getCollisionShape(Blocks.OAK_LEAVES.defaultBlockState(),l,leaves).isEmpty(),"Horizontal leaf collision removed");p.setPos(320.5,162,.5);leaf=net.minecraft.world.phys.shapes.CollisionContext.of(p);check(!leaf.getCollisionShape(Blocks.OAK_LEAVES.defaultBlockState(),l,leaves).isEmpty(),"Standing on leaf collision retained");p.setShiftKeyDown(true);leaf=net.minecraft.world.phys.shapes.CollisionContext.of(p);check(leaf.getCollisionShape(Blocks.OAK_LEAVES.defaultBlockState(),l,leaves).isEmpty(),"Crouch descends through leaves");p.setShiftKeyDown(false);
        archaeology(l,p);
        p.setPos(320.5,161,.5);clear(p);
    }
    private static void archaeology(ServerLevel l,FakePlayer p){
        BlockPos pos=new BlockPos(350,160,0);l.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(pos,Blocks.SUSPICIOUS_SAND.defaultBlockState());
        var be=(net.minecraft.world.level.block.entity.BrushableBlockEntity)l.getBlockEntity(pos);
        var table=ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("minecraft:archaeology/desert_pyramid"));be.setLootTable(table,123);set(l,p,"dune");var brush=new ItemStack(Items.BRUSH);
        for(int i=0;i<9;i++)check(!be.brush(1000+i*10,l,p,Direction.UP,brush),"Only completed brushing can preserve");
        long seed=0;while(true){l.getRandom().setSeed(seed);if(l.getRandom().nextFloat()<.2)break;seed++;}l.getRandom().setSeed(seed);
        check(be.brush(1090,l,p,Direction.UP,brush)&&l.getBlockState(pos).is(Blocks.SUSPICIOUS_SAND),"Legitimate completed brushing preserved");
        var saved=be.saveWithFullMetadata(l.registryAccess());var reloaded=net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos,l.getBlockState(pos),saved,l.registryAccess());reloaded.setLevel(l);l.setBlockEntity(reloaded);be=(net.minecraft.world.level.block.entity.BrushableBlockEntity)reloaded;
        for(int i=0;i<10;i++)be.brush(2000+i*10,l,p,Direction.UP,brush);
        check(l.getBlockState(pos).is(Blocks.SAND),"Second extraction depletes preserved block after reload");check(!ArchaeologyMemory.claim(l,pos),"Position remains spent after replacing block");
        var items=l.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3));check(items.size()==2,"Two independent archaeology extractions");items.forEach(ItemEntity::discard);l.removeBlock(pos,false);l.removeBlock(pos.below(),false);
    }
    private static void check(boolean b,String s){if(!b)throw new IllegalStateException("Trim verification: "+s);}
}
