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
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SignBlockBackstepCoordsChange;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SignBlockRotationPayload;
import at.tobiazsh.myworld.traffic_addition.utils.Coordinates;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class SignBlockEntityRenderer<T extends SignBlockEntity, G extends SignBlock> implements BlockEntityRenderer<T> {

    private final BakedModelManager bakedModelMgr;
    private Coordinates mountingOffset;
    private BlockPos attachmentBlockPos;
    public String textureIdentifier;

    public static float zOffsetRenderLayer = 3f;
    public static float zOffsetRenderLayerDefault = 3f;

    public SignBlockEntityRenderer(BakedModelManager bakedModelMgr) {
        this.bakedModelMgr = bakedModelMgr;
    }

    @Override
    public void render(T entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {

        G signBlock = (G) entity.getCachedState().getBlock();
        Coordinates backstepCoords = signBlock.getBackMovementCoordinates(entity.getCachedState());

        if(backstepCoords != entity.getBackstepCoords()) {
            ClientPlayNetworking.send(new SignBlockBackstepCoordsChange(entity.getPos(), backstepCoords.x, backstepCoords.y, backstepCoords.z, backstepCoords.direction));
        }

        reassignValues(backstepCoords.direction, entity);

        BlockEntity blockEntityBehind = MinecraftClient.getInstance().world.getBlockEntity(attachmentBlockPos);

        textureIdentifier = entity.getTexturePath();

        matrices.push();

        if(blockEntityBehind instanceof SignPoleBlockEntity signPoleBlockEntity) {
            int rotationDegrees = signPoleBlockEntity.getRotationValue() + 180;

            if (entity.getRotation() != rotationDegrees) {
                ClientPlayNetworking.send(new SignBlockRotationPayload(entity.getPos(), rotationDegrees));
            }

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

            renderSignHolder(entity, matrices, vertexConsumers, light, overlay, entity.getCachedState().get(SignBlock.FACING));
        }

        BlockStateModel signBlockStateModel = bakedModelMgr.getBlockModels().getModel(entity.getCachedState());
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(
                matrices.peek(),
                consumer,
                signBlockStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        renderTextureOnModel(entity, matrices, vertexConsumers, light, overlay);

        matrices.pop();
    }

    private void reassignValues(Direction offsetDirection, SignBlockEntity entity) {
        switch (offsetDirection) {
            case EAST -> {
                attachmentBlockPos = entity.getPos().west();
                mountingOffset = new Coordinates(-1, 0, 0, Direction.WEST);
            }

            case SOUTH -> {
                attachmentBlockPos = entity.getPos().north();
                mountingOffset = new Coordinates(0, 0, -1, Direction.NORTH);
            }

            case WEST -> {
                attachmentBlockPos = entity.getPos().east();
                mountingOffset = new Coordinates(1, 0, 0, Direction.EAST);
            }

            default -> {
                attachmentBlockPos = entity.getPos().south();
                mountingOffset = new Coordinates(0, 0, 1, Direction.SOUTH);
            }
        }
    }

    protected void renderTextureOnModel(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, textureIdentifier);

        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffsetRenderLayer, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT, texture);
        RenderLayer renderLayer = imageLayering.buildRenderLayer();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.push();
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0.57, 1, 0);

        rotateTexture(entity, matrices);

        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -0.5f, -0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.5f, -0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.5f, 0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -0.5f, 0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);

        matrices.pop();
    }

    /**
     * Rotates the texture of the sign block entity based on its facing direction.
     */
    public static void rotateTexture(SignBlockEntity entity, MatrixStack matrices) {
        Direction facing = entity.getCachedState().get(SignBlock.FACING);

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
    private static void moveHolderBack(SignBlockEntity entity, MatrixStack matrices) {
        switch (entity.getCachedState().get(SignBlock.FACING)) {
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

    private void renderSignHolder(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

        BlockStateModel signHolderModel = bakedModelMgr.getBlockModels().getModel(ModBlocks.SIGN_HOLDER_BLOCK.getBlock().getDefaultState());

        matrices.push();

        moveHolderBack(entity, matrices);


        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 * getRotationManeuverCount(facing)));
        matrices.translate(-0.5, -0.5, -0.5);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(
                matrices.peek(),
                consumer,
                signHolderModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        matrices.pop();
    }
}
