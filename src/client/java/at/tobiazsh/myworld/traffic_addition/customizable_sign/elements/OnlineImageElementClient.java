package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Textures;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.elements.OnlineImageElement;
import at.tobiazsh.myworld.traffic_addition.utils.custom_image.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.utils.custom_image.OnlineImageLogic;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OnlineImageElementClient extends OnlineImageElement implements ClientElementInterface, TexturableElementInterface {

    public boolean textureLoaded = false;
    private boolean shouldRegisterTexture = false;

    private final CompletableFuture<byte[]> imageFuture = new CompletableFuture<>();

    private static final String defaultResourcePath = "assets/myworld_traffic_addition/textures/imgui/icons/not_found_placeholder.png";
    private static final Texture defaultTexture = Textures.smartRegisterTexture(defaultResourcePath);

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

        loadTexture(); // Ensure texture is loaded

        if (textureLoaded) {
            toImageElementCL().renderImGui(scale);
            return; // Texture is loaded, render normally
        }

        if (mayDownload) {
            requestImageDownload();
        }

        if (getResourcePath() == null || getResourcePath().isEmpty()) {
            MyWorldTrafficAddition.LOGGER.debug("No resource path set for OnlineImageElementClient with ID {}! Probably the image hasn't finished downloading yet but it could be caused by a different issue! Not rendering imgui!", getId());
            return; // No resource path set, nothing to render
        }
    }

    @Override
    public void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

        loadTexture(); // Ensure texture is loaded

        if (textureLoaded) {
            toImageElementCL().renderMinecraft(indexInList, csbeHeight, matrices, vertexConsumers, light, overlay, facing);
            return; // Texture is loaded, render normally
        }

        if (mayDownload) {
            requestImageDownload();
        }

        if (getResourcePath() == null || getResourcePath().isEmpty()) {
            MyWorldTrafficAddition.LOGGER.debug("No resource path set for OnlineImageElementClient with ID {}! Probably the image hasn't finished downloading yet but it could be caused by a different issue! Not rendering Minecarft!", getId());
            return; // No resource path set, nothing to render
        }
    }

    public ImageElementClient toImageElementCL() {
        return new ImageElementClient(
                getX(), getY(),
                getWidth(), getHeight(),
                getFactor(),
                getRotation(),
                elementTexture,
                getParentId()
        );
    }

    // TEXTURES

    @Override // TexturableElementInterface
    public boolean isTextureLoaded() {
        return textureLoaded;
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
        if (shouldRegisterTexture) {
            elementTexture = Textures.smartRegisterTexture(resourcePath);
            textureLoaded = true;
            shouldRegisterTexture = false;
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
            elementTexture = Textures.smartRegisterTexture(resourcePath); // Update the texture reference
            textureLoaded = true;
            return;
        }

        OnlineImageLogic.fetchImage(imageFuture, getPictureReference())
            .thenAccept(image -> {
                if (image != null && image.length > 0) {
                    Path path = OnlineImageCache.cacheImage(image, getPictureReference().toString() + ".png");
                    resourcePath = path.toString();
                    shouldRegisterTexture = true;
                    textureLoaded = true;
                    MyWorldTrafficAddition.LOGGER.info("Image downloaded successfully for OnlineImageElementClient with ID: {}", getId());
                } else {
                    resourcePath = defaultResourcePath;
                    elementTexture = defaultTexture; // Update the texture reference
                    MyWorldTrafficAddition.LOGGER.error("Failed to download image for OnlineImageElementClient with ID: {}", getId());
                }
        })
            .exceptionally(e -> {
                resourcePath = defaultResourcePath;
                elementTexture = defaultTexture; // Update the texture reference
                MyWorldTrafficAddition.LOGGER.error("Exception while downloading image for OnlineImageElementClient with ID: {}", getId(), e);
                return null;
        });
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
}
