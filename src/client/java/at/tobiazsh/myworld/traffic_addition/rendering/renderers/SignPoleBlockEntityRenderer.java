package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignBlockRenderState;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignPoleBlockRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SignPoleBlockEntityRenderer implements BlockEntityRenderer<SignPoleBlockEntity, SignPoleBlockRenderState> {

    private BlockStateModel signPoleModel = null;

    public SignPoleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(SignPoleBlockRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if(!state.shouldRender) return;

        if (signPoleModel == null)
            signPoleModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(state.blockState);

        matrices.push();

        matrices.translate(.5, 0, .5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.rotation));
        matrices.translate(-0.5f, 0, -0.5f);

        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getSolid(),
                signPoleModel,
                1.0f, 1.0f, 1.0f,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }

    @Override
    public SignPoleBlockRenderState createRenderState() {
        return new SignPoleBlockRenderState();
    }

    @Override
    public void updateRenderState(SignPoleBlockEntity blockEntity, SignPoleBlockRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.rotation = blockEntity.getRotationValue();
        state.shouldRender = blockEntity.isShouldRender();
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }
}
