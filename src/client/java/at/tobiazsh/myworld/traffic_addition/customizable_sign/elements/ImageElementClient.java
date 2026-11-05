package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import at.tobiazsh.myworld.traffic_addition.texture.*;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.crypto.Encoding;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import at.tobiazsh.myworld.traffic_addition.sign.elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.ImUtil.rotatePivot;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

@SuppressWarnings("DuplicatedCode")
public class ImageElementClient extends ImageElement implements ClientElementInterface {

    private @Nullable HybridTexture hybridTexture = null;
    private final AtomicBoolean textureLoadFailed = new AtomicBoolean(false);

    private static @Nullable HybridTexture fallbackHybridTexture = null;
    private static final AtomicBoolean fallbackLoadingFailed = new AtomicBoolean(false);

    private final AtomicBoolean autoSize = new AtomicBoolean(false);

    private static final int DEFAULT_WIDTH = 64;
    private static final int DEFAULT_HEIGHT = 64;

    public ImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            String path,
            ParentElementId parentId
    ) {
        super(x, y, width, height, factor, rotation, path, parentId);
    }

    public ImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            String path,
            ElementId id,
            ParentElementId parentId
    ) {
        super(x, y, width, height, factor, rotation, path, id, parentId);
    }

    public ImageElementClient(
            float x, float y,
            float factor,
            String path,
            ParentElementId parentId
    ) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, factor, 0, path, parentId);
        autoSize.set(true);
        // TODO: Maybe make an option to center the image after auto sizing? Currently it will be top left corner. Check with ElementAddWindow.
    }

    private ImVec2 p0, p1, p2, p3;

    /**
     * Renders the image element in an ImGui Context.
     */
    @Override
    public void renderImGui(float scale) {
        ensureFallbackLoaded();

        if (fallbackHybridTexture == null || !fallbackHybridTexture.isUsableImGui())
            return;

        ensureTextureLoaded();

        ImDrawList drawList = ImGui.getWindowDrawList();

        ImVec2 windowPos = new ImVec2(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY());
        float[] color = this.getColor();

        // Calculate points and account for scale
        p0 = new ImVec2(windowPos.x + x * scale, windowPos.y + y * scale); // Top Left Vertices
        p1 = new ImVec2(windowPos.x + (x + width) * scale, windowPos.y + y * scale); // Top Right Vertices
        p2 = new ImVec2(windowPos.x + (x + width) * scale, windowPos.y + (y + height) * scale); // Bottom Right Vertices
        p3 = new ImVec2(windowPos.x + x * scale, windowPos.y + (y + height) * scale); // Bottom Left Vertices

        rotateTexture(
                rotation,
                new ImVec2(
                        windowPos.x + (this.x + this.width / 2) * scale,
                        windowPos.y + (this.y + this.height / 2) * scale
                )
        );

        float uv0X = 0.0f, uv0Y = 0.0f;
        float uv1X = 1.0f, uv1Y = 0.0f;
        float uv2X = 1.0f, uv2Y = 1.0f;
        float uv3X = 0.0f, uv3Y = 1.0f;

        var texture = hybridTexture != null && hybridTexture.isUsableImGui() ? hybridTexture : fallbackHybridTexture;

        texture.touch();

        drawList.addImageQuad(
                texture.getImGuiTexture().getTextureId(),
                p0.x, p0.y,
                p1.x, p1.y,
                p2.x, p2.y,
                p3.x, p3.y,
                uv0X, uv0Y,
                uv1X, uv1Y,
                uv2X, uv2Y,
                uv3X, uv3Y,
                ImGui.colorConvertFloat4ToU32(color[0], color[1], color[2], color[3])
        );
    }

    /**
     * Renders an ImageElement in Minecraft
     * @param indexInList Index of the element in the list; For layering purposes
     * @param csbeHeight Height of the CustomizableSignBlockEntity
     * @param poseStack PoseStack
     * @param light Light level
     * @param facing Direction the element should face
     */
    @SuppressWarnings("ExtractMethodRecommender")
    @Override
    public void renderMinecraft(
            @NonNull SubmitNodeCollector queue,
            int indexInList,
            int csbeHeight,
            PoseStack poseStack,
            int light,
            Direction facing
    ) {
        ensureFallbackLoaded();

        if (fallbackHybridTexture == null || !fallbackHybridTexture.isUsableMinecraft())
            return;

        ensureTextureLoaded();

        int overlay = OverlayTexture.NO_OVERLAY;

        float w = this.calcBlocks(getWidth());
        float h = this.calcBlocks(getHeight());
        float x = this.calcBlocks(getX());
        float y = this.calcBlocks(getY());
        float rotation = this.getRotation();
        float[] color = this.getColor();

        float viewDistance = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns.viewDistance.getOrDefault();
        float elementDistancing = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns.elementDistancing.getOrDefault();
        float zOffset = viewDistance + (indexInList + 1) * elementDistancing;
        BlockPosFloat shiftForward = new BlockPosFloat(0, 0, 0).offset(facing, ClientElementInterface.zOffset + ((indexInList + 1) * 0.00001f));
        BlockPosFloat renderPos = new BlockPosFloat(0, y * (-1), 0).offset(getRightSideDirection(facing.getOpposite()), x);

        var texture = this.hybridTexture != null && this.hybridTexture.isUsableMinecraft() ?
                this.hybridTexture :
                fallbackHybridTexture;

        texture.touch();

        // Bind texture to vertices
        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(
                zOffset,
                CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT,
                texture.getIdentifier()
        ); // Custom Render Layer to prevent z-fighting

        RenderType renderLayer = imageLayering.buildRenderType();

        poseStack.pushPose();

        poseStack.translate(0, csbeHeight - h, 0); // Render element on top left corner (Default Position)
        poseStack.translate(shiftForward.x, shiftForward.y, shiftForward.z); // Shift element forward depending on layer position to prevent z-fighting
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z); // Shift element to the right position

        // Rotate to the same direction as the block (opposite because the block is facing a certain direction but the canvas is on the opposite)
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
        poseStack.translate(-0.5, -0.5, -0.5);

        // Rotate around the element's center
        poseStack.translate(w / 2, h / 2, 0); // Move origin to element center
        poseStack.mulPose(Axis.ZN.rotationDegrees(rotation));
        poseStack.translate(-w / 2, -h / 2, 0); // Move origin back

        Matrix4f positionMatrix = poseStack.last().pose();

        queue.submitCustomGeometry(poseStack, renderLayer, (_, vertexConsumer) -> {
            vertexConsumer.addVertex(positionMatrix, 0 , 0, 0)
                    .setUv(0, 1)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setLight(light)
                    .setOverlay(overlay)
                    .setNormal(0, 0, 1);

            // Top right
            vertexConsumer.addVertex(positionMatrix, w, 0, 0)
                    .setUv(1, 1)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setLight(light)
                    .setOverlay(overlay)
                    .setNormal(0, 0, 1);

            // Bottom right
            vertexConsumer.addVertex(positionMatrix, w, h, 0)
                    .setUv(1, 0)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setLight(light)
                    .setOverlay(overlay)
                    .setNormal(0, 0, 1);

            // Bottom left
            vertexConsumer.addVertex(positionMatrix, 0, h, 0)
                    .setUv(0, 0)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setLight(light)
                    .setOverlay(overlay)
                    .setNormal(0, 0, 1);
        });

        poseStack.popPose();
    }

    /**
     * Rotates the dynTexture by a given angle
     * @param angle The angle to rotate by
     * @param center The center point to rotate around
     */
    public void rotateTexture(float angle, ImVec2 center){
        // For efficiency
        if (angle == 0) return;

        float radians = (float) Math.toRadians(angle);

        p0 = rotatePivot(p0, center, radians);
        p1 = rotatePivot(p1, center, radians);
        p2 = rotatePivot(p2, center, radians);
        p3 = rotatePivot(p3, center, radians);
    }

    private static void ensureFallbackLoaded() {
        if (fallbackHybridTexture != null || !fallbackLoadingFailed.get())
            return;

        try {
            ImGuiTexture imguiFallbackTexture = ImGuiTextureFactory.fromStream(
                    Objects.requireNonNull(MyWorldTrafficAddition.class.getResourceAsStream("/assets/myworld_traffic_addition/textures/imgui/element_icon.png")),
                    MyWorldTrafficAddition.MOD_ID + ":ImageFallbackTexture"
            );

            fallbackHybridTexture = HybridTexture.ofImGuiTexture(
                    imguiFallbackTexture,
                    Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "image_element.fallback")
            );

            fallbackHybridTexture.pin(); // Don't let the fallback texture be swept away by the HybridTextureSweeper
            fallbackHybridTexture.register();
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to load fallback texture for ImageElement!", e);
            fallbackLoadingFailed.set(true);
        }
    }

    /**
     * Loads the texture for the image element. If the texture fails to load, it will not be attempted in the future.
     */
    private void ensureTextureLoaded() {
        if (textureLoadFailed.get()) return;

        if (
                this.hybridTexture != null &&
                this.hybridTexture.isUsableMinecraft() &&
                this.hybridTexture.isUsableImGui()
        ) {
            return;
        }

        Identifier key = Identifier.fromNamespaceAndPath(
                MyWorldTrafficAddition.MOD_ID,
                Encoding.encodeBase32(resourcePath)
        );

        Optional<HybridTexture> loadedTexture = HybridTexture.tryGetFromManager(key);

        if (loadedTexture.isPresent()) {
            this.hybridTexture = loadedTexture.get();
            return;
        }

        try {
            ImGuiTexture imGuiTexture = ImGuiTextureFactory.fromStream(
                    Objects.requireNonNull(MyWorldTrafficAddition.class.getResourceAsStream(resourcePath)),
                    "Image Element Texture " + key
            );

            this.hybridTexture = HybridTexture.ofImGuiTexture(imGuiTexture, key);
            this.hybridTexture.register();
            this.hybridTexture.pin();
            /* ↑ Pin the texture ↑ because the same texture is shared across ImageElements */
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to load fallback texture for ImageElement!", e);
            textureLoadFailed.set(true);
            return;
        }

        if (autoSize.get())
            setSizeToTexture();
    }

    /**
     * Sets the size of the image element to the size of the texture. If the texture is not loaded, it will print an
     * error message.
     */
    public void setSizeToTexture() {
        if (hybridTexture == null) {
            MyWorldTrafficAddition.LOGGER.warn("Error autosizing image element: Texture has not been loaded yet!");
            return;
        }

        float width = hybridTexture.getTextureWidth(0);
        float height = hybridTexture.getTextureHeight(0);

        setSize(width, height);
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

    @Override
    public @Nullable ImGuiTexture getThumbnailTexture() {
        return this.hybridTexture != null ? this.hybridTexture.getImGuiTexture() : null;
    }

    @Override
    public String getGenericTranslatableName() {
        return "Image Element";
    }
}
