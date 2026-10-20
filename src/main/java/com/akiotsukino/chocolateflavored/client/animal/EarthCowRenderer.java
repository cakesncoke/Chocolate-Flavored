package com.akiotsukino.chocolateflavored.client.animal;
import com.akiotsukino.chocolateflavored.animal.EarthCow;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class EarthCowRenderer extends AgeableMobRenderer<EarthCow, LivingEntityRenderState, CowModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/dairy_cow/dairy_cow.png");
    public EarthCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel(context.bakeLayer(ModModelLayers.LEGACY_COW)), new CowModel(context.bakeLayer(ModModelLayers.LEGACY_COW_BABY)), .7F);
    }
    @Override public LivingEntityRenderState createRenderState() { return new LivingEntityRenderState(); }
    @Override public Identifier getTextureLocation(LivingEntityRenderState state) { return TEXTURE; }
}
