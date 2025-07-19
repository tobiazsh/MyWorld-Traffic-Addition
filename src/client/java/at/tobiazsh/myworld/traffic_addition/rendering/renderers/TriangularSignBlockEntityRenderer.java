package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.block_entities.TriangularSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.blocks.TriangularSignBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class TriangularSignBlockEntityRenderer extends SignBlockEntityRenderer<TriangularSignBlockEntity, TriangularSignBlock> {

    public TriangularSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(MinecraftClient.getInstance().getBakedModelManager());
    }

}