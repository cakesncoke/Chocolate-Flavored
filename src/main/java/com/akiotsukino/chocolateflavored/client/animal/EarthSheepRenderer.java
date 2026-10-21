package com.akiotsukino.chocolateflavored.client.animal;
import com.akiotsukino.chocolateflavored.animal.EarthSheep;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.sheep.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;

public class EarthSheepRenderer extends AgeableMobRenderer<EarthSheep, SheepRenderState, SheepModel> {
    private final Identifier texture;
    private final Identifier babyTexture;
    public EarthSheepRenderer(EntityRendererProvider.Context context, String name) {
        super(context, new SheepModel(context.bakeLayer(ModModelLayers.LEGACY_SHEEP)), new BabySheepModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SHEEP_BABY)), .7F);
        texture = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/" + name + "/" + name + ".png");
        babyTexture = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/" + name + "/" + name + "_baby.png");
        Identifier babyWool = name.equals("long_nosed_sheep") ? Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool_baby.png") : Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/" + name + "/" + name + "_fur_baby.png");
        // Long Nose's upstream brown wool is replaced by vanilla tintable white wool, as requested.
        Identifier wool = name.equals("long_nosed_sheep") ? Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool.png") : Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/" + name + "/" + name + "_fur.png");
        addLayer(new RenderLayer<SheepRenderState, SheepModel>(this) {
            private final SheepFurModel adult = new SheepFurModel(context.bakeLayer(ModModelLayers.LEGACY_WOOL));
            // The source baby wool uses the same undeformed boxes/UVs as its body.
            private final SheepModel baby = new BabySheepModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SHEEP_BABY));
            @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light, SheepRenderState state, float yRot, float xRot) {
                if (!state.isSheared) coloredCutoutModelCopyLayerRender(state.isBaby ? baby : adult, state.isBaby ? babyWool : wool, pose, collector, light, state, state.getWoolColor(), state.isBaby ? 1 : 0);
            }
        });
    }
    @Override public SheepRenderState createRenderState() { return new SheepRenderState(); }
    @Override public Identifier getTextureLocation(SheepRenderState state) { return state.isBaby ? babyTexture : texture; }
    @Override public void extractRenderState(EarthSheep sheep, SheepRenderState state, float ticks) {
        super.extractRenderState(sheep,state,ticks); state.headEatAngleScale=sheep.getHeadEatAngleScale(ticks); state.headEatPositionScale=sheep.getHeadEatPositionScale(ticks); state.isSheared=sheep.isSheared();state.woolColor=sheep.getColor();state.isJebSheep=checkMagicName(sheep,"jeb_");
    }
}
