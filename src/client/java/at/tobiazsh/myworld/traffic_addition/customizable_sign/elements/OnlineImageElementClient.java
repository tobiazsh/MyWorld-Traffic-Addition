package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import at.tobiazsh.myworld.traffic_addition.texture.*;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.sign.elements.OnlineImageElement;
import at.tobiazsh.myworld.traffic_addition.cache.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.network.OnlineImageNetworking;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import com.mojang.math.Axis;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.ImUtil.rotatePivot;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

@SuppressWarnings("DuplicatedCode")
public class OnlineImageElementClient extends OnlineImageElement implements ClientElementInterface {

    private static AtomicBoolean loadingLoadFailed = new AtomicBoolean(false);
    private static AtomicBoolean notFoundLoadFailed = new AtomicBoolean(false);

    private static @Nullable HybridTexture loadingTexture;
    private static @Nullable HybridTexture notFoundTexture;

    private static @Nullable HybridTexture hybridTexture;

    public CompletableFuture<byte[]> imageFuture;

    private final AtomicBoolean shouldAutoSize = new AtomicBoolean(false);

    private final AtomicBoolean mayDownload = new AtomicBoolean(true); // Flag to control if the image should be downloaded

    private ImVec2 p0, p1, p2, p3;

    public OnlineImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            UUID pictureReference,
            ElementId id, ParentElementId parentId
    ) {
        super(x, y, width, height, factor, rotation, pictureReference, id, parentId);
    }

    public OnlineImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            UUID pictureReference,
            ParentElementId parentId
    ) {
        super(x, y, width, height, factor, rotation, pictureReference, parentId);
    }

    /**
     * Renders the image element in an ImGui Context.
     */
    public void renderImGui(float scale) {
        initiateRender();

        HybridTexture texture = getRenderedTexture();

        if (texture == null)
            return; // If ABSOLUTELY no texture is available, don't render anything; should never happen, but just in case

        ImDrawList drawList = ImGui.getWindowDrawList();

        ImVec2 windowPos = new ImVec2(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY());
        float[] color = this.getColor();

        // Calculate points and account for scale
        var p0 = new ImVec2(windowPos.x + x * scale, windowPos.y + y * scale); // Top Left Vertices
        var p1 = new ImVec2(windowPos.x + (x + width) * scale, windowPos.y + y * scale); // Top Right Vertices
        var p2 = new ImVec2(windowPos.x + (x + width) * scale, windowPos.y + (y + height) * scale); // Bottom Right Vertices
        var p3 = new ImVec2(windowPos.x + x * scale, windowPos.y + (y + height) * scale); // Bottom Left Vertices

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
                ImGui.colorConvertFloat4ToU32(color[0], color[1], color[2], color[3]) // Color doesn't work yet
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
        initiateRender();

        HybridTexture texture = getRenderedTexture();

        if (texture == null)
            return; // If ABSOLUTELY no texture is available, don't render anything; should never happen, but just in case

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

        // Bind texture to vertices
        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(
                zOffset,
                CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT,
                texture.getIdentifier()
        ); // Custom Render Layer to prevent z-fighting

        RenderType renderLayer = imageLayering.buildRenderType();

        poseStack.pushPose();
        Matrix4f positionMatrix = poseStack.last().pose();

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

        // Top left
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

    // TEXTURES

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

    private void loadStaticTextures() {
        if (loadingTexture == null && !loadingLoadFailed.get()) {
            try {
                ImGuiTexture imguiTexture = ImGuiTextureFactory.fromStream(
                        Objects.requireNonNull(getClass().getResourceAsStream("/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/loading_placeholder.png")),
                        MyWorldTrafficAddition.MOD_ID + ":OnlineImageLoadingTexture"
                );

                loadingTexture = HybridTexture.ofImGuiTexture(
                        imguiTexture,
                        Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "online_image_element.loading")
                );

                loadingTexture.pin();
                loadingTexture.register();
            } catch (IOException e) {
                loadingLoadFailed.set(true);
                throw new RuntimeException(e);
            }
        }

        if (notFoundTexture == null && !notFoundLoadFailed.get()) { // Not found acts as a fallback!
            try {
                ImGuiTexture imguiTexture = ImGuiTextureFactory.fromStream(
                        Objects.requireNonNull(getClass().getResourceAsStream("/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/not_found.png")),
                        MyWorldTrafficAddition.MOD_ID + ":OnlineImageNotFoundTexture"
                );

                notFoundTexture = HybridTexture.ofImGuiTexture(
                        imguiTexture,
                        Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "online_image_element.not_found")
                );

                notFoundTexture.pin();
                notFoundTexture.register();
            } catch (IOException e) {
                notFoundLoadFailed.set(true);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Method to retrieve next best texture for rendering.
     */
    private @Nullable HybridTexture getRenderedTexture() {
        if (!imageFuture.isDone())
            return loadingTexture; // If the image is still downloading, show loading texture

        if (imageFuture.isCompletedExceptionally())
            return notFoundTexture;

        if (hybridTexture != null && hybridTexture.isUsableMinecraft() && hybridTexture.isUsableImGui())
            return hybridTexture; // If the image is downloaded and usable, show it

        return notFoundTexture; // If the image is downloaded but not usable, show not found texture
    }

    // Sends request with
    //      1) the picture reference UUID
    //      2) request id
    //
    // Gets:
    //      1) one byte (1 = success, 0 = failure)
    //      2) the request id
    //      3) the image data as byte array
    private void requestImageDownload() {
        mayDownload.set(false); // Only allow one download request

        if (OnlineImageCache.isImageCached(this.getPictureReference().toString())) {
            Path path = OnlineImageCache.getCachedImagePath(getPictureReference().toString());
            loadTexture(path);
            return;
        }

        long downloadTimeout = MyWorldTrafficAddition.getServerPreferences().customizableSigns.onlineImages.downloadTimeout.getOrDefault();

        imageFuture = OnlineImageNetworking.fetchImage(imageFuture, getPictureReference())
                .orTimeout(downloadTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .whenComplete((image, throwable) -> {
                    if (throwable != null) {
                        hybridTexture = null;
                        MyWorldTrafficAddition.LOGGER.error("Image download failed for ID: {}", getId(), throwable);
                        return;
                    }

                    if (image == null || image.length == 0) {
                        hybridTexture = null;
                        MyWorldTrafficAddition.LOGGER.error("Failed to download image for OnlineImageElementClient with ID: {}", getId());
                    }

                    try {
                        Path path = OnlineImageCache.cacheImage(image, getPictureReference().toString());
                        loadTexture(path);

                        shouldAutoSize.set(shouldAutoSize.get()); // Trigger auto-size if requested
                        shouldAutoSize.set(false); // Reset flag
                    } catch (IOException e) {
                        hybridTexture = null;
                        MyWorldTrafficAddition.LOGGER.error("Failed to cache downloaded image for OnlineImageElementClient with ID: {}", getId(), e);
                    }
                })
                .exceptionally(_ -> new byte[0]); // Return empty byte array on exception
    }

    private void loadTexture(Path path) {
        try {
            ImGuiTexture imguiTexture = ImGuiTextureFactory.fromFile(path, MyWorldTrafficAddition.MOD_ID + ":OnlineImageElement_" + getPictureReference().toString());

            hybridTexture = HybridTexture.ofImGuiTexture(
                    imguiTexture,
                    Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "online_image_element." + getPictureReference().toString())
            );

            hybridTexture.register();

            if (shouldAutoSize.get()) {
                setSizeToTexture();
                shouldAutoSize.set(false);
            }
        } catch (IOException e) {
            hybridTexture = null;
            MyWorldTrafficAddition.LOGGER.error("Failed to load texture from path for OnlineImageElementClient with ID: {}", getId(), e);
        }
    }

    /**
     * Helper method to initiate rendering, handling texture loading and download requests.
     */
    private void initiateRender() {
        loadStaticTextures();

        if (mayDownload.get())
            requestImageDownload();
    }

    @Override
    public void onPaste() {
    }

    @Override
    public void onImport() {
    }

    @Override
    public ClientElementInterface copy() {
        OnlineImageElementClient copy = new OnlineImageElementClient(
                x, y,
                width, height,
                factor,
                rotation,
                pictureReference,
                null,
                parentId
        );

        copy.setName(name);
        copy.setColor(color);

        return copy;
    }

    @Override
    public @Nullable ImGuiTexture getThumbnailTexture() {
        return null;
    }

    @Override
    public String getGenericTranslatableName() {
        return "Online Image Element";
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

    public void autoSizeAfterLoad() {
        shouldAutoSize.set(true);
    }
}
