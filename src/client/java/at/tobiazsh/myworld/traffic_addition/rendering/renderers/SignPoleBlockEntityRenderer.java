package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignPoleBlockRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SignPoleBlockEntityRenderer implements BlockEntityRenderer<SignPoleBlockEntity, SignPoleBlockRenderState> {

    private BlockStateModel signPoleModel = null;

    public SignPoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void submit(SignPoleBlockRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if(!state.shouldRender) return;

        if (signPoleModel == null)
            signPoleModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state.blockState);

        matrices.pushPose();

        matrices.translate(.5, 0, .5);
        matrices.mulPose(Axis.YP.rotationDegrees(state.rotation));
        matrices.translate(-0.5f, 0, -0.5f);

        queue.submitBlockModel(
                matrices,
                RenderTypes.solidMovingBlock(),
                signPoleModel,
                1.0f, 1.0f, 1.0f,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }

    @Override
    public SignPoleBlockRenderState createRenderState() {
        return new SignPoleBlockRenderState();
    }

    @Override
    public void extractRenderState(SignPoleBlockEntity blockEntity, SignPoleBlockRenderState state, float tickProgress, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.rotation = blockEntity.getRotationValue();
        state.shouldRender = blockEntity.isShouldRender();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
