package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 16:58
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignBlockRenderState;
import at.tobiazsh.myworld.traffic_addition.utils.math.Coordinates;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.ModelManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SignBlockEntityRenderer<T extends SignBlockEntity> implements BlockEntityRenderer<@NotNull T, @NotNull SignBlockRenderState> {

    private final ModelManager bakedModelMgr;
    
    private final static RandomSource random = RandomSource.create();

    public SignBlockEntityRenderer(ModelManager bakedModelMgr) {
        this.bakedModelMgr = bakedModelMgr;
    }

    @Override
    public void submit(
            SignBlockRenderState state,
            PoseStack matrices,
            @NonNull SubmitNodeCollector queue,
            @NonNull CameraRenderState camera
    ) {

        int light = state.lightCoords;

        MultiBufferSource provider = Minecraft.getInstance().renderBuffers().bufferSource();
        Direction facing = state.blockState.getValue(SignBlock.FACING);

        BlockEntity blockEntityBehind = Minecraft.getInstance().level.getBlockEntity( getBlockPosBehind(facing, state.blockPos) );

        matrices.pushPose();

        if(blockEntityBehind instanceof SignPoleBlockEntity signPoleBlockEntity) {
            int rotationDegrees = signPoleBlockEntity.getRotationValue() + 180;
            Coordinates mountingOffset = Coordinates.getNormalInDirection(facing.getOpposite());

            matrices.translate(mountingOffset.x, mountingOffset.y, mountingOffset.z); // Place it in the correct position
            matrices.translate(0.5, 0, 0.5); // Set it back by half a block in each direction
            matrices.mulPose(Axis.YP.rotationDegrees(rotationDegrees)); // Rotate it to the desired degree
            matrices.translate(-0.5, 0, -0.5); // Return to original position

            // Now it's inside out

            matrices.translate(mountingOffset.x, mountingOffset.y, mountingOffset.z); // Set it back by another block
            matrices.translate(0.5, 0, 0.5); // Set it back by half a block in each direction
            matrices.mulPose(Axis.YP.rotationDegrees(180)); // Rotate it 180° to turn it the correct way
            matrices.translate(-0.5, 0, -0.5); // Set it back to original position
            // Do not set it back by -1 again. Since the model is right on the side of the next block, it does not need this behaviour.

            renderSignHolder(queue, matrices, light, facing);
        }

        BlockStateModel signBlockStateModel = this.bakedModelMgr.getBlockStateModelSet().get(state.blockState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        signBlockStateModel.collectParts(random, parts);

        renderTextureOnModel(state.texturePath, matrices, provider, facing, light, OverlayTexture.NO_OVERLAY);

        queue.submitBlockModel(
                matrices,
                RenderTypes.solidMovingBlock(),
                parts,
                new int[] {},
                light, OverlayTexture.NO_OVERLAY, 0
        );

        matrices.popPose();
    }

    @Override
    public SignBlockRenderState createRenderState() {
        return new SignBlockRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, SignBlockRenderState state, float tickProgress, @NotNull Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.texturePath = blockEntity.getTexturePath();
        state.blockState = blockEntity.getBlockState();
    }



    // ----------------------------------------------------
    // RENDER UTILITY METHODS -----------------------------

    protected void renderTextureOnModel(String texturePath, PoseStack matrices, MultiBufferSource vertexConsumers, Direction facing, int light, int overlay) {
        Identifier texture = Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, texturePath);

        float zOffset = MyWorldTrafficAdditionClient.getClientPreferences()
                .signs.viewDistance
                .getOrDefault()
                .value();

        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffset, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT, texture);
        RenderType renderLayer = imageLayering.buildRenderType();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.pushPose();
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0.57, 1, 0);

        rotateTexture(facing, matrices);

        vertexConsumer.addVertex(matrices.last().pose(), -0.5f, -0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(0.0f, 1.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrices.last().pose(), 0.5f, -0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(1.0f, 1.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrices.last().pose(), 0.5f, 0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(1.0f, 0.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrices.last().pose(), -0.5f, 0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(0.0f, 0.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);

        matrices.popPose();
    }

    private void renderSignHolder(SubmitNodeCollector queue, PoseStack matrices, int light, Direction facing) {

        matrices.pushPose();

        moveHolderBack(facing, matrices);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(90 * getRotationManeuverCount(facing)));
        matrices.translate(-0.5, -0.5, -0.5);

        BlockStateModel signBlockStateModel = this.bakedModelMgr.getBlockStateModelSet().get(ModBlocks.SIGN_HOLDER_BLOCK.getBlock().defaultBlockState());
        List<BlockStateModelPart> parts = new ArrayList<>();
        signBlockStateModel.collectParts(random, parts);

        queue.submitBlockModel(
                matrices,
                RenderTypes.solidMovingBlock(),
                parts,
                new int[]{},
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }



    // ----------------------------------------------------
    // HELPER METHODS -------------------------------------

    /**
     * Gets the block position of the block behind the sign
     * @param facingDirection In which direction the sign is facing.
     * @param signBlockPos The position of the sign
     * @return The block position of the block the sign is attached to.
     */
    private BlockPos getBlockPosBehind(Direction facingDirection, BlockPos signBlockPos) {
        return switch (facingDirection) {
            case EAST -> signBlockPos.west();
            case SOUTH -> signBlockPos.north();
            case WEST -> signBlockPos.east();
            default -> signBlockPos.south();
        };
    }

    /**
     * Rotates the texture of the sign block entity based on its facing direction.
     */
    public static void rotateTexture(Direction facing, PoseStack matrices) {
        switch (facing) {
            case EAST -> {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.mulPose(Axis.YP.rotationDegrees(90));
                matrices.translate(-0.5, -0.5, -0.5);
            }
            case WEST -> {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.mulPose(Axis.YN.rotationDegrees(90));
                matrices.translate(0.5, -0.5, -0.36);
            }
            case SOUTH -> matrices.translate(0.43, 0, 0.57);
            default -> { // NORTH
                matrices.translate(0.5, 0.5, 0.5);
                matrices.mulPose(Axis.YP.rotationDegrees(180));
                matrices.translate(0.07, -0.5, -0.93);
            }
        }
    }

    /**
     * Rotates the holder of the sign block entity based on its facing direction.
     */
    private static void moveHolderBack(Direction facing, PoseStack matrices) {
        switch (facing) {
            case SOUTH -> matrices.translate(0, 0, -1);
            case EAST -> matrices.translate(-1, 0, 0);
            case WEST -> matrices.translate(1, 0, 0);
            default -> matrices.translate(0, 0, 1);
        }
    }

    private static int getRotationManeuverCount(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 0;
            case WEST -> 3;
            default -> 2; // NORTH
        };
    }
}
