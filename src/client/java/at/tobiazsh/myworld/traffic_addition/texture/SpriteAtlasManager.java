package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class SpriteAtlasManager {

    public static final SpriteAtlasManager INSTANCE = new SpriteAtlasManager();

    private final HashMap<Identifier, SpriteAtlas> atlases = new HashMap<>(); // Loaded atlases

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
     * Loads a sprite atlas from a JSON file
     * @param jsonFile the JSON file to load the sprite atlas from
     * @return the loaded SpriteAtlas
     * @throws IOException If an error occurs while reading the file (such as a non-existent file)
     * @throws IllegalArgumentException If the JSON is malformed or the sprite atlas cannot be created
     */
    public SpriteAtlas loadSpriteAtlas(FileSystem.File jsonFile) throws IOException, IllegalArgumentException {
        if (!jsonFile.exists())
            throw new FileNotFoundException("Sprite atlas JSON file not found: " + jsonFile.path);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(jsonFile.toJavaFile()))) {
            byte[] file = bis.readAllBytes();
            String jsonContent = new String(file, StandardCharsets.UTF_8);

            // Construct the sprite atlas from the JSON content
            // This will also validate the JSON inside the file and throw an IllegalArgumentException if the JSON is
            // malformed or the sprite atlas cannot be created
            SpriteAtlas atlas = SpriteAtlas.fromJson(JsonParser.parseString(jsonContent).getAsJsonObject());

            atlas.loadTexture(); // Load the texture into the TextureManager so that we can get the atlas dimensions for sprite initialization
            atlas.initializeSprites(); // Initialize all sprites to convert their raw data into actual sprites
            atlases.put(atlas.getAtlasId(), atlas); // Add the atlas to the manager

            return atlas;
        }
    }
}
