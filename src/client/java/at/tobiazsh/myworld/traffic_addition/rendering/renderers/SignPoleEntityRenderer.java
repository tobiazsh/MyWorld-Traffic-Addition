package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class SignPoleEntityRenderer implements BlockEntityRenderer<SignPoleBlockEntity> {

    private BlockStateModel signPoleModel = null;

    public SignPoleEntityRenderer (BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(SignPoleBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        if(!entity.isShouldRender()) return;

        if (signPoleModel == null) {
            signPoleModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(
                    entity.getCachedState().getBlock().getDefaultState()
            );
        }

        int rotation_value = entity.getRotationValue();

        matrices.push();

        matrices.translate(.5, 0, .5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation_value));
        matrices.translate(-0.5f, 0, -0.5f);

        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(
                entity.getWorld(),
                signPoleModel,
                entity.getCachedState(),
                entity.getPos(),
                matrices,
                vertexConsumers,
                true,
                entity.getPos().asLong(),
                overlay
        );

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(SignPoleBlockEntity entity) {
        return true;
    }
}
