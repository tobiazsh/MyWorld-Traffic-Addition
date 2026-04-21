package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignPoleBlockRenderState;
import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@NullMarked
public class SignPoleBlockEntityRenderer implements BlockEntityRenderer<SignPoleBlockEntity, SignPoleBlockRenderState> {

    private static RandomSource random = RandomSource.create();
    @Nullable private static ImmutableList<BlockStateModelPart> signPoleParts = null;

    public SignPoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void submit(SignPoleBlockRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState renderState) {
        if(!state.shouldRender) return;

        if (signPoleParts == null) {
            BlockStateModel signBlockStateModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(ModBlocks.SIGN_POLE_BLOCK.getBlock().defaultBlockState());
            List<BlockStateModelPart> parts = new ArrayList<>();
            signBlockStateModel.collectParts(random, parts);
            signPoleParts = ImmutableList.copyOf(parts);
        }

        matrices.pushPose();

        matrices.translate(.5, 0, .5);
        matrices.mulPose(Axis.YP.rotationDegrees(state.rotation));
        matrices.translate(-0.5f, 0, -0.5f);

        queue.submitBlockModel(
                matrices,
                RenderTypes.solidMovingBlock(),
                signPoleParts,
                new int[] {},
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
