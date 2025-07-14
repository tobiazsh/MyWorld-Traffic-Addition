package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Textures;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.UUID;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.ImUtil.rotatePivot;
import static at.tobiazsh.myworld.traffic_addition.rendering.renderers.SignBlockEntityRenderer.getFacingRotation;

public class ImageElementClient extends ImageElement implements ClientElementInterface {

    public boolean textureLoaded = false;

    public ImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            String path,
            UUID parentId
    ) {
        super(x, y, width, height, factor, rotation, path, parentId);
    }

    public ImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            Texture texture,
            UUID parentId
    ) {
        super(x, y, width, height, factor, rotation, null, parentId);
        this.elementTexture = texture;
        this.textureLoaded = true; // Assume texture is loaded if provided
    }

    public ImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            String path,
            UUID id,
            UUID parentId
    ) {
        this(x, y, width, height, factor, rotation, path, parentId);
        this.id = id; // Set the ID for the element
    }

    public void checkSize() {
        if (this.width == -1 || this.height == -1) {
            MyWorldTrafficAddition.LOGGER.debug("ImageElement has not been initialized properly! x, y, width or height is -1! Sizing auto!");
            this.sizeAuto();
        }
    }

    private ImVec2 p0, p1, p2, p3;

    /**
     * Renders the image element in an ImGui Context.
     */
    public void renderImGui(float scale) {
        ImDrawList drawList = ImGui.getWindowDrawList();

        if (!this.textureLoaded) this.loadTexture();

        if (this.getTexture() == null) {
            MyWorldTrafficAddition.LOGGER.error("Texture is null for ImageElement!");
            return;
        }

        checkSize();

        ImVec2 windowPos = new ImVec2(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY());
        float[] color = this.getColor();

        // Calculate points and account for scale
        p0 = new ImVec2(windowPos.x + x * scale, windowPos.y + y * scale); // Top Left Vertices
        p1 = new ImVec2(windowPos.x + (x + width) * scale, windowPos.y + y * scale); // Top Right Vertices
        p2 = new ImVec2(windowPos.x + (x + width) * scale, windowPos.y + (y + height) * scale); // Bottom Right Vertices
        p3 = new ImVec2(windowPos.x + x * scale, windowPos.y + (y + height) * scale); // Bottom Left Vertices

        rotateTexture(
                rotation,
                windowPos,
                new ImVec2(
                        windowPos.x + (this.x + this.width / 2) * scale,
                        windowPos.y + (this.y + this.height / 2) * scale
                )
        );

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
     * @param indexInList Index of the element in the list; For layering purposes
     * @param csbeHeight Height of the CustomizableSignBlockEntity
     * @param matrices MatrixStack
     * @param vertexConsumers VertexConsumerProvider
     * @param light Light level
     * @param overlay Overlay level
     * @param facing Direction the element should face
     */
    @Override
    public void renderMinecraft(
            int indexInList,
            int csbeHeight,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light, int overlay,
            Direction facing
    ) {

        float w = this.calcBlocks(getWidth());
        float h = this.calcBlocks(getHeight());
        float x = this.calcBlocks(getX());
        float y = this.calcBlocks(getY());
        float rotation = this.getRotation();
        float[] color = this.getColor();

        float zOffset = CustomizableSignBlockEntityRenderer.zOffsetRenderLayer + (indexInList + 1) * CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer;
        BlockPosFloat shiftForward = new BlockPosFloat(0, 0, 0).offset(facing, ClientElementInterface.zOffset + ((indexInList + 1) * 0.00001f));
        BlockPosFloat renderPos = new BlockPosFloat(0, y * (-1), 0).offset(CustomizableSignBlockEntity.getRightSideDirection(facing.getOpposite()), x);

        matrices.push();

        matrices.translate(0, csbeHeight - h, 0); // Render element on top left corner (Default Position)
        matrices.translate(shiftForward.x, shiftForward.y, shiftForward.z); // Shift element forward depending on layer position to prevent z-fighting
        matrices.translate(renderPos.x, renderPos.y, renderPos.z); // Shift element to the right position

        // Bind texture to vertices
        Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, this.getResourcePath());

        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffset, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT, texture); // Custom Render Layer to prevent z-fighting
        RenderLayer renderLayer = imageLayering.buildRenderLayer();

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
    public void rotateTexture(float angle, ImVec2 windowPos, ImVec2 center){
        // For efficiency
        if (angle == 0) return;

        float radians = (float) Math.toRadians(angle);

        p0 = rotatePivot(p0, center, radians);
        p1 = rotatePivot(p1, center, radians);
        p2 = rotatePivot(p2, center, radians);
        p3 = rotatePivot(p3, center, radians);
    }

    public Texture getTexture() {
        return elementTexture;
    }

    public void loadTexture() {
        if (resourcePath == null || resourcePath.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.debug("Error (Loading texture on ImageElement): Couldn't load texture because resource path is empty!");
            return;
        }

        elementTexture = Textures.smartRegisterTexture(resourcePath);
        textureLoaded = true;
    }

    public void setCustomTexture(Texture texture) {
        this.elementTexture = texture;
    }

    // Always call after loadTexture() was called!

    public void sizeAuto() {
        if (elementTexture.isEmpty()) {
            System.err.println("Error (Loading ImageElement size): Couldn't determine size because texture hasn't been initialized! Initialize with ImageElement.loadTexture()!");
            return;
        }

        float w = elementTexture.getWidth();
        float h = elementTexture.getHeight();

        if (w == -1) {
            System.err.println("Error (Loading ImageElement size): Couldn't determine width because width in Texture class is -1. Possible cause: No texture ID has been associated with that resource path. Make sure that the texture has been registered!");
            return;
        }

        if (h == -1) {
            System.err.println("Error (Loading ImageElement size): Couldn't determine height because height in Texture class is -1. Possible cause: No texture ID has been associated with that resource path. Make sure that the texture has been registered!");
            return;
        }

        setHeight(h);
        setWidth(w);
    }

    @Override
    public void onPaste() {
    }

    @Override
    public void onImport() {
    }

    @Override
    public ClientElementInterface copy() {
        ImageElementClient copy = new ImageElementClient(
                x, y,
                width, height,
                factor,
                rotation,
                resourcePath,
                null,
                parentId
        );

        copy.setName(this.getName());
        copy.setColor(this.getColor());

        return copy;
    }
}
