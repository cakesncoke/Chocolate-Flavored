package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.dye.*;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

final class DyeingVerification {
    static void run(ServerLevel level, FakePlayer player) {
        BlockPos pos = new BlockPos(240,160,0);
        player.setGameMode(GameType.SURVIVAL);player.setPos(240.5,161,.5);
        for (DyeColor color : DyeColor.values()) {
            var f=DyedWood.FAMILIES.get(color);
            check(new ItemStack(f.item("planks")).is(ItemTags.PLANKS),"Plank crafting tag");
            for (Item plank : List.of(Items.OAK_PLANKS,Items.BAMBOO_PLANKS,Items.WARPED_PLANKS,Items.CRIMSON_PLANKS,Items.PALE_OAK_PLANKS)) {
                var input=CraftingInput.of(2,1,List.of(new ItemStack(plank),new ItemStack(item(color.getName()+"_dye"))));
                var recipe=level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level);
                check(recipe.isPresent()&&recipe.get().value().assemble(input).is(f.item("planks")),"Native plank dye recipe");
            }
            var stick=CraftingInput.of(1,2,List.of(new ItemStack(f.item("planks")),new ItemStack(f.item("planks"))));
            check(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,stick,level).orElseThrow().value().assemble(stick).is(Items.STICK),"Native stick recipe accepts dyed planks");
            level.setBlockAndUpdate(pos,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(color.getName()+"_dye"),2));click(level,player,pos);
            check(level.getBlockState(pos).is(f.block("water_cauldron"))&&player.getMainHandItem().getCount()==1,"Dye water using real interaction");
            check(level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL)==3,"Adding dye preserves water level");
            for (String kind:DyedWood.KINDS) {
                Item source=item("oak_"+kind);
                int count=source.getDefaultInstance().getMaxStackSize();
                var stack=new ItemStack(source,count);stack.set(DataComponents.CUSTOM_NAME,Component.literal("Keep my name"));
                level.setBlockAndUpdate(pos,f.block("water_cauldron").defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);click(level,player,pos);
                check(player.getMainHandItem().is(f.item(kind))&&player.getMainHandItem().getCount()==count,"Whole wood stack: "+kind);
                check(player.getMainHandItem().getHoverName().getString().equals("Keep my name"),"Name preserved");
                check(level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL)==2,"Exactly one water level per stack");
                check(DyeingCauldrons.target(f.item(kind),DyeColor.RED)==DyedWood.FAMILIES.get(DyeColor.RED).item(kind),"Recolor existing dyed wood");
            }
            for (Item input:List.of(Items.WOOL.white(),Items.CONCRETE.red(),Items.GLASS,Items.GLASS_PANE,Items.TERRACOTTA,Items.CANDLE,Items.STAINED_GLASS.blue(),Items.STAINED_GLASS_PANE.black(),Items.DYED_TERRACOTTA.red(),Items.DYED_CANDLE.blue(),Items.CONCRETE_POWDER.yellow())) {
                level.setBlockAndUpdate(pos,f.block("water_cauldron").defaultBlockState());
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(input,64));click(level,player,pos);
                check(player.getMainHandItem().is(DyeingCauldrons.target(input,color))&&player.getMainHandItem().getCount()==64,"Colored material stack");
                check(level.getBlockState(pos).is(Blocks.CAULDRON),"Final water use empties cauldron");
            }
            check(DyeingCauldrons.target(Items.CONCRETE_POWDER.yellow(),color)==item(color.getName()+"_concrete"),"Powder becomes concrete");
            for(String kind:List.of("sign","wall_sign","hanging_sign","wall_hanging_sign","shelf")) {
                level.setBlock(pos,f.block(kind).defaultBlockState(),2);
                var be=level.getBlockEntity(pos);
                check(be!=null&&be.getType().isValid(level.getBlockState(pos)),"Native block entity accepts "+kind);
                if(be instanceof ShelfBlockEntity shelf){shelf.setItem(0,new ItemStack(Items.DIAMOND,3));check(shelf.getItem(0).getCount()==3,"Shelf inventory");}
                level.removeBlock(pos,false);
            }
            var boat=f.boat.get().create(level,EntitySpawnReason.COMMAND);
            check(boat!=null&&boat.getPickResult().is(f.item("boat")),"Boat returns own colored item");
            for(String kind:List.of("planks","trapdoor","stairs","pressure_plate","fence","fence_gate","button","sign","hanging_sign")) {
                var drops=Block.getDrops(f.block(kind).defaultBlockState(),level,pos,null,player,ItemStack.EMPTY);
                check(drops.size()==1&&drops.getFirst().is(f.item(kind)),"Correct wood loot: "+kind);
            }
        }
        check(DyeingCauldrons.mix(level,DyeColor.RED,DyeColor.YELLOW)==DyeColor.ORANGE,"Red plus yellow");
        check(DyeingCauldrons.mix(level,DyeColor.BLUE,DyeColor.RED)==DyeColor.PURPLE,"Blue plus red");
        check(DyeingCauldrons.mix(level,DyeColor.WHITE,DyeColor.RED)==DyeColor.PINK,"White plus red");
        check(DyeingCauldrons.mix(level,DyeColor.BROWN,DyeColor.BLUE)==DyeColor.BLUE,"Unmixable colors replaced");
        for(Item forbidden:List.of(Items.DIAMOND,Items.STONE_STAIRS,Items.IRON_DOOR,Items.OAK_CHEST_BOAT,Items.CHEST,Items.BUCKET))
            check(DyeingCauldrons.target(forbidden,DyeColor.RED)==null,"Unrelated items excluded");
        level.setBlockAndUpdate(pos,DyedWood.FAMILIES.get(DyeColor.RED).block("water_cauldron").defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BUCKET));click(level,player,pos);
        check(player.getMainHandItem().is(Items.WATER_BUCKET)&&level.getBlockState(pos).is(Blocks.CAULDRON),"Native bucket interaction remains functional");
        level.removeBlock(pos,false);player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        ChocolateFlavored.LOGGER.info("DYEING_VERIFICATION_PASSED: sixteen colors, native crafting, real cauldron interactions, whole stacks, metadata, concrete conversion, mix/replacement, water consumption, native signs/shelves, boat items and loot");
    }
    private static Item item(String path){return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));}
    private static void click(ServerLevel level,FakePlayer player,BlockPos pos){player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Dyeing: "+message);}
}
