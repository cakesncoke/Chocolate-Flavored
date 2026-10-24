package com.akiotsukino.chocolateflavored.client.dye;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.dye.DyedWood;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=ChocolateFlavored.MODID, value=Dist.CLIENT)
public final class DyedWoodClient {
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.BlockTintSources e) {
        DyedWood.FAMILIES.forEach((color, f) -> f.blocks.forEach((kind, block) ->
                e.register(List.of(BlockTintSources.constant(kind.equals("water_cauldron") ? color.getTextureDiffuseColor() : DyedWood.pastel(color))), block.get())));
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        DyedWood.FAMILIES.forEach((color, f) -> e.registerEntityRenderer(f.boat.get(), context -> new PastelBoatRenderer(context, DyedWood.pastel(color))));
    }
    private DyedWoodClient() {}
}
