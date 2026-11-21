package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.ThrowingSupplier;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A wrapper around an {@link ImGuiTexture} that tracks its usage and manages its disposal lifecycle.
 *
 * <p>Each instance maintains the game tick at which the texture was last accessed. This information can be
 * used by a cache management implementation to identify textures that are no longer in use and can safely
 * be disposed of.
 *
 * <p>Textures can be <b>pinned</b> to prevent automatic disposal.
 * Pinned textures remain protected regardless of their last usage tick until explicitly unpinned or disposed.
 *
 * <p>By default, textures are not pinned.
 *
 * <p>Use {@link #touch()} whenever the texture is accessed to update its last-used tick. If no level is
 * currently loaded, the usage tick is not updated.
 *
 * <p>Disposal can be performed directly using {@link #dispose()} or safely using {@link #disposeSafe()}.
 * The latter respects the pinned state and will not dispose of pinned textures.
 *
 * <p><b>Note:</b> Disposing a texture invalidates its underlying GPU resources. A disposed texture must
 * not be used again.
 *
 */
public class TickableImGuiTexture {

    private final @NonNull ImGuiTexture imGuiTexture;
    private final Identifier id;

    private final AtomicBoolean pinned = new AtomicBoolean(false);

    private final AtomicLong lastUsedTick = new AtomicLong(0);

    public TickableImGuiTexture(
            @NonNull ImGuiTextureImpl imGuiTexture,
            @NonNull Identifier id
    ) {
        this.id = id;
        this.imGuiTexture = imGuiTexture;
    }

    /**
     * Creates a new {@link TickableImGuiTexture} from an {@link ImGuiTexture}.
     *
     * @param imGuiTexture The ImGuiTexture to use.
     * @param id The id of the ImGuiTexture. Not necessarily used in this class, but useful for other implementations.
     * @return A new TickableImGuiTexture instance.
     *
     * @throws IllegalArgumentException if the {@link ImGuiTexture} is not an instance of
     * {@link dev.tobiazsh.imguib3d.client.texture.ImGuiTextureImpl}
     */
    public static @NonNull TickableImGuiTexture ofImGuiTexture(
            @NonNull ImGuiTexture imGuiTexture,
            Identifier id
    ) {
        if (!(imGuiTexture instanceof ImGuiTextureImpl impl))
            throw new IllegalArgumentException("ImGuiTexture must be an instance of ImGuiTextureImpl");

        return new TickableImGuiTexture(impl, id);
    }

    /**
     * Tries to get the texture from the provided cache using the id. If not found, creates a new
     * {@link TickableImGuiTexture} from an {@link ImGuiTexture}.
     *
     * @param imGuiTextureSupplier A supplier of the ImGuiTexture. Used to not unnecessarily create resources later disposed.
     *                             Can throw an exception. Whatever exception is thrown is passed up to this method.
     * @param id The id of the ImGuiTexture. Not necessarily used in this class, but useful for other implementations.
     * @param <E> The type of exception thrown by the supplier.
     * @param shouldCache Whether the entry should be cached right away.
     * @param cache The cache to use.
     * @return A new TickableImGuiTexture instance.
     *
     * @throws E if the supplier failed.
     * @throws IllegalArgumentException if the {@link ImGuiTexture} is not an instance of
     * {@link dev.tobiazsh.imguib3d.client.texture.ImGuiTextureImpl}
     */
    public static <E extends Exception> @NonNull TickableImGuiTexture ofImGuiTextureCached(
            ThrowingSupplier<@NonNull ImGuiTexture, E> imGuiTextureSupplier,
            Identifier id,
            boolean shouldCache,
            @NonNull TickableImGuiTextureCache cache
    ) throws E {
        var cached = cache.byId(id);

        if (cached != null)
            return cached;

        var texture = ofImGuiTexture(imGuiTextureSupplier.get(), id);

        if (shouldCache)
            cache.add(texture);

        return texture;
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
     * Disposes the texture, regardless of it being pinned or not.
     * After calling this method, the texture should not be used anymore.
     *
     * <p>It is generally not recommended to use this method unless you know what you are doing. If you don't,
     * please consider using {@link #disposeSafe()}</p>
     */
    public void dispose() {
        imGuiTexture.dispose(); // Then dispose the ImGuiTexture, which will finally dispose off the GPU resources
    }

    /**
     * Only disposes unpinned textures. If the texture is pinned, it will not be disposed of.
     * After calling this method, the texture should not be used anymore.
     */
    public void disposeSafe() {
        if (isPinned())
            return;

        dispose();
    }

    /**
     * Checks if the texture is pinned. A pinned texture should not be disposed of.
     */
    public boolean isPinned() {
        return pinned.get();
    }

    /**
     * Pins the texture. A pinned texture should not be disposed of.
     */
    public void pin() {
        pinned.set(true);
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
     * Unpins the texture. An unpinned texture can be disposed of by.
     */
    public void unpin() {
        pinned.set(true);
    }
}
