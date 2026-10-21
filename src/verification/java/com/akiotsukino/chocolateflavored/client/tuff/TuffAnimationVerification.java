package com.akiotsukino.chocolateflavored.client.tuff;

/** Exercises production pose blending, including times beyond the old 1.5-second cutoff. */
public final class TuffAnimationVerification {
    public static void run(){
        var root=TuffGolemModel.layer().bakeRoot();var model=new TuffGolemModel(root);
        var arm=root.getChild("body").getChild("leftArm");
        var leg=root.getChild("leftLeg");
        var state=new TuffGolemRenderer.State();state.carrying=true;state.animation="showItem";state.elapsed=3;
        model.setupAnim(state);float held=arm.xRot;
        check(Math.abs(held+(float)Math.PI/2)<.001,"Completed showItem retains raised arms");
        state.elapsed=300;state.walkAnimationPos=7;model.setupAnim(state);
        check(Math.abs(arm.xRot-held)<.001&&Math.abs(leg.xRot)<.001,"Stationary holding pose has no gait at arbitrary walk phase");
        state.walkAnimationSpeed=1;state.walkAnimationPos=1.25F;model.setupAnim(state);
        check(Math.abs(leg.xRot)>.01&&arm.xRot<-1,"Walking adds to holding pose");
        state.walkAnimationSpeed=0;state.animation="";model.setupAnim(state);
        check(Math.abs(arm.xRot-held)<.001,"Reloaded carrying entity restores holding pose");
        state.carrying=false;state.animation="hideItem";state.elapsed=4;model.setupAnim(state);
        check(Math.abs(arm.xRot)<.001,"Completed hideItem restores arms");
        state.sleeping=true;state.animation="";model.setupAnim(state);
        float resting=root.getChild("body").y;state.animation="sleep";state.elapsed=20;model.setupAnim(state);
        check(Math.abs(root.getChild("body").y-resting)<.001,"Reloaded sleeping pose matches completed transition");
    }
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Tuff animation: "+message);}
    private TuffAnimationVerification(){}
}
