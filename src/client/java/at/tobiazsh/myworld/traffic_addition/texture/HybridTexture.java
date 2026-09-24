package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.mixin.client.TextureManagerAccessor;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that represents a texture, which works both in Minecraft and in ImGui.
 *
 * <p>Extends {@link PrimitiveTexture} and holds a reference to an {@link ImGuiTexture}.
 *
 * <p>Provides methods to register and unregister the texture in Minecraft's
 * {@link net.minecraft.client.renderer.texture.TextureManager} and to check if the texture is usable in both Minecraft and ImGui.
 *
 * <p>The {@link ImGuiTexture} is always the manager of the GPU resources, so disposing the {@link ImGuiTexture} will
 * also dispose the GPU resources, making the {@link HybridTexture} unusable. Therefore, it is recommended to use the
 * {@link #dispose()} method to dispose of the texture, which will unregister it from Minecraft and dispose the
 * {@link ImGuiTexture}.
 *
 * <p>This texture is being swept every tick. If you want to prolong lifetime until the program is closed, pin your
 * texture using {@link #pin()}. For the sweeping logic, see {@link HybridTextureSweeper}</p>
 */
public class HybridTexture extends PrimitiveTexture {

    private final @NonNull ImGuiTexture imGuiTexture;
    private final @NonNull Identifier id;

    private boolean pinned = false;

    private AtomicLong lastUsedTick = new AtomicLong(0);

    public HybridTexture(
            @NonNull ImGuiTextureImpl imGuiTexture,
            Identifier id
    ) {
        super(
                imGuiTexture.getGpuTexture(),
                imGuiTexture.getGpuTextureView(),
                imGuiTexture.getGpuSampler(),
                id
        );

        this.id = id;
        this.imGuiTexture = imGuiTexture;
    }

    /**
     * Creates a new {@link HybridTexture} from an {@link ImGuiTexture}. Registers the texture in Minecraft's
     * {@link net.minecraft.client.renderer.texture.TextureManager} if the {@link ImGuiTexture} is not null.
     * íf the {@link ImGuiTexture} is null, returns a {@link HybridTexture} with both fields set to null.
     *
     * @param imGuiTexture The ImGuiTexture to create the HybridTexture from.
     *                     Copies the references to the GPU texture, GPU texture view and sampler from the ImGuiTexture.
     * @param id The id to register the texture with in the TextureManager.
     * @return A new HybridTexture instance.
     *
     * @throws IllegalArgumentException if the {@link ImGuiTexture} is not an instance of
     * {@link dev.tobiazsh.imguib3d.client.texture.ImGuiTextureImpl}
     */
    public static @NonNull HybridTexture ofImGuiTexture(
            @NonNull ImGuiTexture imGuiTexture,
            Identifier id
    ) {
        if (!(imGuiTexture instanceof ImGuiTextureImpl impl))
            throw new IllegalArgumentException("ImGuiTexture must be an instance of ImGuiTextureImpl");

        return new HybridTexture(impl, id);
    }


    /**
     * Tries to get the {@link HybridTexture} from Minecraft's
     * {@link net.minecraft.client.renderer.texture.TextureManager} by its id.
     * If the texture is not found, returns an empty {@link Optional}.
     *
     * @param id The id of the texture to get from the TextureManager.
     * @return An {@link Optional} containing the {@link HybridTexture} if found, or an empty {@link Optional} if not
     * found.
     */
    public static Optional<HybridTexture> tryGetFromManager(Identifier id) {
        @Nullable AbstractTexture texture = ((TextureManagerAccessor) Minecraft.getInstance()).getByPath().get(id);

        if (texture == null)
            return Optional.empty();

        if (!(texture instanceof HybridTexture hybridTexture))
            return Optional.empty();

        return Optional.of(hybridTexture);
    }

    public @NonNull ImGuiTexture getImGuiTexture() {
        return imGuiTexture;
    }

    public @NonNull Identifier getIdentifier() {
        return id;
    }

    public long getLastUsedTick() {
        return lastUsedTick.get();
    }

    /**
     * Disposes the texture, unregistering it from Minecraft and disposing the ImGuiTexture.
     * After calling this method, the texture should not be used anymore.
     *
     * <p>It is generally not recommended to use this method unless you know what you are doing. If you don't,
     * please consider using {@link #disposeSafe()}</p>
     */
    public void dispose() {
        unregister(); // First unregister, so it's no longer used in Minecraft rendering
        drop(); // Then drop the references

        imGuiTexture.dispose(); // Then dispose the ImGuiTexture, which will finally dispose off the GPU resources
    }

    /**
     * Only disposes unpinned textures. If the texture is pinned, it will not be disposed of.
     */
    public void disposeSafe() {
        if (isPinned())
            return;

        dispose();
    }

    /**
     * Checks if the texture is usable in Minecraft. A texture is considered usable in Minecraft if it isn't null, is
     * registered and has a valid GPU texture and sampler.
     */
    public boolean isUsableMinecraft() {
        return isRegistered() && isUsable();
    }

    /**
     * Checks if the texture is usable in ImGui. A texture is considered usable in ImGui if it isn't null and has a
     * valid GPU texture and sampler.
     */
    public boolean isUsableImGui() {
        return imGuiTexture.isUsable();
    }

    /**
     * Checks if the texture is pinned. A pinned texture should not be disposed of by the HybridTextureSweeper.
     */
    public boolean isPinned() {
        return pinned;
    }

    /**
     * Pins the texture. A pinned texture should not be disposed of by the HybridTextureSweeper.
     */
    public void pin() {
        pinned = true;
    }

    public void touch() {
        Level currentLevel = Minecraft.getInstance().level;

        if (currentLevel == null) {
            MyWorldTrafficAddition.LOGGER.warn("Tried to touch texture {} but the no level is loaded. Not touching.", id);
            return;
        }

        lastUsedTick.lazySet(currentLevel.getGameTime());
    }

    /**
     * Unpins the texture. An unpinned texture can be disposed of by the HybridTextureSweeper.
     */
    public void unpin() {
        pinned = false;
    }

    /**
     * Returns the width of the texture at the specified mipmap level. If the texture is not usable, returns 0.
     */
    public int getTextureWidth(int mipmapLevel) {
        if (textureView == null)
            return 0;

        return textureView.getWidth(mipmapLevel);
    }

    /**
     * Returns the height of the texture at the specified mipmap level. If the texture is not usable, returns 0.
     */
    public int getTextureHeight(int mipmapLevel) {
        if (textureView == null)
            return 0;

        return textureView.getHeight(mipmapLevel);
    }
}
