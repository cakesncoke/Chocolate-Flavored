package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModTags;
import com.akiotsukino.chocolateflavored.equipment.SteelContent;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;

final class SteelVerification {
    static void run(ServerLevel level, FakePlayer player) {
        BlockPos pos=new BlockPos(256,160,0);player.setPos(256.5,161,.5);player.setGameMode(GameType.SURVIVAL);
        ItemStack legacy=ItemStack.CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE),JsonParser.parseString("{\"id\":\"chocolateflavored:knapped_flint\",\"count\":1,\"components\":{\"minecraft:damage\":3}}" )).getOrThrow();
        check(legacy.is(SteelContent.STONE_HAMMER.get())&&legacy.getDamageValue()==3&&legacy.getMaxDamage()==32,"Old saved flint remaps to stone hammer");
        check(BuiltInRegistries.ITEM.keySet().stream().noneMatch(id->id.equals(Identifier.parse("chocolateflavored:knapped_flint"))),"Obsolete item has no registry entry");
        int index=0;
        for(Item hammer:List.of(SteelContent.STONE_HAMMER.get(),SteelContent.DEEPSLATE_HAMMER.get())) {
            ItemStack stack=new ItemStack(hammer);check(stack.is(ModTags.HAMMERS),"Hammer tag");
            check(stack.getMaxDamage()==(index==0?32:new ItemStack(Items.IRON_PICKAXE).getMaxDamage()),"Durability matches requested tier");
            var attributes=stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            double[] values={attributes.compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND),attributes.compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND)};
            check(Math.abs(values[0]-(index==0?11:12))<.001&&Math.abs(values[1]-(index==0?.6:.7))<.001,"Heavy damage and slow attack speed");
            var axe=new ItemStack(index==0?Items.STONE_AXE:Items.IRON_AXE).get(DataComponents.ATTRIBUTE_MODIFIERS);
            check(values[0]>axe.compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)&&values[1]<axe.compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND),"Stronger and slower than corresponding native axe");
            if(index==0) for(Block block:List.of(Blocks.STONE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.COBBLESTONE,Blocks.MOSSY_COBBLESTONE,Blocks.COAL_ORE,Blocks.COPPER_ORE)) {
                clear(level,pos);level.setBlockAndUpdate(pos,block.defaultBlockState());stack=new ItemStack(hammer);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                check(stack.getDestroySpeed(block.defaultBlockState())==1,"Hands mining speed");
                player.gameMode.destroyBlock(pos);
                Block expected=block==Blocks.COBBLESTONE||block==Blocks.MOSSY_COBBLESTONE?Blocks.AIR:Blocks.COBBLESTONE;
                check(level.getBlockState(pos).is(expected)&&stack.getDamageValue()==1,"Real conversion and exactly one wear: "+block);
                Item drop=block==Blocks.COAL_ORE?com.akiotsukino.chocolateflavored.content.ModContent.COAL_DUST.get():block==Blocks.COPPER_ORE?com.akiotsukino.chocolateflavored.content.ModContent.COPPER_ORE_PILE.get():com.akiotsukino.chocolateflavored.content.ModContent.STONE_PEBBLE.get();
                check(level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).stream().map(ItemEntity::getItem).allMatch(s->s.is(drop))&&level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).stream().mapToInt(e->e.getItem().getCount()).sum()==1,"One primitive output; no duplicated vanilla loot");
            }
            // The native successful-hit lifecycle charges one durability, not a second mining charge.
            stack=new ItemStack(hammer);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var target=net.minecraft.world.entity.EntityType.COW.create(level,EntitySpawnReason.COMMAND);
            check(stack.hurtEnemy(target,player),"Native weapon hit component");stack.postHurtEnemy(target,player);check(stack.getDamageValue()==1,"One wear per successful weapon hit");
            var broken=new ItemStack(hammer);broken.setDamageValue(broken.getMaxDamage()-1);player.setItemInHand(InteractionHand.MAIN_HAND,broken);level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());player.gameMode.destroyBlock(pos);check(player.getMainHandItem().isEmpty(),"Final conversion breaks hammer");
            var repair=new ItemStack(hammer);repair.setDamageValue(20);var menu=new AnvilMenu(5,player.getInventory());menu.getSlot(0).set(repair);menu.getSlot(1).set(new ItemStack(index==0?Items.COBBLESTONE:Items.IRON_INGOT));menu.createResult();check(menu.getSlot(2).hasItem()&&menu.getCost()==0,"Native material repair uses existing free anvil rule");
            index++;
        }
        clear(level,pos);var steel=SteelContent.STEEL_BLOCK.get();level.setBlockAndUpdate(pos,steel.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));player.gameMode.destroyBlock(pos);
        check(level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).stream().anyMatch(e->e.getItem().is(SteelContent.STEEL_BLOCK_ITEM.get())),"Steel block drops itself with correct tool");
        clear(level,pos);level.setBlockAndUpdate(pos,steel.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SteelContent.STONE_HAMMER.get()));player.gameMode.destroyBlock(pos);
        check(level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).isEmpty(),"Hammer is not a general-purpose pickaxe");
        for(var material:List.of(SteelContent.STEEL_INGOT,SteelContent.STEEL_NUGGET,SteelContent.STEEL_PLATE,SteelContent.DIAMOND_PLATE,SteelContent.TANNED_LEATHER,SteelContent.CUT_TANNED_LEATHER,SteelContent.STRAP,SteelContent.PADDING,SteelContent.WOOL,SteelContent.CREEPER_OYSTERS,SteelContent.DIAMOND_ARMOR_PLATE))check(material.get().getDefaultInstance().getMaxStackSize()==64&&!material.get().getDefaultInstance().has(DataComponents.CONSUMABLE),"Plain material item");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);level.removeBlock(pos,false);clear(level,pos);
        ChocolateFlavored.LOGGER.info("STEEL_VERIFICATION_PASSED: hammer conversion/drop/wear matrix, durability and combat components, last-use breakage, material repairs, old flint save alias, steel block harvesting and all eleven materials");
    }
    private static void clear(ServerLevel level,BlockPos pos){level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).forEach(ItemEntity::discard);}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Steel/hammers: "+message);}
}
