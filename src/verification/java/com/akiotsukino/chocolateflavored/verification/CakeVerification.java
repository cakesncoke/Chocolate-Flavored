package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.cake.*;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

final class CakeVerification {
    static void run(ServerLevel level, FakePlayer player) {
        var pos = new BlockPos(224, 160, 0);
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(224.5,161,.5);
        for (var flavor : CakeContent.FLAVORS) {
            var cake = flavor.block().get();
            check(flavor.item().get().getDefaultInstance().getMaxStackSize() == Items.CAKE.getDefaultInstance().getMaxStackSize(), "Vanilla stack size");
            check(!flavor.item().get().getDefaultInstance().has(DataComponents.CONSUMABLE), "Cake is placed to eat");
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.removeBlock(pos, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(flavor.item().get()));
            check(flavor.item().get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false))).consumesAction(), "Place " + flavor.name());
            check(level.getBlockState(pos).is(cake) && player.getMainHandItem().isEmpty(), "Placement consumes cake item");
            player.getFoodData().setFoodLevel(20);
            click(level, player, pos, false);
            check(level.getBlockState(pos).getValue(CakeBlock.BITES) == 0, "Full player cannot eat");
            player.getFoodData().setFoodLevel(0);player.getFoodData().setSaturation(0);
            for (int bite=0; bite<7; bite++) {
                var state = level.getBlockState(pos);
                var vanilla = Blocks.CAKE.defaultBlockState().setValue(CakeBlock.BITES,bite);
                check(state.getValue(CakeBlock.BITES)==bite, "Seven native serving states");
                check(state.getShape(level,pos).bounds().equals(vanilla.getShape(level,pos).bounds()), "Native sliced shape");
                check(state.getAnalogOutputSignal(level,pos,Direction.NORTH)==vanilla.getAnalogOutputSignal(level,pos,Direction.NORTH), "Native comparator output");
                check(Block.getDrops(state,level,pos,null,player,ItemStack.EMPTY).isEmpty(), "Broken cake gives no drops");
                click(level,player,pos,false);
                check(player.getFoodData().getFoodLevel()==2*(bite+1), "Two hunger per serving");
                check(Math.abs(player.getFoodData().getSaturationLevel()-.4F*(bite+1))<.0001F, "Vanilla saturation per serving");
            }
            check(level.getBlockState(pos).isAir(), "Seventh serving removes cake");
            level.setBlockAndUpdate(pos,cake.defaultBlockState());level.removeBlock(pos.below(),false);
            check(level.getBlockState(pos).isAir(), "Cake requires solid support");
            level.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
            for (String name : CakeContent.CANDLES) {
                clear(level,pos);
                var candle = (CandleBlock)BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(name));
                var nativeForm = CandleCakeBlock.byCandle(candle);
                var form = flavor.candles().get(name).get();
                level.setBlockAndUpdate(pos,cake.defaultBlockState());
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(candle,2));
                click(level,player,pos,true);
                check(level.getBlockState(pos).is(form) && player.getMainHandItem().getCount()==1, "Add matching candle and consume one");
                check(CandleCakeBlock.byCandle(candle).equals(nativeForm), "Vanilla candle lookup unchanged");
                check(level.getBlockState(pos).getShape(level,pos).bounds().equals(nativeForm.getShape(level,pos).bounds()), "Vanilla candle-cake shape");
                check(level.getBlockState(pos).getAnalogOutputSignal(level,pos,Direction.NORTH)==14, "Full candle-cake signal");
                var drops=Block.getDrops(level.getBlockState(pos),level,pos,null,player,ItemStack.EMPTY);
                check(drops.size()==1 && drops.getFirst().is(candle.asItem()) && drops.getFirst().getCount()==1, "Only matching candle drops");
                for (var lighter : List.of(Items.FLINT_AND_STEEL,Items.FIRE_CHARGE)) {
                    var stack=new ItemStack(lighter);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    click(level,player,pos,true);
                    check(level.getBlockState(pos).getValue(AbstractCandleBlock.LIT), "Native ignition with " + lighter);
                    if(lighter==Items.FIRE_CHARGE) check(stack.isEmpty(),"Fire charge consumed");
                    else check(stack.getDamageValue()==1,"Flint and steel wear");
                    player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);click(level,player,pos,true);
                    check(!level.getBlockState(pos).getValue(AbstractCandleBlock.LIT),"Empty hand extinguishes candle");
                }
                player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
                click(level,player,pos,false);
                check(level.getBlockState(pos).is(cake) && level.getBlockState(pos).getValue(CakeBlock.BITES)==1,"Eating preserves cake flavor");
                check(level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2)).stream().filter(e->e.getItem().is(candle.asItem())).mapToInt(e->e.getItem().getCount()).sum()==1,"First slice drops one candle");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(candle));
                click(level,player,pos,true);
                check(level.getBlockState(pos).is(cake),"Bitten cake refuses candle");
                clear(level,pos);
            }
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        level.removeBlock(pos,false);clear(level,pos);
        ChocolateFlavored.LOGGER.info("CAKE_VERIFICATION_PASSED: all four flavors, native placement/stacking, seven servings, hunger/saturation, shapes, signals, support and loot; all 68 vanilla candle combinations, ignition/extinguishing, candle drops, preserved flavors and vanilla lookup");
    }
    private static void click(ServerLevel level,FakePlayer player,BlockPos pos,boolean candle) {
        var hit=new BlockHitResult(new Vec3(pos.getX()+.5,pos.getY()+(candle?.8:.25),pos.getZ()+.5),Direction.UP,pos,false);
        player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
    }
    private static void clear(ServerLevel level,BlockPos pos) { level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).forEach(ItemEntity::discard); }
    private static void check(boolean value,String message) {if(!value)throw new IllegalStateException("Cakes: "+message);}
    private CakeVerification() {}
}
