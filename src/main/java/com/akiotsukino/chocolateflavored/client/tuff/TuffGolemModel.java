package com.akiotsukino.chocolateflavored.client.tuff;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
/** Model/animation data comes exclusively from the original Friends & Foes Blockbench file. */
final class TuffGolemModel extends EntityModel<TuffGolemRenderer.State> {
    private static final BlockbenchAsset ASSET=new BlockbenchAsset("/assets/chocolateflavored/upstream/friendsandfoes/TuffGolem.bbmodel");
    private final java.util.Map<String,net.minecraft.client.animation.KeyframeAnimation> animations;
    TuffGolemModel(ModelPart root){super(root);animations=ASSET.bakeAnimations(root);}
    static LayerDefinition layer(){return ASSET.layer();}
    @Override public void setupAnim(TuffGolemRenderer.State state){
        super.setupAnim(state);
        // Walking is additive to the persistent pose, exactly as in the source animations.
        float walkMultiplier=4*(state.carrying?.175F/.225F:1);
        animations.get(state.carrying?"walkWithItem":"walk").applyWalk(state.walkAnimationPos,state.walkAnimationSpeed,walkMultiplier,walkMultiplier);
        String pose=state.animation;
        if(pose.isEmpty())pose=state.sleeping?(state.carrying?"sleepWithItem":"sleep"):(state.carrying?"showItem":"hideItem");
        animations.get(pose).apply(state.animation.isEmpty()?10000L:(long)(Math.max(0,state.elapsed)*1000),1);
    }
}
