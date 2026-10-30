package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class TrimOutlineClientVerification {
    private static int ticks;
    private static boolean setup, requested, durationChecked;
    public static boolean completed;
    private static long received = -1;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || !TideJumpClientVerification.completed || ++ticks<180)return;
        var mc=Minecraft.getInstance();if(mc.level==null || mc.player==null || mc.getSingleplayerServer()==null)return;
        if(!setup){
            setup=true;mc.gui.setScreen(null);
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(p==null)throw new IllegalStateException("Missing outline fixture player");
                var level=p.level();
                for(BlockPos pos:BlockPos.betweenClosed(new BlockPos(444,159,-3),new BlockPos(456,166,12)))level.setBlock(pos,pos.getY()==159?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
                // Two visible ores and two behind a solid stone wall: all must receive borders only.
                level.setBlockAndUpdate(new BlockPos(448,161,5),Blocks.DIAMOND_ORE.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(450,161,5),Blocks.GOLD_ORE.defaultBlockState());
                for(BlockPos pos:BlockPos.betweenClosed(new BlockPos(451,160,4),new BlockPos(454,164,4)))level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(452,161,5),Blocks.EMERALD_ORE.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(453,162,5),Blocks.IRON_ORE.defaultBlockState());
                p.setItemSlot(EquipmentSlot.HEAD,TrimVerification.armor(level,Items.IRON_HELMET,"eye","gold"));
                p.setItemSlot(EquipmentSlot.CHEST,TrimVerification.armor(level,Items.DIAMOND_CHESTPLATE,"eye","gold"));
                p.setItemSlot(EquipmentSlot.LEGS,TrimVerification.armor(level,Items.NETHERITE_LEGGINGS,"eye","gold"));
                p.setItemSlot(EquipmentSlot.FEET,TrimVerification.armor(level,Items.LEATHER_BOOTS,"eye","gold"));
                p.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(Items.DIAMOND_PICKAXE));
                p.connection.teleport(451,160,.5,0,0);
            });
        }
        // Activation occurs only after equipment and teleport packets have reached the client,
        // matching a real player who equips the set before crouching to reveal ores.
        if(!requested && mc.player.getX()>440 && TrimAbilities.full(mc.player,"eye")) {
            requested=true;
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());TrimHighlights.scan(p,false);
            });
        }
        try {
            var field=com.akiotsukino.chocolateflavored.client.trim.TrimOutlines.class.getDeclaredField("positions");field.setAccessible(true);
            int count=((java.util.List<?>)field.get(null)).size();
            if(received<0 && count==4)received=mc.level.getGameTime();
            if(received>=0 && !completed){
                long elapsed=mc.level.getGameTime()-received;
                if(!durationChecked && elapsed>=400){
                    if(count!=4)throw new IllegalStateException("Eye glow cleared early: elapsed="+elapsed+", alive="+mc.player.isAlive()+", fullSet="+TrimAbilities.full(mc.player,"eye")+", count="+count);
                    durationChecked=true;
                }
                if(elapsed>=620){
                    if(count!=0)throw new IllegalStateException("Eye glow did not expire after thirty seconds");
                    completed=true;
                    mc.getSingleplayerServer().execute(()->{
                        var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());TrimHighlights.scan(p,false);
                    });
                    ChocolateFlavored.LOGGER.info("TRIM_OUTLINE_CLIENT_VERIFICATION_PASSED: real server payload, emissive borders, reveal persists past ten seconds and expires at thirty seconds");
                }
            }
            if(received<0 && ticks>500)throw new IllegalStateException("Expected four authorized ore borders");
        } catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
}
