package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.exception.SpriteNotFoundException;
import at.tobiazsh.myworld.traffic_addition.exception.TextureNotLoadedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import at.tobiazsh.myworld.traffic_addition.texture.Sprite.RawSpriteData;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

/**
 * <p>
 * A sprite atlas that holds multiple sprites in a single texture.
 * The sprites do not copy any pixels and instead only hold UV, XY and size information.
 * Use the texture from this atlas to render the sprites.
 * </p>
 * <p>
 * INFORMATION:
 * You should not use this class directly. Instead, to load a sprite atlas, use the {@link SpriteAtlasManager}.
 * The manager handles loading, unloading and reusing of sprite atlases.
 * </p>
 * <p>
 * If the manager does not fulfill your desires, you can always use this class directly, although it is not
 * recommended.
 * If you have other desires, which neither this class nor the manager can fulfill, feel free to either open an issue,
 * or fix it yourself and open a PR.
 * </p>
 */
public class SpriteAtlas implements AutoCloseable {

    public static final String KEY_ATLAS_ID = "atlasId";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_IN_JAR = "inJar";
    public static final String KEY_SPRITES = "sprites";

    private final String locationInJar; // String for simplicity. Otherwise, use Identifier if refactor is necessary
    private final boolean isResource;
    private final DynamicTexture texture;
    private final HashMap<Identifier, Sprite> sprites = new HashMap<>(); // Map of sprites by their Identifier
    private final HashSet<RawSpriteData> uninitializedSprites = new HashSet<>();
    private final Identifier atlasId;

    /**
     * Creates a SpriteAtlas instance
     * Starts at root of jar
     * @param locationInJar Location in jar starting with /assets/modid/... (e.g. /assets/myworld_traffic_addition/textures/atlas/sprites.png)
     */
    public SpriteAtlas(Identifier atlasId, String locationInJar, RawSpriteData...sprites) {
        this.atlasId = atlasId;
        this.locationInJar = locationInJar;
        this.isResource = true; // For now, only support resources; Implemented for future use cases

        texture = new DynamicTexture(
                locationInJar,
                Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, locationInJar),
                this.isResource // The "always true" is fine because we only support resources for now but might add file support later
        );

        texture.dontDestroyWhenPossible(); // Atlases, which are in the JAR, should stay in memory unless explicitly removed

        this.uninitializedSprites.addAll(Arrays.asList(sprites));
    }

    public DynamicTexture getTexture() {
        return texture;
    }

    public boolean isResource() {
        return isResource;
    }

    /**
     * Loads the texture into the TextureManager
     */
    public void loadTexture() {
        texture.smartRegisterTexture();
    }

    public Collection<Sprite> getSprites() {
        return Collections.unmodifiableCollection(sprites.values());
    }

    public boolean isLoaded() {
        return texture.isLoaded();
    }

    public int getAtlasWidth() throws TextureNotLoadedException {
        if (!isLoaded())
            throw new TextureNotLoadedException("Cannot get width of SpriteAtlas " + locationInJar + " because the texture is not loaded yet!");

        return texture.getWidth();
    }

    public int getAtlasHeight() throws TextureNotLoadedException {
        if (!isLoaded())
            throw new TextureNotLoadedException("Cannot get height of SpriteAtlas " + locationInJar + " because the texture is not loaded yet!");

        return texture.getHeight();
    }

    public Identifier getAtlasId() {
        return atlasId;
    }

    /**
     * Initializes all uninitialized sprites by calculating their UV coordinates based on the atlas size
     * @throws TextureNotLoadedException if the texture is not loaded yet
     */
    public void initializeSprites() throws TextureNotLoadedException {
        int atlasWidth = getAtlasWidth();
        int atlasHeight = getAtlasHeight();

        for (RawSpriteData rawSprite : uninitializedSprites) {
            Sprite sprite = new Sprite(
                    rawSprite.spriteId(),
                    rawSprite.x(),
                    rawSprite.y(),
                    rawSprite.width(),
                    rawSprite.height(),
                    atlasWidth,
                    atlasHeight
            );

            sprites.put(sprite.spriteId, sprite);
        }

        uninitializedSprites.clear();
    }

    /**
     * Checks if all sprites are initialized
     * @return true if all sprites are initialized, false otherwise
     */
    public boolean allSpritesInitialized() {
        return uninitializedSprites.isEmpty();
    }

    /**
     * Gets the uninitialized sprites
     * @return Set of uninitialized sprites
     */
    public Collection<RawSpriteData> getUninitializedSprites() {
        return Collections.unmodifiableCollection(uninitializedSprites);
    }

    /**
     * Gets a sprite by its Identifier
     * @param id Identifier of the sprite
     * @return Sprite with the given Identifier
     * @throws SpriteNotFoundException if no sprite with the given Identifier is found
     */
    public Sprite getSprite(Identifier id) throws SpriteNotFoundException {
        Sprite spr = sprites.get(id);
        if (spr == null)
            throw new SpriteNotFoundException("Sprite with id " + id.toString() + " not found in SpriteAtlas " + locationInJar);

        return spr;
    }

    @Override
    public void close() throws Exception {
        texture.close();
    }

    // STATIC METHODS ------------------------------------------------------------

    /**
     * Loads a SpriteAtlas from a JSON file.
     * The JSON file should look like this:
     * <pre>
     * {@code
     * {
     *     "atlasId": "namespace:atlas_name",
     *     "location": "./sprites/someatlas.png",
     *     "inJar": true,
     *     "sprites": [
     *          ... list of sprites ...
     *     ]
     * }
     * }
     * </pre>
     * <p>
     * For structure of sprites, see {@link Sprite#fromJson(JsonObject)}
     * </p>
     * @param jsonAtlas JsonObject representing the SpriteAtlas
     * @return Deserialized SpriteAtlas
     */
    public static SpriteAtlas fromJson(JsonObject jsonAtlas) throws IllegalArgumentException, NotImplementedException {

        if (
                !jsonAtlas.has(KEY_LOCATION) ||
                !jsonAtlas.has(KEY_IN_JAR) ||
                !jsonAtlas.has(KEY_ATLAS_ID)
        ) {
            throw new IllegalArgumentException("Invalid SpriteAtlas JSON: Missing required fields (atlasId, location, inJar)");
        }

        // No check for sprites existence, as it is not required and can be empty

        Identifier atlasId = Identifier.parse(jsonAtlas.get(KEY_ATLAS_ID).getAsString());
        String location = jsonAtlas.get(KEY_LOCATION).getAsString();
        boolean inJar = jsonAtlas.get(KEY_IN_JAR).getAsBoolean();

        if (!inJar)
            throw new NotImplementedException("SpriteAtlas loading from file system is not implemented yet!");

        JsonArray jsonSprites = jsonAtlas.get(KEY_SPRITES).getAsJsonArray();
        RawSpriteData[] sprites = new RawSpriteData[jsonSprites.size()];

        for (int i = 0; i < jsonSprites.size(); i++) {
            sprites[i] = Sprite.fromJson(jsonSprites.get(i).getAsJsonObject());
        }

        return new SpriteAtlas(atlasId, location, sprites);
    }

    /**
     * Serializes a SpriteAtlas to a JSON file.
     * @return JsonObject representing the SpriteAtlas
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(KEY_LOCATION, locationInJar);
        json.addProperty(KEY_IN_JAR, isResource);

        JsonArray spritesArray = new JsonArray();

        for (Sprite sprite : sprites.values())
            spritesArray.add(sprite.toJson());

        json.add(KEY_SPRITES, spritesArray);

        return json;
    }
}
