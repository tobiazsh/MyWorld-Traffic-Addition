package at.tobiazsh.myworld.traffic_addition.components.Renderers;


/*
 * @created 09/09/2024 (DD/MM/YYYY) - 20:34
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.Blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.Utils.BlockPosExtended;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import java.util.*;

public class CustomSignBlockEntityRenderer implements BlockEntityRenderer<CustomizableSignBlockEntity> {

    private BakedModelManager bakedModelManager;
    private BlockRenderManager blockRenderManager;
    private Direction direction;
    private String borderModelPath;
    private int rotation = 0;

    public CustomSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        bakedModelManager = MinecraftClient.getInstance().getBakedModelManager();
        blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        borderModelPath = "block/customizable_sign_block_border_all";
    }

    @Override
    public void render(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        if (!entity.isRendering()) return;

        direction = entity.getCachedState().get(CustomizableSignBlock.FACING);
        BakedModel baseModel = bakedModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block"), "facing=" + direction.getName()));

        BlockEntity masterEntity = MinecraftClient.getInstance().world.getBlockEntity(entity.getMasterPos());

        if(masterEntity instanceof CustomizableSignBlockEntity) {
            rotation = ((CustomizableSignBlockEntity) masterEntity).getRotation();
        }

        matrices.push();

        rotateSign(entity, direction, rotation, matrices);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), baseModel, 1.0f, 1.0f, 1.0f, light, overlay);

        renderBorder(entity, tickDelta, matrices, vertexConsumers, light, overlay, entity.getCachedState().get(CustomizableSignBlock.FACING));

        if (entity.isMaster()) {
            renderSignPoles(entity, tickDelta, matrices, vertexConsumers, light, overlay);
            renderSigns(entity, tickDelta, matrices, vertexConsumers, light, overlay, direction);
        }

        matrices.pop();
    }

    private void renderSigns(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction FACING) {
        String constructedSignPositions = entity.getSignPositions();

        if(constructedSignPositions.isEmpty()) return;

        List<BlockPos> signPositions = CustomizableSignBlockEntity.deconstructBlockPosListString(constructedSignPositions);
        BakedModel signModel = bakedModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block"), "facing=" + FACING.getName()));

        for (BlockPos sign : signPositions) {
            if (entity.getWorld().getBlockEntity(sign) instanceof CustomizableSignBlockEntity signBlockEntity) renderSign(signBlockEntity, tickDelta, matrices, vertexConsumers, light, overlay, FACING, signModel);
        }
    }

    private void renderSign(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction FACING, BakedModel model) {
        matrices.push();

        BlockPos masterPos = entity.getMasterPos();
        BlockPos offset = BlockPosExtended.getOffset(masterPos, entity.getPos());
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1));
        matrices.translate(offset.getX(), offset.getY(), offset.getZ());

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), model, 1.0f, 1.0f, 1.0f, light, overlay);

        renderBorder(entity, tickDelta, matrices, vertexConsumers, light, overlay, FACING);

        matrices.pop();
    }

    private void renderBorder(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction FACING) {
        matrices.push();

        borderModelPath = "block/" + entity.getBorderType();

        if (Objects.equals(borderModelPath, "block/customizable_sign_block_border_none")) {
            matrices.pop();
            return;
        }

        BakedModel model = bakedModelManager.getModel(Identifier.of(MyWorldTrafficAddition.MOD_ID, borderModelPath));

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getFacingRotation(FACING)));
        matrices.translate(-0.5, -0.5, -0.5);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), model, 1.0f, 1.0f, 1.0f, light, overlay);

        matrices.pop();
    }

    private void renderSignPoles(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        String signPolePositionsString = entity.getSignPolePositions();

        if(signPolePositionsString.isEmpty()) return;

        BakedModel signPoleModel = bakedModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "sign_pole_block"), ""));
        List<BlockPos> positions = CustomizableSignBlockEntity.deconstructBlockPosListString(signPolePositionsString);

        positions.forEach(pos -> renderSignPole(entity, tickDelta, matrices, vertexConsumers, light, overlay, pos, signPoleModel));
    }

    private void renderSignPole(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockPos position, BakedModel signPoleModel) {
        matrices.push();

        BlockPos masterPos = entity.getMasterPos();
        BlockPos offset = BlockPosExtended.getOffset(masterPos, position);

        // Correct positions of the poles
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1));
        matrices.translate(offset.getX(), offset.getY(), offset.getZ());

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), signPoleModel, 1.0f, 1.0f, 1.0f, light, overlay);

        matrices.pop();
    }

    private int getFacingRotation(Direction FACING) {
        switch (FACING) {
            default -> { return 0; }
            case SOUTH -> { return 180; }
            case WEST -> { return 90; }
            case EAST -> { return 270; }
        }
    }

    private void rotateSign(CustomizableSignBlockEntity entity, Direction FACING, int rotationDegrees, MatrixStack matrices) {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        matrices.translate(-0.5, -0.5, -0.5);
    }
}
