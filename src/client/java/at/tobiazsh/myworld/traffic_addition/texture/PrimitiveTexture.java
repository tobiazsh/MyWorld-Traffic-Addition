package at.tobiazsh.myworld.traffic_addition.texture;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * A class consisting of a GPU texture view, GPU texture and a sampler, representing a primitive texture, extending
 * the {@link net.minecraft.client.renderer.texture.AbstractTexture} class. Can be registered in the
 * {@link net.minecraft.client.renderer.texture.TextureManager} and used in Minecraft rendering.
 */
public class PrimitiveTexture extends AbstractTexture {

    protected final Identifier id;
    protected boolean registered;

    public PrimitiveTexture(
            @Nullable GpuTexture gpuTexture,
            @Nullable GpuTextureView gpuTextureView,
            GpuSampler gpuSampler,
            Identifier id
    ) {
        super.texture = gpuTexture;
        super.textureView = gpuTextureView;
        super.sampler = gpuSampler;

        this.id = id;
    }

    /**
     * Creates a new {@link PrimitiveTexture} from an {@link ImGuiTexture}. Does not register the texture in Minecraft's
     * {@link net.minecraft.client.renderer.texture.TextureManager}. Use {@link #register()} to register the texture.
     *
     * @param imGuiTexture The ImGuiTexture to create the PrimitiveTexture from. Copies the references to the GPU
     *                     texture, GPU texture view and sampler from the ImGuiTexture.
     * @param id The id to register the texture with in the TextureManager.
     *
     * @return A new PrimitiveTexture instance.
     * @throws IllegalArgumentException if the {@link ImGuiTexture} is not an instance of {@link ImGuiTextureImpl}
     */
    public static @NonNull PrimitiveTexture ofImGuiTexture(
            @NonNull ImGuiTexture imGuiTexture,
            Identifier id
    ) throws IllegalArgumentException {
        if (!(imGuiTexture instanceof ImGuiTextureImpl impl))
            throw new IllegalArgumentException("ImGuiTexture must be an instance of ImGuiTextureImpl");

        return new PrimitiveTexture(
                impl.getGpuTexture(),
                impl.getGpuTextureView(),
                impl.getGpuSampler(),
                id
        );
    }

    /**
     * Registers the texture in Minecraft's {@link net.minecraft.client.renderer.texture.TextureManager} if it's not
     * already registered. If the texture is already registered, this method does nothing.
     */
    public void register() {
        if (this.registered)
            return;

        Minecraft.getInstance().getTextureManager().register(id, this);
        this.registered = true;
    }

    /**
     * Unregisters the texture from Minecraft's {@link net.minecraft.client.renderer.texture.TextureManager} if it's
     * registered. If the texture is not registered, this method does nothing.
     */
    public void unregister() {
        if (!this.registered)
            return;

        Minecraft.getInstance().getTextureManager().release(id);
        this.registered = false;
    }

    /**
     * Frees the GPU resources associated with this texture.
     * This method should be called when the texture is no longer needed to prevent memory leaks.
     *
     * <p>
     *     Do <b>NOT</b> call as part of {@link HybridTexture} because both the ImGuiTexture and the PrimitiveTexture
     *     share a reference to the same GPU resources, which will lead to a double free and crash the game.
     * </p>
     */
    public void dispose() {
        releaseTextures();
    }

    /**
     * Drops the references to the GPU resources associated with this texture without freeing them.
     * This method should be called when the texture is no longer needed, but the GPU resources are still managed
     * elsewhere (e.g., by an ImGuiTexture).
     */
    public void drop() {
        this.texture = null;
        this.textureView = null;
    }

    /**
     * Checks if the texture is usable, i.e., if it has valid GPU resources (texture and texture view).
     * Does not inform about whether the texture is registered in the TextureManager or not. Use {@link #isRegistered()}
     * for that.
     */
    public boolean isUsable() {
        return this.texture != null && this.textureView != null;
    }

    /**
     * Checks if the texture is registered in Minecraft's {@link net.minecraft.client.renderer.texture.TextureManager}.
     */
    public boolean isRegistered() {
        return registered;
    }
}
