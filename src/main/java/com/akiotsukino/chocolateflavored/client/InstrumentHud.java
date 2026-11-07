package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import com.akiotsukino.chocolateflavored.horn.CopperHornItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class InstrumentHud {
    public static boolean has(Item item) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return false;
        if (minecraft.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof ItemFrame frame
                && frame.getItem().is(item)) return true;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (player.getInventory().getItem(i).is(item)) return true;
        return false;
    }
    public static boolean precision() { return has(ModContent.PRECISION_METER.get()); }
    @SubscribeEvent public static void hideHornName(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) return;
        var player = Minecraft.getInstance().player;
        if (player != null && player.isUsingItem() && player.getUseItem().getItem() instanceof CopperHornItem) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "instruments"), (g, delta) -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null || mc.gui.hud.isHidden()) return;
            boolean meter = precision();
            boolean overworld = mc.level.dimension().equals(Level.OVERWORLD);
            boolean coordinates = meter || overworld && has(Items.COMPASS);
            boolean clock = meter || overworld && has(Items.CLOCK);
            var lines = new java.util.ArrayList<String>();
            var pos = mc.player.blockPosition();
            if (coordinates) lines.add(meter ? "XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                    : "XZ: " + pos.getX() + " " + pos.getZ());
            if (clock) {
                long ticks = mc.level.clockManager().getInstance(mc.level.registryAccess().getOrThrow(WorldClocks.OVERWORLD)).totalTicks();
                long minutes = (Math.floorMod(ticks, 24000) + 6000) % 24000 * 1440 / 24000;
                lines.add(String.format(java.util.Locale.ROOT, "Time: %02d:%02d", minutes / 60, minutes % 60));
            }
            int y = 6;
            for (String line : lines) {
                int x = mc.getWindow().getGuiScaledWidth() - mc.font.width(line) - 6;
                g.fill(x - 3, y - 2, mc.getWindow().getGuiScaledWidth() - 3, y + 10, 0x88000000);
                g.text(mc.font, line, x, y, 0xFFFFFFFF, true);
                y += 13;
            }
        });
    }
    private InstrumentHud() {}
}
