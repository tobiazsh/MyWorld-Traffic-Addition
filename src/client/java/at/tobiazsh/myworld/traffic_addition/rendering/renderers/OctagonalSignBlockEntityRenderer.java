package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 30/08/2024 (DD/MM/YYYY) - 16:08
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.block_entities.OctagonalSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(EnvType.CLIENT)
public class OctagonalSignBlockEntityRenderer extends SignBlockEntityRenderer<OctagonalSignBlockEntity> {

    public OctagonalSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(Minecraft.getInstance().getModelManager());
    }
}