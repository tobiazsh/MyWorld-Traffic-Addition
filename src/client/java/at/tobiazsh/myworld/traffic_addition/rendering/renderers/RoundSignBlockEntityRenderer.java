package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 00:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.block_entities.RoundSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(EnvType.CLIENT)
public class RoundSignBlockEntityRenderer extends SignBlockEntityRenderer<RoundSignBlockEntity> {

    public RoundSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(Minecraft.getInstance().getModelManager());
    }

}
