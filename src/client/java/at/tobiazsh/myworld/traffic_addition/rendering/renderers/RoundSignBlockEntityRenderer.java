package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 00:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.components.block_entities.RoundSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class RoundSignBlockEntityRenderer extends SignBlockEntityRenderer<RoundSignBlockEntity> {

    public RoundSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(MinecraftClient.getInstance().getBakedModelManager(), "round_sign_block");
    }

}
