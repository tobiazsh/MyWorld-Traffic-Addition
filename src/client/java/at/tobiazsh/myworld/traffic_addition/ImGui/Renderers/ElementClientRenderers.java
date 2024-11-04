package at.tobiazsh.myworld.traffic_addition.ImGui.Renderers;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class ElementClientRenderers {
    public static class ImageElementRenderer {

        public static float zOffset = 0.075f;

        /**
         * Renders an ImageElement in Minecraft
         * @param element Element to render
         * @param indexInList Index of the element in the list; For layering purposes
         * @param csbeHeight Height of the CustomizableSignBlockEntity
         * @param matrices MatrixStack
         * @param vertexConsumers VertexConsumerProvider
         * @param light Light level
         * @param overlay Overlay level
         * @param facing Direction the element should face
         */
        public void renderMinecraft(ImageElement element, int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

            float w = element.calcBlocks(element.getWidth());
            float h = element.calcBlocks(element.getHeight());
            float x = element.calcBlocks(element.getX());
            float y = element.calcBlocks(element.getY());
            float rotation = element.getRotation();

            BlockPosFloat shiftForward = new BlockPosFloat(0, 0, 0).offset(facing, zOffset + ((indexInList + 1) * 0.005f));
            BlockPosFloat renderPos = new BlockPosFloat(0, y * (-1), 0).offset(CustomizableSignBlockEntity.getRightSideDirection(facing.getOpposite()), x);

            matrices.push();

            matrices.translate(0, csbeHeight - h, 0); // Render element on top left corner (Default Position)
            matrices.translate(shiftForward.x, shiftForward.y, shiftForward.z); // Shift element forward depending on layer position to prevent z-fighting
            matrices.translate(renderPos.x, renderPos.y, renderPos.z); // Shift element to the right position

            // Bind texture to vertices
            Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, element.getResourcePath());
            RenderLayer renderLayer = RenderLayer.getEntityCutout(texture);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

            // Rotate to the same direction as the block (opposite because the block is facing a certain direction but the canvas is on the opposite)
            matrices.translate(0.5, 0.5, 0.5);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getFacingRotation(facing.getOpposite())));
            matrices.translate(-0.5, -0.5, -0.5);

            // Rotate the element around it's center
            matrices.translate(w / 2, h / 2, 0); // Move origin to element center
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rotation));
            matrices.translate(-w / 2, -h / 2, 0); // Move origin back

            // Top left
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0 , 0, 0)
                    .texture(0, 1)
                    .color(1f, 1f, 1f, 1f)
                    .light(light)
                    .overlay(overlay)
                    .normal(0, 0, 1);

            // Top right
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), w, 0, 0)
                    .texture(1, 1)
                    .color(1f, 1f, 1f, 1f)
                    .light(light)
                    .overlay(overlay)
                    .normal(0, 0, 1);

            // Bottom right
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), w, h, 0)
                    .texture(1, 0)
                    .color(1f, 1f, 1f, 1f)
                    .light(light)
                    .overlay(overlay)
                    .normal(0, 0, 1);

            // Bottom left
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0, h, 0)
                    .texture(0, 0)
                    .color(1f, 1f, 1f, 1f)
                    .light(light)
                    .overlay(overlay)
                    .normal(0, 0, 1);

            matrices.pop();
        }
    }

    public static class TextElementRenderer {
        public static void renderMinecraft() {
            // Render Text Element
        }
    }

    public static int getFacingRotation(Direction FACING) {
        switch (FACING) {
            default -> { return 0; }
            case SOUTH -> { return 180; }
            case WEST -> { return 90; }
            case EAST -> { return 270; }
        }
    }
}
