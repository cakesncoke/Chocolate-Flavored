package com.akiotsukino.chocolateflavored.trim;
import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
@EventBusSubscriber(modid=ChocolateFlavored.MODID)
public final class TrimTerrain {
    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent e){if(e.getPlacedAgainst().is(TrimBasalt.BLOCK.get()) || e.getLevel().getBlockState(e.getPos().below()).is(TrimBasalt.BLOCK.get()))e.setCanceled(true);}
    @SubscribeEvent public static void mount(EntityTickEvent.Pre e){
        if(e.getEntity() instanceof LivingEntity mount && mount.is(TrimAbilities.CAMELS) && !mount.level().isClientSide()){
            var p=TrimAbilities.rider(mount);
            TrimAbilities.modifier(mount,Attributes.MOVEMENT_SPEED,"camel",p!=null && TrimAbilities.has(p,EquipmentSlot.FEET,"dune")?.25:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }
}
