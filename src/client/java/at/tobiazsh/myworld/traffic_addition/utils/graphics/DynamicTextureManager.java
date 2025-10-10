package at.tobiazsh.myworld.traffic_addition.utils.graphics;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicTextureManager {

    private static final Map<Identifier, DynamicTexture> textures = new ConcurrentHashMap<>();

    /**
     * Adds a DynamicTexture to the manager if it doesn't exist yet. If it does, it just returns the existing DynamicTexture.
     *
     * @param id the Identifier to register the texture with in the TextureManager
     * @param texture The texture to register
     * @return the previous texture associated with the id, or null if there was no mapping for the id.
     */
    public static DynamicTexture addTexture(Identifier id, DynamicTexture texture) {
        return textures.computeIfAbsent(id, k -> texture);
    }

    /**
     * Replaces the texture associated with the given id.
     *
     * @param id the Identifier to register the texture with in the TextureManager
     * @param texture The texture to register
     * @return the previous texture associated with the id, or null if there was no mapping for the id.
     */
    public static DynamicTexture replaceTexture(Identifier id, DynamicTexture texture) {
        return textures.replace(id, texture);
    }

    /**
     * Gets the DynamicTexture associated with the given id.
     *
     * @param id the Identifier to register the texture with in the TextureManager
     * @return the texture associated with the id, or null if there is no mapping for the id.
     */
    public static DynamicTexture getTexture(Identifier id) {
        return textures.get(id);
    }

    /**
     * Removes the DynamicTexture associated with the given id.
     *
     * @param id the Identifier to register the texture with in the TextureManager
     * @return the texture that was removed, or null if there was no mapping for the id.
     */
    public static DynamicTexture removeTexture(Identifier id) {
        return textures.remove(id);
    }

    /**
     * Checks if a texture with the given id exists.
     *
     * @param id the Identifier to register the texture with in the TextureManager
     * @return true if a texture with the given id exists, false otherwise.
     */
    public static boolean hasTexture(Identifier id) {
        return textures.containsKey(id);
    }

    /**
     * Checks if the given texture exists in the manager.
     *
     * @param texture the DynamicTexture to check for
     * @return true if the texture exists in the manager, false otherwise.
     */
    public static boolean hasValue(DynamicTexture texture) {
        return textures.containsValue(texture);
    }

}
