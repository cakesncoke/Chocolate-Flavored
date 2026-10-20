package com.akiotsukino.chocolateflavored.client.mob.model;
import com.akiotsukino.chocolateflavored.client.mob.render.state.BoulderingZombieRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.util.Mth;
/** Native equipment models, animated with the same zombie/climbing poses as the body. */
public final class MobArmorModel<S extends ZombieRenderState> extends HumanoidModel<S> {
 public MobArmorModel(ModelPart root){super(root);}
 @Override public void setupAnim(S state){
  super.setupAnim(state);AnimationUtils.animateZombieArms(leftArm,rightArm,state.isAggressive,state);
  if(state instanceof BoulderingZombieRenderState b&&b.climbable){leftArm.xRot-=Mth.sin(.25F*state.ageInTicks)*.225F;rightArm.xRot+=Mth.sin(.25F*state.ageInTicks)*.225F;leftLeg.xRot=-.4F-Mth.sin(.25F*state.ageInTicks)*.225F;rightLeg.xRot=-.4F+Mth.sin(.25F*state.ageInTicks)*.225F;}
 }
}
