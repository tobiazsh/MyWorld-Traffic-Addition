package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import net.minecraft.block.BlockState;
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
    private static RenderLayer borderRenderLayer;

    /**
     * Initializes the BorderRenderer with the necessary models and render layer.
     */
    public static void init(BakedModelManager bakedModelManager, CustomRenderLayer.ModelLayering modelLayering) {
        BlockState borderBlockState = ModBlocks.CUSTOMIZABLE_SIGN_BORDER.getBlock().getDefaultState();
        BlockState cornerBlockState = ModBlocks.CUSTOMIZABLE_SIGN_CORNER_BIT.getBlock().getDefaultState();

        borderStateModel = bakedModelManager.getBlockModels().getModel(borderBlockState);
        cornerStateModel = bakedModelManager.getBlockModels().getModel(cornerBlockState);

        borderRenderLayer = modelLayering.buildRenderLayer();
    }

    // Here for optimization, so we don't have to create a new BlockPosFloat every time
    private static final BlockPosFloat offsetUp = new BlockPosFloat(0, 0.46875f, 0); // 15/32f == 0.46875f because 16/32 is half the block minus the one pixel for the border itself
    private static final BlockPosFloat offsetDown = new BlockPosFloat(0, -0.46875f, 0); // -15/32f == -0.46875f
    private static final float globalBorderOffset = 0.46875f; // 13/32f == 0.40625f, which is the distance from the center of the sign to the border

    /**
     * Renders the borders of a customizable sign based on the given entity's properties.
     */
    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            BorderProperty borders,
            int light,
            int overlay,
            Direction facing
    ) {

        // Cannot store statically because it depends on the facing direction :(
        BlockPosFloat offsetLeft = new BlockPosFloat(0, 0, 0).offset(DirectionUtils.getRightSideDirection(facing).getOpposite(), -globalBorderOffset); // -15/32f == -0.46875f
        BlockPosFloat offsetRight = new BlockPosFloat(0, 0, 0).offset(DirectionUtils.getRightSideDirection(facing), -globalBorderOffset); // -15/32f == -0.46875f

        BlockPosFloat offsetBack = DirectionUtils.blockPosInDirection(
                facing.getOpposite(),
                new BlockPosFloat(0, 0, 0),
                0.40625f // 13/32f == 0.40625f, which is the distance from the center of the sign to the border
        ); // The amount it has to move back to exactly align with the signs surface

        VertexConsumer consumer = vertexConsumers.getBuffer(borderRenderLayer);

        matrices.push();

        // Borders

        if (borders.up())
            renderBorder(matrices, consumer, light, overlay, 90, offsetUp, offsetBack, facing);

        if (borders.down())
            renderBorder(matrices, consumer, light, overlay, 270, offsetDown, offsetBack, facing);

        if (borders.left())
            renderBorder(matrices, consumer, light, overlay, 0, offsetLeft, offsetBack, facing); // Not really necessary, just for the sake of it

        if (borders.right())
            renderBorder(matrices, consumer, light, overlay, 180, offsetRight, offsetBack, facing); // Not really necessary, just for the sake of it


        // Corners

        if (borders.cornerUpRight())
            renderCorner(matrices, consumer, light, overlay, offsetRight, globalBorderOffset, offsetBack);

        if (borders.cornerUpLeft())
            renderCorner(matrices, consumer, light, overlay, offsetLeft, globalBorderOffset, offsetBack);

        if (borders.cornerDownRight())
            renderCorner(matrices, consumer, light, overlay, offsetRight, -globalBorderOffset, offsetBack);

        if (borders.cornerDownLeft())
            renderCorner(matrices, consumer, light, overlay, offsetLeft, -globalBorderOffset, offsetBack);

        matrices.pop();
    }

    /**
     * Renders the border of a customizable sign in the given direction.
     *
     * @param matrices the MatrixStack to use for rendering
     * @param consumer the VertexConsumers to use for rendering
     * @param light light level
     * @param overlay overlay texture
     * @param angle the angle to rotate the border
     * @param offset the desired offsetXZ in any direction
     * @param offsetBack the offsetXZ to move back to align with the sign's surface
     * @param facing the facing direction of the original sign
     */
    private static void renderBorder(
            MatrixStack matrices,
            VertexConsumer consumer,
            int light,
            int overlay,
            int angle,
            BlockPosFloat offset,
            BlockPosFloat offsetBack,
            Direction facing
    ) {
        matrices.push();

        matrices.translate(offsetBack.x, offsetBack.y, offsetBack.z); // Move back to align with the sign's surface
        matrices.translate(offset.x, offset.y, offset.z);

        // Rotating
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(DirectionUtils.getFacingRotation(facing))); // Rotate towards face of sign
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle)); // Rotate by the given angle
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

    /**
     * Renders the necessary corner bits for the customizable sign.
     *
     * @param matrices the MatrixStack to use for rendering
     * @param consumer the VertexConsumers to use for rendering
     * @param light light level
     * @param overlay overlay texture
     * @param offsetXZ the desired offsetXZ in either X or Z direction
     * @param offsetY the desired offsetY in either Up or Down direction
     * @param offsetBack the offsetXZ to move back to align with the sign's surface
     */
    private static void renderCorner(
            MatrixStack matrices,
            VertexConsumer consumer,
            int light,
            int overlay,
            BlockPosFloat offsetXZ,
            float offsetY,
            BlockPosFloat offsetBack
    ) {
        matrices.push();

        // No rotating here, just translating yay (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        matrices.translate(offsetBack.x, offsetBack.y, offsetBack.z);
        matrices.translate(offsetXZ.x, offsetXZ.y, offsetXZ.z);
        matrices.translate(0, offsetY, 0);

        BlockModelRenderer.render(
                matrices.peek(),
                consumer,
                cornerStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        matrices.pop();
    }

    // Image what a pain it would be if we'd live in a 4-dimensional world to get 4-dimensional games going lol
}
