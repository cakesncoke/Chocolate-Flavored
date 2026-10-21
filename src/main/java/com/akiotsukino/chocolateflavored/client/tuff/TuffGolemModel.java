package com.akiotsukino.chocolateflavored.client.tuff;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
/** Model/animation data comes exclusively from the original Friends & Foes Blockbench file. */
final class TuffGolemModel extends EntityModel<TuffGolemRenderer.State> {
    private static final BlockbenchAsset ASSET=new BlockbenchAsset("/assets/chocolateflavored/upstream/friendsandfoes/TuffGolem.bbmodel");
    private final ModelPart modelRoot;
    TuffGolemModel(ModelPart root){super(root);modelRoot=root;}
    static LayerDefinition layer(){return ASSET.layer();}
    @Override public void setupAnim(TuffGolemRenderer.State state){
        super.setupAnim(state);
        if(!state.animation.isEmpty()&&state.elapsed<1.5F)ASSET.animate(modelRoot,state.animation,state.elapsed,false);
        else if(state.sleeping)ASSET.animate(modelRoot,state.carrying?"sleepWithItem":"sleep",10,false);
        else ASSET.animate(modelRoot,state.carrying?"walkWithItem":"walk",state.walkAnimationPos/8F,true);
    }
}
