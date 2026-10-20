package com.akiotsukino.chocolateflavored.client.animal;
import com.akiotsukino.chocolateflavored.animal.EarthChicken;
import net.minecraft.client.model.animal.chicken.AdultChickenModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
public class EarthChickenRenderer extends AgeableMobRenderer<EarthChicken, ChickenRenderState, AdultChickenModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/bronzed_chicken/bronzed_chicken.png");
    public EarthChickenRenderer(EntityRendererProvider.Context context) { super(context, new AdultChickenModel(context.bakeLayer(ModModelLayers.BRONZED_CHICKEN)), new AdultChickenModel(context.bakeLayer(ModModelLayers.BRONZED_CHICKEN_BABY)), .3F); }
    @Override public ChickenRenderState createRenderState() { return new ChickenRenderState(); }
    @Override public Identifier getTextureLocation(ChickenRenderState state) { return TEXTURE; }
    @Override public void extractRenderState(EarthChicken entity, ChickenRenderState state, float ticks) {
        super.extractRenderState(entity,state,ticks); state.flap = Mth.lerp(ticks, entity.oFlap, entity.flap); state.flapSpeed = Mth.lerp(ticks,entity.oFlapSpeed,entity.flapSpeed);
    }
}
