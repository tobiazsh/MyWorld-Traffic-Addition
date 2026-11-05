package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteAtlasManager {

    public static final SpriteAtlasManager INSTANCE = new SpriteAtlasManager();

    private final ConcurrentHashMap<Identifier, SpriteAtlas> atlases = new ConcurrentHashMap<>(); // Loaded atlases

    private SpriteAtlasManager() {}

    public SpriteAtlas getSpriteAtlas(Identifier atlasId) {
        return atlases.get(atlasId);
    }

    public boolean isAtlasLoaded(Identifier atlasId) {
        return atlases.containsKey(atlasId);
    }

    public Collection<SpriteAtlas> getAtlases() {
        return Collections.unmodifiableCollection(atlases.values());
    }

    /**
     * Loads a sprite atlas from a JSON file into memory.
     * <p>Does not load the actual sprite's file content.
     *    Use {@link SpriteAtlas#loadTexture(boolean)} to load the texture from the sprite atlas.
     * @param jsonFile the JSON file to load the sprite atlas from
     * @return the loaded SpriteAtlas
     * @throws IOException If an error occurs while reading the file (such as a non-existent file)
     * @throws IllegalArgumentException If the JSON is malformed or the sprite atlas cannot be created
     * @throws NullPointerException Error during file read
     */
    public SpriteAtlas loadSpriteAtlas(FileSystem.File jsonFile) throws IOException, IllegalArgumentException, NullPointerException {
        if (!jsonFile.exists())
            throw new FileNotFoundException("Sprite atlas JSON file not found: " + jsonFile.path);

        String jsonContent = new String(jsonFile.readBytes(), StandardCharsets.UTF_8);

        // Construct the sprite atlas from the JSON content
        // This will also validate the JSON inside the file and throw an IllegalArgumentException if the JSON is
        // malformed or the sprite atlas cannot be created
        SpriteAtlas atlas = SpriteAtlas.fromJson(JsonParser.parseString(jsonContent).getAsJsonObject());

        // Do not initialize sprites here. They will be initialized on-the-go when the sprite atlas' content is loaded
        // into memory the first time and then kept there until the program is closed. This is to avoid loading all the
        // sprites into memory at once while starting, which could cause a long loading time and high memory usage if
        // there are many sprites to be loaded.

        atlases.put(atlas.getAtlasId(), atlas); // Add the atlas to the manager

        return atlas;
    }
}
