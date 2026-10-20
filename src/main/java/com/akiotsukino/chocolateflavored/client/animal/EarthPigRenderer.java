package com.akiotsukino.chocolateflavored.client.animal;
import com.akiotsukino.chocolateflavored.animal.EarthPig;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

public class EarthPigRenderer extends AgeableMobRenderer<EarthPig, PigRenderState, PigModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chocolateflavored", "textures/entity/pale_pig/pale_pig.png");
    public EarthPigRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel(context.bakeLayer(ModModelLayers.LEGACY_PIG)), new PigModel(context.bakeLayer(ModModelLayers.LEGACY_PIG_BABY)), .7F);
        addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.PIG_SADDLE, s -> s.saddle, new PigModel(context.bakeLayer(ModelLayers.PIG_SADDLE)), null));
    }
    @Override public PigRenderState createRenderState() { return new PigRenderState(); }
    @Override public Identifier getTextureLocation(PigRenderState state) { return TEXTURE; }
    @Override public void extractRenderState(EarthPig entity, PigRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks); state.saddle = entity.getItemBySlot(EquipmentSlot.SADDLE).copy(); state.variant = entity.getVariant().value();
    }
}
