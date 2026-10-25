package com.akiotsukino.chocolateflavored.verification;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.trim.TrimAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Runs the real local player's keyboard/input/NeoForge movement path underwater. */
@EventBusSubscriber(modid=ChocolateFlavored.MODID,value=Dist.CLIENT)
public final class TideJumpClientVerification {
    public static boolean completed;
    private static int ticks, jumpTicks;
    private static boolean setup, jumping;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("chocolateflavored.verify.cooking.client") || completed || !TrimClientVerification.completed || ++ticks<180)return;
        var mc=Minecraft.getInstance();if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
        if(!setup){
            setup=true;mc.setScreen(null);
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var level=p.level();
                for(BlockPos pos:BlockPos.betweenClosed(new BlockPos(444,159,-3),new BlockPos(456,165,10))) {
                    boolean wall=pos.getX()==444 || pos.getX()==456 || pos.getZ()==-3 || pos.getZ()==10 || pos.getY()==159;
                    level.setBlock(pos,wall?Blocks.STONE.defaultBlockState():pos.getY()<165?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
                }
                p.setGameMode(GameType.SURVIVAL);p.getAbilities().flying=false;p.onUpdateAbilities();
                for(var slot:TrimAbilities.ARMOR)p.setItemSlot(slot,net.minecraft.world.item.ItemStack.EMPTY);
                p.setItemSlot(EquipmentSlot.FEET,TrimVerification.armor(level,Items.LEATHER_BOOTS,"tide","iron"));
                p.setAirSupply(p.getMaxAirSupply());p.connection.teleport(451,160,.5,0,0);
            });
        }
        if(!jumping && mc.player.isInWater() && mc.player.onGround() && TrimAbilities.has(mc.player,EquipmentSlot.FEET,"tide")){
            jumping=true;mc.options.keyJump.setDown(true);
        }
        if(jumping && ++jumpTicks==20){
            mc.options.keyJump.setDown(false);
            if(mc.player.onGround() || mc.player.getY()<160.8 || !mc.player.isInWater())throw new IllegalStateException("Tide boots prevented real client underwater jump/swim transition");
            completed=true;
            ChocolateFlavored.LOGGER.info("TIDE_JUMP_CLIENT_VERIFICATION_PASSED: real local-player jump input leaves the underwater floor and continues swimming upward");
        }
        if(ticks>480 && !completed)throw new IllegalStateException("Tide client fixture never reached underwater ground contact");
    }
}
