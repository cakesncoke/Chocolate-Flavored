package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.item.SpearItem;
import com.akiotsukino.chocolateflavored.network.SpearNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.network.PacketDistributor;

/** Old rendering/model API adapter for modern inventory/in-hand spear sprites and charge phases. */
public final class SpearClient {
    private SpearClient() {}
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered event) {
        var player = Minecraft.getInstance().player;
        if (!event.isAttack() || player == null || player.isSpectator()
                || !(player.getMainHandItem().getItem() instanceof SpearItem)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (!player.isUsingItem()) PacketDistributor.sendToServer(SpearNetwork.Jab.INSTANCE);
    }
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(ChocolateFlavored.MOD_ID, name); }
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        for (String tier : new String[]{"iron", "diamond", "netherite"}) {
            event.register(ModelResourceLocation.standalone(id("item/" + tier + "_spear_in_hand")));
        }
    }
    public static void bakeModels(ModelEvent.ModifyBakingResult event) {
        for (String tier : new String[]{"iron", "diamond", "netherite"}) {
            var inventoryId = new ModelResourceLocation(id(tier + "_spear"), "inventory");
            var inventory = event.getModels().get(inventoryId);
            var held = event.getModels().get(ModelResourceLocation.standalone(id("item/" + tier + "_spear_in_hand")));
            if (inventory != null && held != null) event.getModels().put(inventoryId, new Presentation(inventory, held, 0, 0));
        }
    }
    private static final class Presentation extends BakedModelWrapper<BakedModel> {
        private final BakedModel held;
        private final float phase, recoil;
        private Presentation(BakedModel inventory, BakedModel held, float phase, float recoil) {
            super(inventory); this.held = held; this.phase = phase; this.recoil = recoil;
        }
        @Override public ItemOverrides getOverrides() {
            return new ItemOverrides() {
                @Override public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
                    float phase = 0, recoil = 0;
                    if (entity != null && stack.getItem() instanceof SpearItem spear) {
                        if (entity.isUsingItem() && entity.getUseItem() == stack) {
                            int age = entity.getTicksUsingItem() - spear.material().delay;
                            phase = age > spear.material().knockbackDuration ? 2 : age > spear.material().dismountDuration ? 1 : 0;
                        }
                        if (entity instanceof net.minecraft.world.entity.player.Player player) {
                            long ticks = player.level().getGameTime() - player.getPersistentData().getLong("chocolateflavored:spear_hit_time");
                            if (player.getPersistentData().contains("chocolateflavored:spear_hit_time") && ticks >= 0 && ticks < 4) recoil = 1 - ticks / 4F;
                        }
                    }
                    return new Presentation(originalModel, held, phase, recoil);
                }
            };
        }
        @Override public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean left) {
            if (context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED) {
                return originalModel.applyTransform(context, pose, left);
            }
            var rendered = held.applyTransform(context, pose, left);
            if (phase == 1) {
                pose.mulPose(Axis.XP.rotationDegrees(-45));
                pose.translate(Math.sin(Minecraft.getInstance().level.getGameTime() * 3) * .012, 0, 0);
            } else if (phase == 2) pose.mulPose(Axis.XP.rotationDegrees(30));
            pose.translate(0, 0, recoil * -.18);
            return rendered;
        }
    }
}
