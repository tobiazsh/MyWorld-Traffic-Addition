package at.tobiazsh.myworld.traffic_addition.utils.graphics;

import at.tobiazsh.myworld.traffic_addition.mixin.client.TextureManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Objects;

public class DynamicTexture extends AbstractTexture {

    // private
    private int subscribers;
    private boolean deleteWhenPossible;

    // private final
    private final Identifier id;
    private final boolean isResource;
    private final String path;

    public DynamicTexture(int subscribers, boolean deleteWhenPossible, Identifier id, boolean isResource, String path) {
        this.subscribers = subscribers;
        this.deleteWhenPossible = deleteWhenPossible;
        this.id = id;
        this.isResource = isResource;
        this.path = path;
    }

    /**
     * Creates a DynamicTexture instance.
     * @param path the path to the image file (either resource path or absolute file path)
     * @param id the Identifier to register the texture with in the TextureManager
     * @param isResource whether the path is a resource path (inside the mod's assets) or an absolute file path
     */
    public DynamicTexture(String path, Identifier id, boolean isResource) {
        this(0, false, id, isResource, path);
    }

    /**
     * Registers the texture in the TextureManager. Throws RuntimeException if the image could not be loaded. Note, this does NOT add the texture to the DynamicTextureManager! Use {@link #register()} for that.
     */
    public DynamicTexture registerTexture(boolean blur, boolean clamp) {
        this.close();

        try (NativeImage image = this.getImage(isResource)){
            if (image == null)
                throw new RuntimeException("Could not register texture in TextureManager from DynamicTexture, image is null!");

            this.load(image, blur, clamp);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, this);
        } catch (IOException e) {
            throw new RuntimeException("Could not register texture in TextureManager from DynamicTexture!", e);
        }

        return this;
    }

    /**
     * Registers the texture in the TextureManager only if it isn't already registered there or in the DynamicTextureManager. Note, this does NOT add the texture to the DynamicTextureManager! Use {@link #register()} for that.
     * @param blur whether to use blur filtering (or linear filtering)
     * @param clamp whether to use clamp wrapping
     * @return this DynamicTexture instance
     */
    public DynamicTexture smartRegisterTexture(boolean blur, boolean clamp) {
        if (((TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager()).getTextures().containsKey(id)) // Already registered in TextureManager
            return this;

        if (DynamicTextureManager.hasTexture(id)) // Already registered in DynamicTextureManager
            return this;

        return this.registerTexture(blur, clamp).register();
    }

    /**
     * Registers the texture in the DynamicTextureManager. If a texture with the same id already exists, it won't be replaced. Note, this does NOT register the texture in Minecraft's TextureManager!
     * @return this DynamicTexture instance
     */
    public DynamicTexture register() {
        DynamicTextureManager.addTexture(this.id, this);
        return this;
    }

    /**
     * Replaces the texture in the DynamicTextureManager. If a texture with the same id doesn't exist yet, it won't be added. Note, this does NOT register the texture in Minecraft's TextureManager!
     * @return this DynamicTexture instance
     */
    public DynamicTexture replace() {
        DynamicTextureManager.replaceTexture(this.id, this);
        return this;
    }

    /**
     * Unregisters the texture from the DynamicTextureManager. Note, this does NOT unregister the texture from Minecraft's TextureManager!
     */
    public void unregister() {
        DynamicTextureManager.removeTexture(this.id);
    }

    /**
     * Subscribes to this texture (so it won't get deleted while in use).
     */
    public DynamicTexture subscribe() {
        subscribers++;
        return this;
    }

    /**
     * Unsubscribes from this texture (so it can get deleted when not in use anymore).
     */
    public DynamicTexture unsubscribe() {
        subscribers--;
        if (subscribers < 0) subscribers = 0; // Safety guards

        if (subscribers == 0 && deleteWhenPossible)
            destroy();

        return this;
    }

    // Get number of subscribers
    public int getSubscribers() {
        return subscribers;
    }

    // Information on whether the texture should be deleted when no subscribers are left
    public boolean isDeleteWhenPossible() {
        return deleteWhenPossible;
    }

    /**
     * Unregisters the texture from the TextureManager when no subscribers are left and deletes from GPU (hopefully, if MC does that - yes it does, just checked lmao).
     * If subscribers are still present, marks the texture for deletion when possible (so when the last subscriber unsubscribes).
     */
    public void destroy() {
        if (subscribers > 0) {
            deleteWhenPossible = true;
            return;
        }

        this.unregister();
        this.close();
    }

    public void dontDestroyWhenPossible() {
        deleteWhenPossible = false;
    }

    public Identifier getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    /**
     * Creates the texture (glTexture) from the specified NativeImage.
     * @param image the NativeImage to load the texture from
     * @param blur whether to use blur filtering (or linear filtering)
     * @param clamp whether to use clamp wrapping
     */
    private void load(NativeImage image, boolean blur, boolean clamp) {
        GpuDevice gpu = RenderSystem.getDevice();
        this.close();
        Objects.requireNonNull(this.id);
        this.glTexture = gpu.createTexture(this.id.toString(), 5, TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1, 1);
        this.glTextureView = gpu.createTextureView(this.glTexture);
        this.setFilter(blur, false);
        this.setClamp(clamp);
        gpu.createCommandEncoder().writeToTexture(this.glTexture, image);
    }


    /**
     * Reads the image from the specified path and loads it as a NativeImage.
     * @param isResource whether the path is a resource path (inside the mod's assets) or an absolute file path
     * @return the NativeImage loaded from the specified path or null if the image file is null
     * @throws IOException if an I/O error occurs while reading the image
     */
    private NativeImage getImage(boolean isResource) throws IOException {
        byte[] imageBytes;
        FileSystem.File imageFile = new FileSystem.File(path, isResource).evaluateFileType();
        if (imageFile == null) return null;

        return NativeImage.read(imageFile.readBytes());
    }
}
