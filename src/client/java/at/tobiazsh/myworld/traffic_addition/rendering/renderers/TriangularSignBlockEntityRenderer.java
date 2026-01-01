package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.block_entities.TriangularSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(EnvType.CLIENT)
public class TriangularSignBlockEntityRenderer extends SignBlockEntityRenderer<TriangularSignBlockEntity> {

    public TriangularSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(Minecraft.getInstance().getModelManager());
    }

}