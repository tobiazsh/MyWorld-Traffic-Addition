package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class BorderRenderer {

    private static BlockStateModel borderStateModel;
    private static BlockStateModel cornerStateModel;
    private static BakedModelManager bakedModelManager;
    private static RenderLayer borderRenderLayer;

    public static void init(BakedModelManager bakedModelManager, CustomRenderLayer.ModelLayering modelLayering) {
        BorderRenderer.bakedModelManager = bakedModelManager;

        BlockState borderBlockState = ModBlocks.CUSTOMIZABLE_SIGN_BORDER.getBlock().getDefaultState();
        BlockState cornerBlockState = ModBlocks.CUSTOMIZABLE_SIGN_CORNER_BIT.getBlock().getDefaultState();

        borderStateModel = bakedModelManager.getBlockModels().getModel(borderBlockState);
        cornerStateModel = bakedModelManager.getBlockModels().getModel(cornerBlockState);

        borderRenderLayer = modelLayering.buildRenderLayer();
    }

    public static <T extends BlockEntity> void render(
            T entity,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            BorderProperty borders,
            int light,
            int overlay,
            Direction facing
    ) {

        matrices.push();

//
//        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(
//                matrices.peek(),
//                consumer,
//                borderStateModel,
//                1.0f, 1.0f, 1.0f,
//                light, overlay
//        );
//
//        matrices.pop();
//
//        matrices.push();
//

        VertexConsumer consumer = vertexConsumers.getBuffer(borderRenderLayer);

        BlockPosFloat pos = DirectionUtils.blockPosInDirection(
                facing.getOpposite(),
                new BlockPosFloat(0, 0, 0),
                13/32f
        );

        matrices.translate(pos.x, pos.y, pos.z);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(DirectionUtils.getFacingRotation(facing)));
        matrices.translate(-0.5, -0.5, -0.5);

        BlockModelRenderer.render(
                matrices.peek(),
                consumer,
                borderStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        matrices.pop();
    }

    private static void renderBorder(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int angle
    ) {
    }

    private static void rotateAngle(MatrixStack matrices) {
        matrices.push();

        //matrices.multiply();

        matrices.pop();
    }

    private static void renderCorners(MatrixStack matrices) {

    }

}
