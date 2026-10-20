package com.akiotsukino.chocolateflavored.client.mob.render.layer;

import com.akiotsukino.chocolateflavored.client.mob.ModModelLayers;
import com.akiotsukino.chocolateflavored.client.mob.model.MobArmorModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;

/** The imported babies use scaled adult meshes, so their armor needs the same mesh and UV transform. */
public final class MobArmorLayer<S extends ZombieRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    private final ArmorModelSet<MobArmorModel<S>> adults;
    private final ArmorModelSet<MobArmorModel<S>> babies;
    private final EquipmentLayerRenderer equipment;

    public MobArmorLayer(RenderLayerParent<S, M> parent, EntityRendererProvider.Context context) {
        super(parent);
        adults = ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), MobArmorModel::new);
        babies = ArmorModelSet.bake(ModModelLayers.BABY_ARMOR, context.getModelSet(), MobArmorModel::new);
        equipment = context.getEquipmentRenderer();
    }

    @Override public void submit(PoseStack poses, SubmitNodeCollector collector, int light, S state, float yaw, float pitch) {
        for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            var stack = switch (slot) {
                case HEAD -> state.headEquipment;
                case CHEST -> state.chestEquipment;
                case LEGS -> state.legsEquipment;
                default -> state.feetEquipment;
            };
            if (!HumanoidArmorLayer.shouldRender(stack, slot)) continue;
            var asset = stack.get(DataComponents.EQUIPPABLE).assetId().orElseThrow();
            var layer = slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
            equipment.renderLayers(layer, asset, (state.isBaby ? babies : adults).get(slot), state, stack, poses, collector, light, state.outlineColor);
        }
    }
}
