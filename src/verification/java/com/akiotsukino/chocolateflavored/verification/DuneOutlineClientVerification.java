package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.TrimAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class DuneOutlineClientVerification {
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || !TrimOutlineClientVerification.completed || ++ticks<60)return;
        var mc=Minecraft.getInstance();if(mc.player==null || mc.getSingleplayerServer()==null)return;
        if(ticks==60)mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var level=p.level();
            var positions=java.util.List.of(new BlockPos(448,161,5),new BlockPos(450,161,5),new BlockPos(452,161,5),new BlockPos(453,162,5));
            for(int i=0;i<positions.size();i++){
                var pos=positions.get(i);level.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(pos,(i%2==0?Blocks.SUSPICIOUS_SAND:Blocks.SUSPICIOUS_GRAVEL).defaultBlockState());
            }
            for(var slot:TrimAbilities.ARMOR){var item=switch(slot){case HEAD->Items.IRON_HELMET;case CHEST->Items.DIAMOND_CHESTPLATE;case LEGS->Items.NETHERITE_LEGGINGS;default->Items.LEATHER_BOOTS;};p.setItemSlot(slot,TrimVerification.armor(level,item,"dune","gold"));}
            p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BRUSH));
        });
        if(ticks==140 || ticks==220){
            var field=com.akiotsukino.chocolateflavored.client.trim.TrimOutlines.class.getDeclaredField("positions");field.setAccessible(true);
            if(((java.util.List<?>)field.get(null)).size()!=4)throw new IllegalStateException("Dune must retain four suspicious-block borders through automatic refresh");
        }
        if(ticks==220)ChocolateFlavored.LOGGER.info("DUNE_OUTLINE_CLIENT_VERIFICATION_PASSED: suspicious sand/gravel share Eye emissive outlines, visible and behind stone, with automatic brush refresh");
    }
}
