package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.texture.CommonTextures;
import at.tobiazsh.myworld.traffic_addition.texture.DynamicTexture;
import at.tobiazsh.myworld.traffic_addition.texture.Texture;
import at.tobiazsh.myworld.traffic_addition.texture.Textures;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.sign.elements.OnlineImageElement;
import at.tobiazsh.myworld.traffic_addition.cache.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.network.OnlineImageNetworking;
import imgui.ImGui;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class OnlineImageElementClient extends OnlineImageElement implements ClientElementInterface, TexturableElementInterface {

    public AtomicBoolean textureLoaded = new AtomicBoolean(false);
    public AtomicBoolean isTexturePlaceholder = new AtomicBoolean(true);
    private boolean shouldLogNotRenderable = true;

    private final AtomicBoolean shouldRegisterTexture = new AtomicBoolean(false);
    private final CompletableFuture<byte[]> imageFuture = new CompletableFuture<>();

    DynamicTexture dynamicTexture = null;

    private boolean mayDownload = true; // Flag to control if the image should be downloaded

    public OnlineImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            UUID pictureReference,
            UUID id, UUID parentId
    ) {
        super(x, y, width, height, factor, rotation, pictureReference, parentId, id);
    }

    public OnlineImageElementClient(
            float x, float y,
            float width, float height,
            float factor,
            float rotation,
            UUID pictureReference,
            UUID parentId
    ) {
        super(x, y, width, height, factor, rotation, pictureReference, parentId);
    }

    @Override
    public void renderImGui(float scale) {
        initiateRender(() -> toImageElementCL(isTexturePlaceholder.get()).renderImGui(scale));
    }

    @Override
    public void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        initiateRender(() -> toImageElementCL(isTexturePlaceholder.get()).renderMinecraft(indexInList, csbeHeight, matrices, vertexConsumers, light, overlay, facing));
    }

    public ImageElementClient toImageElementCL(boolean isPlaceholder) {
        return new ImageElementClient(
                getX(), getY(),
                getWidth(), getHeight(),
                getFactor(),
                getRotation(),
                elementTexture,
                getParentId()
        ).fromOnlineImage(this, isPlaceholder);
    }

    // TEXTURES


    @Override
    public DynamicTexture getDynamicTexture() {
        return dynamicTexture;
    }

    @Override // TexturableElementInterface
    public boolean isTextureLoaded() {
        return textureLoaded.get();
    }

    @Override // TexturableElementInterface
    public void setTexture(Texture texture) {
        this.elementTexture = texture;
    }

    @Override
    public Texture getTexture() {
        return this.elementTexture;
    }

    @Override
    public void loadTexture() {
        if (shouldRegisterTexture.get()) {
            elementTexture = Textures.smartRegisterTexture(resourcePath);
            textureLoaded.set(true);
            shouldRegisterTexture.set(false);
        }
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
        mayDownload = false; // Only allow one download request

        if (OnlineImageCache.isImageCached(this.getPictureReference() + ".png")) {
            resourcePath = OnlineImageCache.getCachedImagePath(getPictureReference().toString() + ".png").toString();
            shouldRegisterTexture.set(true);
            return;
        }

        resourcePath = CommonTextures.LOADING_PLACEHOLDER.getResourcePath();
        elementTexture = CommonTextures.LOADING_PLACEHOLDER; // Set to loading placeholder while downloading
        textureLoaded.set(true); // Mark placeholder as available so render paths display it

        OnlineImageNetworking.fetchImage(imageFuture, getPictureReference())
            .thenAccept(image -> {
                if (image != null && image.length > 0) {
                    Path path = OnlineImageCache.cacheImage(image, getPictureReference().toString() + ".png");
                    resourcePath = path.toString();
                    textureLoaded.set(false); // Mark as not loaded to trigger loading
                    shouldRegisterTexture.set(true); // Trigger registration on next loadTexture call
                    isTexturePlaceholder.set(false);
                    MyWorldTrafficAddition.LOGGER.info("Image downloaded successfully for OnlineImageElementClient with ID: {}", getId());
                } else {
                    resourcePath = CommonTextures.NOT_FOUND_PLACEHOLDER.getResourcePath();
                    elementTexture = CommonTextures.NOT_FOUND_PLACEHOLDER; // Update texture to not found placeholder
                    textureLoaded.set(true); // Mark placeholder as available so render paths display it
                    MyWorldTrafficAddition.LOGGER.error("Failed to download image for OnlineImageElementClient with ID: {}", getId());
                }
        })
            .exceptionally(e -> {
                resourcePath = CommonTextures.NOT_FOUND_PLACEHOLDER.getResourcePath();
                elementTexture = CommonTextures.NOT_FOUND_PLACEHOLDER; // Update texture to not found placeholder
                textureLoaded.set(true); // Mark placeholder as available so render paths display it
                MyWorldTrafficAddition.LOGGER.error("Exception while downloading image for OnlineImageElementClient with ID: {}", getId(), e);
                return null;
        });
    }

    @Override
    public void markTextureStale() {
        if (dynamicTexture == null) return;
        try {
            dynamicTexture.unsubscribe();
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.warn("Failed to unsubscribe dynamic texture", e);
        }
        dynamicTexture = null;
        textureLoaded.set(false); // Force reload
    }

    /**
     * Helper method to initiate rendering, handling texture loading and download requests.
     * @param onTextureLoaded Executed once the texture has been loaded successfully.
     * @return True if successful, false if not.
     */
    private boolean initiateRender(Runnable onTextureLoaded) {
        loadTexture(); // Ensure texture is loaded
        sizeAuto(); // Adjust size if set to -1

        if (textureLoaded.get()) {
            onTextureLoaded.run();
            return true; // Texture is loaded, render normally
        }

        if (mayDownload) {
            requestImageDownload();
        }

        if (getResourcePath() == null || getResourcePath().isEmpty()) {
            if (shouldLogNotRenderable) {
                MyWorldTrafficAddition.LOGGER.debug("OnlineImageElementClient with ID: {} is not renderable yet (no resource path)", getId());
                shouldLogNotRenderable = false; // Only log once
            }
            return false; // No resource path set, nothing to render
        }

        return true;
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

    private void sizeAuto() {
        if (width == -1) {
            if (elementTexture != null && !elementTexture.isEmpty()) {
                width = elementTexture.getWidth();
            }
        }

        if (height == -1) {
            if (elementTexture != null && !elementTexture.isEmpty()) {
                height = elementTexture.getHeight();
            }
        }
    }

    @Override
    public void renderPreview(float w, float h) {
        int textureId;

        if (textureLoaded.get()) {
            if (!this.getTexture().isEmpty()) {
                textureId = this.getTexture().getTextureId();
            } else if (imageFuture.isCompletedExceptionally() || imageFuture.isCancelled()) {
                textureId = CommonTextures.NOT_FOUND_PLACEHOLDER.getTextureId();
            } else {
                textureId = CommonTextures.LOADING_PLACEHOLDER.getTextureId();
            }
        } else {
            textureId = CommonTextures.LOADING_PLACEHOLDER.getTextureId();
        }

        float startX = ImGui.getCursorScreenPosX();
        float startY = ImGui.getCursorScreenPosY();

        ImGui.image(textureId, w, h);

        float iconSize = Math.min(32f, Math.min(w / 2f, h / 2f));
        float padding = 4f;

        float iconX = startX + w - iconSize - padding;
        float iconY = startY + h - iconSize - padding;

        ImGui.setCursorScreenPos(iconX, iconY); // Set Position to bottom-right corner
        ImGui.image(CommonTextures.INTERNET_GLOBE.getTextureId(), iconSize, iconSize); // Draw internet globe icon

        ImGui.setCursorScreenPos(startX, startY); // Reset cursor position
    }
}
