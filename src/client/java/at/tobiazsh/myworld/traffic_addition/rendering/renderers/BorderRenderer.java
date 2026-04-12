package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;

public class BorderRenderer {

    private static BlockStateModel borderStateModel;
    private static BlockStateModel cornerStateModel;
    private static RenderType borderRenderLayer;

    /**
     * Initializes the BorderRenderer with the necessary models and render layer.
     */
    public static void init(ModelManager bakedModelManager, CustomRenderLayer.ModelLayering modelLayering) {
        BlockState borderBlockState = ModBlocks.CUSTOMIZABLE_SIGN_BORDER.getBlock().defaultBlockState();
        BlockState cornerBlockState = ModBlocks.CUSTOMIZABLE_SIGN_CORNER_BIT.getBlock().defaultBlockState();

        borderStateModel = bakedModelManager.getBlockModelShaper().getBlockModel(borderBlockState);
        cornerStateModel = bakedModelManager.getBlockModelShaper().getBlockModel(cornerBlockState);

        borderRenderLayer = modelLayering.buildRenderType();
    }

    // Here for optimization, so we don't have to create a new BlockPosFloat every time
    private static final BlockPosFloat offsetUp = new BlockPosFloat(0, 0.46875f, 0); // 15/32f == 0.46875f because 16/32 is half the block minus the one pixel for the border itself
    private static final BlockPosFloat offsetDown = new BlockPosFloat(0, -0.46875f, 0); // -15/32f == -0.46875f
    private static final float globalBorderOffset = 0.46875f; // 13/32f == 0.40625f, which is the distance from the center of the sign to the border

    /**
     * Renders the borders of a customizable sign based on the given entity's properties.
     */
    public static void render(
            SubmitNodeCollector queue,
            PoseStack matrices,
            BorderProperty borders,
            int light,
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

        matrices.pushPose();

        // Borders

        if (borders.up())
            renderEdge(queue, matrices, light, 90, offsetUp, offsetBack, facing);

        if (borders.down())
            renderEdge(queue, matrices, light, 270, offsetDown, offsetBack, facing);

        if (borders.left())
            renderEdge(queue, matrices, light, 0, offsetLeft, offsetBack, facing); // Not really necessary, just for the sake of it

        if (borders.right())
            renderEdge(queue, matrices, light, 180, offsetRight, offsetBack, facing); // Not really necessary, just for the sake of it


        // Corners

        if (borders.cornerUpRight())
            renderCorner(queue, matrices, light, offsetRight, globalBorderOffset, offsetBack);

        if (borders.cornerUpLeft())
            renderCorner(queue, matrices, light, offsetLeft, globalBorderOffset, offsetBack);

        if (borders.cornerDownRight())
            renderCorner(queue, matrices, light, offsetRight, -globalBorderOffset, offsetBack);

        if (borders.cornerDownLeft())
            renderCorner(queue, matrices, light, offsetLeft, -globalBorderOffset, offsetBack);

        matrices.popPose();
    }

    /**
     * Renders the border of a customizable sign in the given direction.
     *
     * @param matrices the MatrixStack to use for rendering
     * @param light light level
     * @param angle the angle to rotate the border
     * @param offset the desired offsetXZ in any direction
     * @param offsetBack the offsetXZ to move back to align with the sign's surface
     * @param facing the facing direction of the original sign
     */
    private static void renderEdge(
            SubmitNodeCollector queue,
            PoseStack matrices,
            int light,
            int angle,
            BlockPosFloat offset,
            BlockPosFloat offsetBack,
            Direction facing
    ) {
        matrices.pushPose();

        matrices.translate(offsetBack.x, offsetBack.y, offsetBack.z); // Move back to align with the sign's surface
        matrices.translate(offset.x, offset.y, offset.z);

        // Rotating
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing))); // Rotate towards face of sign
        matrices.mulPose(Axis.ZP.rotationDegrees(angle)); // Rotate by the given angle
        matrices.translate(-0.5, -0.5, -0.5);

        queue.submitBlockModel(
                matrices,
                borderRenderLayer,
                borderStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }

    /**
     * Renders the necessary corner bits for the customizable sign.
     *
     * @param matrices the MatrixStack to use for rendering
     * @param light light level
     * @param offsetXZ the desired offsetXZ in either X or Z direction
     * @param offsetY the desired offsetY in either Up or Down direction
     * @param offsetBack the offsetXZ to move back to align with the sign's surface
     */
    private static void renderCorner(
            SubmitNodeCollector queue,
            PoseStack matrices,
            int light,
            BlockPosFloat offsetXZ,
            float offsetY,
            BlockPosFloat offsetBack
    ) {
        matrices.pushPose();

        // No rotating here, just translating yay (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        matrices.translate(offsetBack.x, offsetBack.y, offsetBack.z);
        matrices.translate(offsetXZ.x, offsetXZ.y, offsetXZ.z);
        matrices.translate(0, offsetY, 0);

        queue.submitBlockModel(
                matrices,
                borderRenderLayer,
                cornerStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }

    // Image what a pain it would be if we'd live in a 4-dimensional world to get 4-dimensional games going lol
}
