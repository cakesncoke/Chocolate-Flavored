package com.akiotsukino.chocolateflavored.mob;
import com.akiotsukino.chocolateflavored.mob.block.CarvedMelonBlock;
import net.minecraft.core.Direction;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
public final class MelonCarving {
 public static void interact(PlayerInteractEvent.RightClickBlock event){
  if(!(event.getItemStack().getItem() instanceof ShearsItem)||!event.getLevel().getBlockState(event.getPos()).is(Blocks.MELON)||event.getFace()==null||event.getFace().getAxis()==Direction.Axis.Y)return;
  if(!event.getLevel().isClientSide()){
   event.getLevel().setBlock(event.getPos(),MobContent.CARVED_MELON.get().defaultBlockState().setValue(CarvedMelonBlock.FACING,event.getFace()),3);
   event.getLevel().playSound(null,event.getPos(),SoundEvents.PUMPKIN_CARVE,SoundSource.BLOCKS,1,1);
   event.getItemStack().hurtAndBreak(1,event.getEntity(),LivingEntity.getSlotForHand(event.getHand()));
  }
  event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
 }
 private MelonCarving(){}
}
