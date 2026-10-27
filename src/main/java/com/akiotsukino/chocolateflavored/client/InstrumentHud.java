package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.content.ModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.clock.WorldClocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = ChocolateFlavored.MODID, value = Dist.CLIENT)
public final class InstrumentHud {
    public static boolean has(Item item) {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (player.getInventory().getItem(i).is(item)) return true;
        return false;
    }
    public static boolean precision() { return has(ModContent.PRECISION_METER.get()); }
    @SubscribeEvent public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "instruments"), (g, delta) -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null || mc.options.hideGui) return;
            boolean meter = precision();
            boolean overworld = mc.level.dimension().equals(Level.OVERWORLD);
            boolean coordinates = meter || overworld && has(Items.COMPASS);
            boolean clock = meter || overworld && has(Items.CLOCK);
            var lines = new java.util.ArrayList<String>();
            var pos = mc.player.blockPosition();
            if (coordinates) lines.add(meter ? "X: " + pos.getX() + "  Y: " + pos.getY() + "  Z: " + pos.getZ()
                    : "X: " + pos.getX() + "  Z: " + pos.getZ());
            if (clock) {
                long ticks = mc.level.clockManager().getTotalTicks(mc.level.registryAccess().getOrThrow(WorldClocks.OVERWORLD));
                long seconds = Math.floorMod(ticks + 6000, 24000) * 86400 / 24000;
                lines.add(String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60));
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
