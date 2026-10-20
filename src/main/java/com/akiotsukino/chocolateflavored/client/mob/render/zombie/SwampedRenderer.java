// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from baguchi/EarthMobs 26.1.x, d5acab9846bd3400ac5f093bfec7439351a1bd61.
package com.akiotsukino.chocolateflavored.client.mob.render.zombie;

import com.akiotsukino.chocolateflavored.ChocolateFlavored;
import com.akiotsukino.chocolateflavored.client.mob.ModModelLayers;
import com.akiotsukino.chocolateflavored.client.mob.model.AbstractLobberZombieModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import com.akiotsukino.chocolateflavored.mob.entity.Swamped;
import net.minecraft.client.model.geom.ModelLayers;
import com.akiotsukino.chocolateflavored.client.mob.model.MobArmorModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import com.akiotsukino.chocolateflavored.client.mob.render.layer.MobArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;


public class SwampedRenderer<T extends Swamped> extends AgeableMobRenderer<T, ZombieRenderState, AbstractLobberZombieModel<ZombieRenderState>> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(ChocolateFlavored.MODID, "textures/entity/lobber_zombie/lobber_zombie.png");

	public SwampedRenderer(EntityRendererProvider.Context p_173964_) {
		super(p_173964_, new AbstractLobberZombieModel<>(p_173964_.bakeLayer(ModModelLayers.SWAMPED)), new AbstractLobberZombieModel<>(p_173964_.bakeLayer(ModModelLayers.SWAMPED_BABY)), 0.5F);
        this.addLayer(new MobArmorLayer<>(this, p_173964_));
        this.addLayer(new ItemInHandLayer<>(this));
    }

	@Override
	public ZombieRenderState createRenderState() {
		return new ZombieRenderState();
	}

	protected HumanoidModel.ArmPose getArmPose(T p_388016_, HumanoidArm p_386643_) {
		return HumanoidModel.ArmPose.EMPTY;
	}

	@Override
	public void extractRenderState(T p_365075_, ZombieRenderState p_361774_, float p_363123_) {
		super.extractRenderState(p_365075_, p_361774_, p_363123_);
		extractHumanoidRenderState(p_365075_, p_361774_, p_363123_, this.itemModelResolver);
		p_361774_.leftArmPose = this.getArmPose(p_365075_, HumanoidArm.LEFT);
		p_361774_.rightArmPose = this.getArmPose(p_365075_, HumanoidArm.RIGHT);
        p_361774_.isAggressive = p_365075_.isAggressive();
	}

	public static void extractHumanoidRenderState(LivingEntity p_365104_, HumanoidRenderState p_362998_, float p_363706_, ItemModelResolver p_388651_) {
        ArmedEntityRenderState.extractArmedEntityRenderState(p_365104_, p_362998_, p_388651_, p_363706_);
		p_362998_.isCrouching = p_365104_.isCrouching();
		p_362998_.isFallFlying = p_365104_.isFallFlying();
		p_362998_.isVisuallySwimming = p_365104_.isVisuallySwimming();
		p_362998_.isPassenger = p_365104_.isPassenger() && (p_365104_.getVehicle() != null && p_365104_.getVehicle().shouldRiderSit());
		p_362998_.speedValue = 1.0F;
		if (p_362998_.isFallFlying) {
			p_362998_.speedValue = (float) p_365104_.getDeltaMovement().lengthSqr();
			p_362998_.speedValue /= 0.2F;
			p_362998_.speedValue = p_362998_.speedValue * p_362998_.speedValue * p_362998_.speedValue;
		}

		if (p_362998_.speedValue < 1.0F) {
			p_362998_.speedValue = 1.0F;
		}

		p_362998_.attackTime = p_365104_.getAttackAnim(p_363706_);
		p_362998_.swimAmount = p_365104_.getSwimAmount(p_363706_);
		p_362998_.attackArm = getAttackArm(p_365104_);
		p_362998_.useItemHand = p_365104_.getUsedItemHand();
		p_362998_.maxCrossbowChargeDuration = (float) CrossbowItem.getChargeDuration(p_365104_.getUseItem(), p_365104_);
		p_362998_.ticksUsingItem = p_365104_.getTicksUsingItem();
		p_362998_.isUsingItem = p_365104_.isUsingItem();
		p_362998_.elytraRotX = p_365104_.elytraAnimationState.getRotX(p_363706_);
		p_362998_.elytraRotY = p_365104_.elytraAnimationState.getRotY(p_363706_);
		p_362998_.elytraRotZ = p_365104_.elytraAnimationState.getRotZ(p_363706_);
		p_362998_.headEquipment = getEquipmentIfRenderable(p_365104_, EquipmentSlot.HEAD);
		p_362998_.chestEquipment = getEquipmentIfRenderable(p_365104_, EquipmentSlot.CHEST);
		p_362998_.legsEquipment = getEquipmentIfRenderable(p_365104_, EquipmentSlot.LEGS);
		p_362998_.feetEquipment = getEquipmentIfRenderable(p_365104_, EquipmentSlot.FEET);
	}

	private static ItemStack getEquipmentIfRenderable(LivingEntity p_386637_, EquipmentSlot p_386956_) {
		ItemStack itemstack = p_386637_.getItemBySlot(p_386956_);
		return HumanoidArmorLayer.shouldRender(itemstack, p_386956_) ? itemstack.copy() : ItemStack.EMPTY;
	}

	private static HumanoidArm getAttackArm(LivingEntity p_362737_) {
		HumanoidArm humanoidarm = p_362737_.getMainArm();
		return p_362737_.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
	}

    public Identifier getTextureLocation(ZombieRenderState p_114115_) {
		return LOCATION;
	}
}
