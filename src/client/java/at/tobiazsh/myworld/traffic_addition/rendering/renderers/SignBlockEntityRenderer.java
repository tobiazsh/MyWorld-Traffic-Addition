package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 16:58
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignBlockRenderState;
import at.tobiazsh.myworld.traffic_addition.utils.math.Coordinates;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SignBlockEntityRenderer<T extends SignBlockEntity> implements BlockEntityRenderer<T, SignBlockRenderState> {

    private final BakedModelManager bakedModelMgr;

    public static float zOffsetRenderLayer = 3f;
    public static float zOffsetRenderLayerDefault = 3f;

    public SignBlockEntityRenderer(BakedModelManager bakedModelMgr) {
        this.bakedModelMgr = bakedModelMgr;
    }

    @Override
    public void render(SignBlockRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {

        int light = state.lightmapCoordinates;

        VertexConsumerProvider provider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        Direction facing = state.blockState.get(SignBlock.FACING);

        //Coordinates backstepCoords = Coordinates.getNormalInDirection(-1.55f, facing);

        // Commented out for now; Don't know if it's necessary to constantly sync this value
//        if(backstepCoords != entity.getBackstepCoords()) {
//            ClientPlayNetworking.send(new SignBlockBackstepCoordsChange(entity.getPos(), backstepCoords.x, backstepCoords.y, backstepCoords.z, backstepCoords.direction));
//        }

        BlockEntity blockEntityBehind = MinecraftClient.getInstance().world.getBlockEntity( getBlockPosBehind(facing, state.pos) );

        matrices.push();

        if(blockEntityBehind instanceof SignPoleBlockEntity signPoleBlockEntity) {
            int rotationDegrees = signPoleBlockEntity.getRotationValue() + 180;
            Coordinates mountingOffset = Coordinates.getNormalInDirection(facing.getOpposite());

            // Commented out for now; Don't know if it's necessary to constantly sync this value
//            if (entity.getRotation() != rotationDegrees) {
//                ClientPlayNetworking.send(new SignBlockRotationPayload(entity.getPos(), rotationDegrees));
//            }

            matrices.translate(mountingOffset.x, mountingOffset.y, mountingOffset.z); // Place it in the correct position
            matrices.translate(0.5, 0, 0.5); // Set it back by half a block in each direction
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees)); // Rotate it to the desired degree
            matrices.translate(-0.5, 0, -0.5); // Return to original position

            // Now it's inside out

            matrices.translate(mountingOffset.x, mountingOffset.y, mountingOffset.z); // Set it back by another block
            matrices.translate(0.5, 0, 0.5); // Set it back by half a block in each direction
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180)); // Rotate it 180° to turn it the correct way
            matrices.translate(-0.5, 0, -0.5); // Set it back to original position
            // Do not set it back by -1 again. Since the model is right on the side of the next block, it does not need this behaviour.

            renderSignHolder(queue, matrices, light, facing);
        }

        BlockStateModel signBlockStateModel = bakedModelMgr.getBlockModels().getModel(state.blockState);

        renderTextureOnModel(state.texturePath, matrices, provider, facing, light, OverlayTexture.DEFAULT_UV);

        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getSolid(),
                signBlockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }

    @Override
    public SignBlockRenderState createRenderState() {
        return new SignBlockRenderState();
    }

    @Override
    public void updateRenderState(T blockEntity, SignBlockRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.texturePath = blockEntity.getTexturePath();
    }



    // ----------------------------------------------------
    // RENDER UTILITY METHODS -----------------------------

    protected void renderTextureOnModel(String texturePath, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Direction facing, int light, int overlay) {
        Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, texturePath);

        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffsetRenderLayer, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT, texture);
        RenderLayer renderLayer = imageLayering.buildRenderLayer();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.push();
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0.57, 1, 0);

        rotateTexture(facing, matrices);

        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -0.5f, -0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.5f, -0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.5f, 0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -0.5f, 0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);

        matrices.pop();
    }

    private void renderSignHolder(OrderedRenderCommandQueue queue, MatrixStack matrices, int light, Direction facing) {

        BlockStateModel signHolderModel = bakedModelMgr.getBlockModels().getModel(ModBlocks.SIGN_HOLDER_BLOCK.getBlock().getDefaultState());

        matrices.push();

        moveHolderBack(facing, matrices);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 * getRotationManeuverCount(facing)));
        matrices.translate(-0.5, -0.5, -0.5);

        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getSolid(),
                signHolderModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
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
    public static void rotateTexture(Direction facing, MatrixStack matrices) {
        switch (facing) {
            case EAST -> {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                matrices.translate(-0.5, -0.5, -0.5);
            }
            case WEST -> {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90));
                matrices.translate(0.5, -0.5, -0.36);
            }
            case SOUTH -> matrices.translate(0.43, 0, 0.57);
            default -> { // NORTH
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                matrices.translate(0.07, -0.5, -0.93);
            }
        }
    }

    /**
     * Rotates the holder of the sign block entity based on its facing direction.
     */
    private static void moveHolderBack(Direction facing, MatrixStack matrices) {
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
