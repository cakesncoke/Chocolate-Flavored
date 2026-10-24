package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.equipment.SteelContent;
import com.akiotsukino.chocolateflavored.gameplay.MiningRules;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.bus.api.EventPriority;
import java.util.function.Consumer;

final class DeepslateMiningVerification {
    static void run(ServerLevel level,FakePlayer player) {
        BlockPos pos=new BlockPos(272,160,0);player.setPos(272.5,161,.5);player.setGameMode(GameType.SURVIVAL);
        var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var old=ItemStack.CODEC.parse(ops,JsonParser.parseString("{\"id\":\"chocolateflavored:iron_hammer\",\"count\":1,\"components\":{\"minecraft:damage\":19}}" )).getOrThrow();
        check(old.is(SteelContent.DEEPSLATE_HAMMER.get())&&old.getDamageValue()==19&&old.getMaxDamage()==500,"Iron hammer save migration");
        for(Block stone:List.of(Blocks.STONE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE)) {
            conversion(level,player,pos,stone,Blocks.AIR,ModContent.STONE_PEBBLE.get(),4);
            clear(level,pos);level.setBlockAndUpdate(pos,stone.defaultBlockState());var flint=new ItemStack(Items.FLINT,7);player.setItemInHand(InteractionHand.MAIN_HAND,flint);player.gameMode.destroyBlock(pos);
            check(level.getBlockState(pos).is(Blocks.COBBLESTONE)&&player.getMainHandItem().is(Items.FLINT)&&flint.getCount()==6,"Flint conversion consumes exactly one");drops(level,pos,ModContent.STONE_PEBBLE.get(),1);
        }
        conversion(level,player,pos,Blocks.COBBLESTONE,Blocks.AIR,ModContent.STONE_PEBBLE.get(),1);
        conversion(level,player,pos,Blocks.MOSSY_COBBLESTONE,Blocks.AIR,ModContent.STONE_PEBBLE.get(),1);
        conversion(level,player,pos,Blocks.DEEPSLATE,Blocks.COBBLED_DEEPSLATE,ModContent.DEEPSLATE_PEBBLE.get(),1);
        conversion(level,player,pos,Blocks.DEEPSLATE_COPPER_ORE,Blocks.COBBLED_DEEPSLATE,ModContent.COPPER_ORE_PILE.get(),1);
        conversion(level,player,pos,Blocks.DEEPSLATE_IRON_ORE,Blocks.COBBLED_DEEPSLATE,ModContent.IRON_ORE_PILE.get(),1);
        conversion(level,player,pos,Blocks.DEEPSLATE_GOLD_ORE,Blocks.COBBLED_DEEPSLATE,ModContent.GOLD_ORE_PILE.get(),1);
        conversion(level,player,pos,Blocks.IRON_ORE,Blocks.COBBLESTONE,ModContent.IRON_ORE_PILE.get(),1);
        conversion(level,player,pos,Blocks.GOLD_ORE,Blocks.COBBLESTONE,ModContent.GOLD_ORE_PILE.get(),1);
        for(Block ore:List.of(Blocks.COAL_ORE,Blocks.COPPER_ORE,Blocks.DEEPSLATE_COAL_ORE)) {
            for(boolean silk:new boolean[]{false,true}) {
                clear(level,pos);level.setBlockAndUpdate(pos,ore.defaultBlockState());var hammer=new ItemStack(SteelContent.DEEPSLATE_HAMMER.get());
                if(silk)hammer.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
                check(MiningRules.conversion(ore.defaultBlockState(),hammer)==null,"Native coal/copper path");
                player.setItemInHand(InteractionHand.MAIN_HAND,hammer);player.gameMode.destroyBlock(pos);
                check(level.getBlockState(pos).isAir()&&hammer.getDamageValue()==1,"Native ore removal and one wear");
                var entities=level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3));Item expected=silk?ore.asItem():ore==Blocks.COPPER_ORE?Items.RAW_COPPER:Items.COAL;
                check(!entities.isEmpty()&&entities.stream().allMatch(e->e.getItem().is(expected)),"Native ore loot / Silk Touch");
            }
        }
        var hammer=new ItemStack(SteelContent.DEEPSLATE_HAMMER.get());
        check(hammer.getDestroySpeed(Blocks.STONE.defaultBlockState())==ToolMaterial.STONE.speed()&&hammer.getDestroySpeed(Blocks.DIRT.defaultBlockState())==1,"Native pickaxe speed selection");
        check(!hammer.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState())&&!hammer.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),"Other ores retain stone-tier restrictions");
        check(MiningRules.conversion(Blocks.COAL_ORE.defaultBlockState(),new ItemStack(Items.FLINT))==null&&MiningRules.conversion(Blocks.COBBLESTONE.defaultBlockState(),new ItemStack(Items.FLINT))==null,"Flint converts only specified stones");
        clear(level,pos);level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT));player.gameMode.destroyBlock(pos);check(player.getMainHandItem().isEmpty(),"Final flint consumed");drops(level,pos,ModContent.STONE_PEBBLE.get(),1);
        Consumer<BreakBlockEvent> protection=e->{if(e.getPos().equals(pos))e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,protection);
        try {
            clear(level,pos);level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());var flint=new ItemStack(Items.FLINT,3);player.setItemInHand(InteractionHand.MAIN_HAND,flint);player.gameMode.destroyBlock(pos);
            check(level.getBlockState(pos).is(Blocks.STONE)&&flint.getCount()==3&&level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).isEmpty(),"Protected break consumes nothing");
        } finally { NeoForge.EVENT_BUS.unregister(protection); }
        clear(level,pos);player.setGameMode(GameType.CREATIVE);level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());var flint=new ItemStack(Items.FLINT,3);player.setItemInHand(InteractionHand.MAIN_HAND,flint);player.gameMode.destroyBlock(pos);check(level.getBlockState(pos).isAir()&&flint.getCount()==3,"Creative uses ordinary breaking");
        player.setGameMode(GameType.SURVIVAL);player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);level.removeBlock(pos,false);clear(level,pos);
        ChocolateFlavored.LOGGER.info("DEEPSLATE_MINING_VERIFICATION_PASSED: full stone/four pebbles, deepslate/ore conversions, gold piles, native coal/copper loot and Silk Touch, speed/tier/wear, saved hammer alias, consumed flint and protected/creative breaking");
    }
    private static void conversion(ServerLevel level,FakePlayer player,BlockPos pos,Block from,Block to,Item output,int count){
        clear(level,pos);level.setBlockAndUpdate(pos,from.defaultBlockState());var hammer=new ItemStack(SteelContent.DEEPSLATE_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);
        check(hammer.getDestroySpeed(from.defaultBlockState())==4&&hammer.isCorrectToolForDrops(from.defaultBlockState()),"Converted blocks use stone speed and harvest timing");
        player.gameMode.destroyBlock(pos);check(level.getBlockState(pos).is(to)&&hammer.getDamageValue()==1,"Conversion and single wear: "+from);drops(level,pos,output,count);
    }
    private static void drops(ServerLevel level,BlockPos pos,Item item,int count){var all=level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3));check(!all.isEmpty()&&all.stream().allMatch(e->e.getItem().is(item))&&all.stream().mapToInt(e->e.getItem().getCount()).sum()==count,"Exact output count without duplicate loot");}
    private static void clear(ServerLevel level,BlockPos pos){level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).forEach(ItemEntity::discard);}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Deepslate mining: "+message);}
}
