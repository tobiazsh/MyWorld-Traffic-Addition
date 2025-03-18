package at.tobiazsh.myworld.traffic_addition.Rendering.Renderers;

import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.TriangularSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class TriangularSignBlockEntityRenderer extends SignBlockEntityRenderer<TriangularSignBlockEntity> {

    public TriangularSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(MinecraftClient.getInstance().getBakedModelManager(), "triangular_sign_block");
    }

}