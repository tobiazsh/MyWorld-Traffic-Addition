package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 30/08/2024 (DD/MM/YYYY) - 16:08
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.block_entities.OctagonalSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class OctagonalSignBlockEntityRenderer extends SignBlockEntityRenderer<OctagonalSignBlockEntity> {

    public OctagonalSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(MinecraftClient.getInstance().getBakedModelManager(), "octagonal_sign_block");
    }
}