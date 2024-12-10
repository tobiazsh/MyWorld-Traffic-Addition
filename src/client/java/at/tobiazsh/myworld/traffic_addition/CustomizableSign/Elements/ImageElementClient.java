package at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ImUtil.rotatePivot;
import static at.tobiazsh.myworld.traffic_addition.components.Renderers.SignBlockEntityRenderer.getFacingRotation;

public class ImageElementClient extends ImageElement implements ClientElementRenderInterface {

    public ImageElementClient(float x, float y, float width, float height, float factor, float rotation, Texture texture) {
        super(x, y, width, height, factor, rotation, texture);
    }

    public ImageElementClient(float x, float y, float width, float height, float factor, float rotation, String path) {
        super(x, y, width, height, factor, rotation, path);
    }

    public ImageElementClient(float x, float y, float width, float height, float factor, Texture texture) {
        this(x, y, width, height, factor, 0, texture);
    }

    public ImageElementClient(float x, float y, float width, float height, float factor, String path) {
        this(x, y, width, height, factor, 0, path);
    }

    private ImVec2 p0, p1, p2, p3;

    /**
     * Renders the image element in an ImGui Context.
     */
    public void renderImGui() {
        ImDrawList drawList = ImGui.getWindowDrawList();

        ImVec2 windowPos = new ImVec2(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY());
        float[] color = this.getColor();

        p0 = new ImVec2(windowPos.x + this.x, windowPos.y + this.y); // Top Left Vertices
        p1 = new ImVec2(windowPos.x + this.x + this.width, windowPos.y + this.y); // Top Right Vertices
        p2 = new ImVec2(windowPos.x + this.x + this.width, windowPos.y + this.y + this.height); // Bottom Right Vertices
        p3 = new ImVec2(windowPos.x + this.x, windowPos.y + this.y + height); // Bottom Left Vertices

        rotateTexture(rotation, windowPos);

        float uv0X = 0.0f, uv0Y = 0.0f;
        float uv1X = 1.0f, uv1Y = 0.0f;
        float uv2X = 1.0f, uv2Y = 1.0f;
        float uv3X = 0.0f, uv3Y = 1.0f;

        drawList.addImageQuad(
                this.getTexture().getTextureId(),
                p0.x, p0.y,
                p1.x, p1.y,
                p2.x, p2.y,
                p3.x, p3.y,
                uv0X, uv0Y,
                uv1X, uv1Y,
                uv2X, uv2Y,
                uv3X, uv3Y,
                ImGui.colorConvertFloat4ToU32(color[0], color[1], color[2], color[3]) // Color doesn't work yet
        );
    }

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
    @Override
    public void renderMinecraft(BaseElement element, int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

        if (!(element instanceof ImageElement)) {
            MyWorldTrafficAddition.LOGGER.error("Element provided in renderMinecraft() Method is not an ImageElement!");
            return;
        }

        float w = element.calcBlocks(element.getWidth());
        float h = element.calcBlocks(element.getHeight());
        float x = element.calcBlocks(element.getX());
        float y = element.calcBlocks(element.getY());
        float rotation = element.getRotation();
        float[] color = element.getColor();

        BlockPosFloat shiftForward = new BlockPosFloat(0, 0, 0).offset(facing, zOffset + ((indexInList + 1) * 0.005f));
        BlockPosFloat renderPos = new BlockPosFloat(0, y * (-1), 0).offset(CustomizableSignBlockEntity.getRightSideDirection(facing.getOpposite()), x);

        matrices.push();

        matrices.translate(0, csbeHeight - h, 0); // Render element on top left corner (Default Position)
        matrices.translate(shiftForward.x, shiftForward.y, shiftForward.z); // Shift element forward depending on layer position to prevent z-fighting
        matrices.translate(renderPos.x, renderPos.y, renderPos.z); // Shift element to the right position

        // Bind texture to vertices
        Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, ((ImageElement) element).getResourcePath());
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(texture);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        // Rotate to the same direction as the block (opposite because the block is facing a certain direction but the canvas is on the opposite)
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getFacingRotation(facing.getOpposite())));
        matrices.translate(-0.5, -0.5, -0.5);

        // Rotate around the element's center
        matrices.translate(w / 2, h / 2, 0); // Move origin to element center
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rotation));
        matrices.translate(-w / 2, -h / 2, 0); // Move origin back

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        // Top left
        vertexConsumer.vertex(positionMatrix, 0 , 0, 0)
                .texture(0, 1)
                .color(color[0], color[1], color[2], color[3])
                .light(light)
                .overlay(overlay)
                .normal(0, 0, 1);

        // Top right
        vertexConsumer.vertex(positionMatrix, w, 0, 0)
                .texture(1, 1)
                .color(color[0], color[1], color[2], color[3])
                .light(light)
                .overlay(overlay)
                .normal(0, 0, 1);

        // Bottom right
        vertexConsumer.vertex(positionMatrix, w, h, 0)
                .texture(1, 0)
                .color(color[0], color[1], color[2], color[3])
                .light(light)
                .overlay(overlay)
                .normal(0, 0, 1);

        // Bottom left
        vertexConsumer.vertex(positionMatrix, 0, h, 0)
                .texture(0, 0)
                .color(color[0], color[1], color[2], color[3])
                .light(light)
                .overlay(overlay)
                .normal(0, 0, 1);

        matrices.pop();
    }

    /**
     * Sets the UV coordinates of the image element
     * @param p0tl Top left UV coordinate
     * @param p1tr Top right UV coordinate
     * @param p2br Bottom right UV coordinate
     * @param p3bl Bottom left UV coordinate
     */
    public void setUV(ImVec2 p0tl, ImVec2 p1tr, ImVec2 p2br, ImVec2 p3bl) {
        this.p0 = p0tl;
        this.p1 = p1tr;
        this.p2 = p2br;
        this.p3 = p3bl;
    }

    /**
     * Rotates the texture by a given angle
     * @param angle The angle to rotate by
     * @param windowPos The position of the window
     */
    public void rotateTexture(float angle, ImVec2 windowPos){
        // For efficiency
        if (angle == 0) return;

        ImVec2 center = new ImVec2(windowPos.x + this.x + this.width / 2, windowPos.y + this.y + this.height / 2);
        float radians = (float) Math.toRadians(angle);

        p0 = rotatePivot(p0, center, radians);
        p1 = rotatePivot(p1, center, radians);
        p2 = rotatePivot(p2, center, radians);
        p3 = rotatePivot(p3, center, radians);
    }

    /**
     * Creates a new ImageElementClient object from an ImageElement object
     * @param element The ImageElement object to create the ImageElementClient object from
     * @return The ImageElementClient object
     */
    public static ImageElementClient fromImageElement(ImageElement element) {
        ImageElementClient img = new ImageElementClient(element.getX(), element.getY(), element.getWidth(), element.getHeight(), element.getFactor(), element.getRotation(), element.getTexture());
        img.setColor(element.getColor());
        return img;
    }
}
