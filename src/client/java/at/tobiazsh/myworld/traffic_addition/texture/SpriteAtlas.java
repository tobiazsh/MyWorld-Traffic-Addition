package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.exception.SpriteNotFoundException;
import at.tobiazsh.myworld.traffic_addition.exception.TextureNotLoadedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import net.minecraft.resources.Identifier;
import at.tobiazsh.myworld.traffic_addition.texture.Sprite.RawSpriteData;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

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

    // BASE REQUIRED
    public static final String KEY_ATLAS_ID = "atlasId";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_IN_JAR = "inJar";
    public static final String KEY_SPRITES = "sprites";
    public static final String KEY_NAME = "name"; // Optional

    // NICHE / NOT STRICTLY REQUIRED
    public static final String KEY_AUTOTYPE = "isAuto";

    // AUTO TYPE REQUIRED
    public static final String KEY_START_X = "startX";
    public static final String KEY_START_Y = "startY";
    public static final String KEY_BASE_WIDTH = "baseWidth";
    public static final String KEY_BASE_HEIGHT = "baseHeight";
    public static final String KEY_ID_NAMESPACE = "spriteIdNamespace";
    public static final String KEY_ID_PATHS = "spriteIdPaths";

    private final String locationInJar; // String for simplicity. Otherwise, use Identifier if refactor is necessary
    private final String name;
    private final boolean isResource;
    private HybridTexture texture;
    private final HashMap<Identifier, Sprite> sprites = new HashMap<>(); // Map of sprites by their Identifier
    private final HashSet<RawSpriteData> uninitializedSprites = new HashSet<>();
    private final Identifier atlasId;

    /*
     * |_________________________________________________| NOTE |_________________________________________________|
     * | IF IMPLEMENTING SPRITE OUTSIDE RESOURCES, THEY CAN ONLY BE LOADED DURING RUNTIME, NOT AT INIT BECAUSE OF |
     * | THE WAY DYNAMIC TEXTURES WORK!                                                                           |
     * |__________________________________________________________________________________________________________|
     * (yes, I had fun designing that)
     */

    /**
     * Creates a SpriteAtlas instance
     * Starts at root of jar
     * @param locationInJar Location in jar starting with /assets/modid/... (e.g. /assets/myworld_traffic_addition/textures/atlas/sprites.png)
     */
    public SpriteAtlas(Identifier atlasId, String locationInJar, String name, RawSpriteData...sprites) {
        this.atlasId = atlasId;
        this.locationInJar = locationInJar;
        this.isResource = true; // For now, only support resources; Implemented for future use cases
        this.name = name;

        this.uninitializedSprites.addAll(Arrays.asList(sprites));
    }

    /**
     * Creates a SpriteAtlas instance
     * Starts at root of jar
     * @param locationInJar Location in jar starting with /assets/modid/... (e.g. /assets/myworld_traffic_addition/textures/atlas/sprites.png)
     */
    public SpriteAtlas(Identifier atlasId, String locationInJar, RawSpriteData...sprites) {
        this(atlasId, locationInJar, atlasId.toString(), sprites);
    }

    public @Nullable HybridTexture getTexture() {
        return texture;
    }

    public boolean isResource() {
        return isResource;
    }

    /**
     * Loads the texture from scratch and registers it in Minecraft's TextureManager.
     * <p>This method should be called after the SpriteAtlas is created and before any sprites are used.
     * @param force If true, forces the texture to be reloaded even if it is already loaded.
     *              If false, does nothing if the texture is already loaded.
     * @throws IOException If the texture could not be loaded (e.g. file not found)
     */
    public void loadTexture(boolean force) throws IOException {
        // Do not unload old textures here. They will be disposed of automatically if unpinned.

        if (!force && isUsable()) {
            if (!isUsableMinecraft())
                texture.register(); // Register the texture in Minecraft's TextureManager if it is not already registered

            return;
        }

        ImGuiTexture imGuiTexture;

        try (var stream = SpriteAtlas.class.getResourceAsStream(locationInJar)) {
            if (stream == null) {
                throw new IOException(
                        "Could not load sprite atlas texture from "
                        + locationInJar + " because the resource does not exist!"
                );
            }

            MyWorldTrafficAddition.LOGGER.info("Uploading sprite atlas texture from " + locationInJar + " into GPU memory...");

            imGuiTexture = ImGuiTextureFactory.fromStream(
                    stream,
                    "SpriteAtlas_" + atlasId.toString()
            );
        }

        texture = HybridTexture.ofImGuiTexture(
                imGuiTexture,
                atlasId
        );

        texture.register(); // Register the texture in Minecraft's TextureManager
        this.initializeSprites();

        // Unlike the versions before, we do not pin the texture here as atlases will keep expanding in the mod
        // and allow for files outside the JAR, it is important that only currently used atlases are kept in memory
        // (pinned). Therefore, the new version requires explicit registration when using and unregistration when no
        // longer using the atlas. This is done to prevent hogging memory with unused atlases.
    }

    /**
     * Unloads the texture and unregisters it from Minecraft's TextureManager.
     */
    public void unloadTexture() {
        if (texture != null) {
            texture.disposeSafe();

            // This does not set the texture to null. Whether the texture is null or not, is an indication of whether
            // the texture has never been loaded before. This is to make use of automatic unloading of textures when
            // no longer used (see HybridTextureSweeper), so we can check here if it must be loaded from 0 again or can
            // be reused.
        }
    }

    /**
     * Returns all the sprites in an unmodifiable collection, no matter whether the texture is actually loaded or
     * not. Check before using.
     */
    public Collection<Sprite> getSprites() {
        return Collections.unmodifiableCollection(sprites.values());
    }

    /**
     * Informs whether the texture is usable (loaded and not disposed) or not.
     * <p>Does not check whether the sprite is usable specifically for Minecraft or ImGui.
     *    Use {@link #isUsableMinecraft()} or {@link #isUsableImGui()} for that.
     */
    public boolean isUsable() {
        return texture != null && texture.isUsable();
    }

    /**
     * Checks whether the sprite atlas texture is usable for ImGui rendering.
     * <p>A texture is considered usable in ImGui if it isn't null and has a valid GPU texture and sampler.
     */
    public boolean isUsableImGui() {
        return texture != null && texture.isUsableImGui();
    }

    /**
     * Checks whether the sprite atlas texture is usable for Minecraft rendering.
     * <p>A texture is considered usable in Minecraft if it isn't null, is registered
     *    and has a valid GPU texture and sampler.
     */
    public boolean isUsableMinecraft() {
        return texture != null && texture.isUsableMinecraft();
    }

    public int getAtlasWidth() throws TextureNotLoadedException {
        if (!isUsable()) {
            throw new TextureNotLoadedException(
                    "Cannot get width of SpriteAtlas " +
                    locationInJar + " because the texture is not loaded!"
            );
        }

        return texture.getTextureWidth(0); // Atlases don't have mipmaps
    }

    public int getAtlasHeight() throws TextureNotLoadedException {
        if (!isUsable()) {
            throw new TextureNotLoadedException(
                    "Cannot get height of SpriteAtlas " +
                    locationInJar + " because the texture is not loaded!"
            );
        }

        return texture.getTextureHeight(0);
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
        unloadTexture();
    }

    // STATIC METHODS ------------------------------------------------------------

    /**
     * <p>
     *     Loads a SpriteAtlas from a JSON file.
     *     The JSON file should look like this:
     *
     *     <pre>
     *     {@code
     *     {
     *         "atlasId": "namespace:atlas_name",
     *         "location": "./sprites/someatlas.png",
     *         "inJar": true,
     *         "isAuto": false,
     *         "sprites": [
     *              ... list of sprites ...
     *         ]
     *     }
     *     }
     *     </pre>
     * <p>
     *     Note that {@code isAuto} defaults to {@code true} if not specified otherwise, and that the structure as well
     *     as the loading method {@link SpriteAtlas#fromJsonAuto(JsonObject)} will be used. Therefor setting
     *     {@code isAuto} to {@code false} is required if manual handling should be used. For more information, see
     *     loading method.
     * <p>
     *     For structure of sprites, see {@link Sprite#fromJson(JsonObject)}
     * @param jsonAtlas JsonObject representing the SpriteAtlas
     * @return Deserialized SpriteAtlas
     */
    public static SpriteAtlas fromJson(JsonObject jsonAtlas) throws IllegalArgumentException, NotImplementedException {

        // If matches type auto, parse as type auto
        if (!jsonAtlas.has(KEY_AUTOTYPE) || jsonAtlas.get(KEY_AUTOTYPE).getAsBoolean())
            return fromJsonAuto(jsonAtlas);

        if (!hasBaseFields(jsonAtlas))
            throw new IllegalArgumentException("Invalid SpriteAtlas JSON: Missing required base fields)");

        if (!hasManualFields(jsonAtlas))
            throw new IllegalArgumentException("Invalid SpriteAtlas JSON: Missing required manual fields");

        // No check for sprites existence, as it is not required and can be empty

        Identifier atlasId = Identifier.parse(jsonAtlas.get(KEY_ATLAS_ID).getAsString());
        String location = jsonAtlas.get(KEY_LOCATION).getAsString();
        boolean inJar = jsonAtlas.get(KEY_IN_JAR).getAsBoolean();
        String name = jsonAtlas.has("name") ? jsonAtlas.get("name").getAsString() : atlasId.toString();

        if (!inJar)
            throw new NotImplementedException("SpriteAtlas loading from file system is not implemented yet!");

        JsonArray jsonSprites = jsonAtlas.get(KEY_SPRITES).getAsJsonArray();
        RawSpriteData[] sprites = new RawSpriteData[jsonSprites.size()];

        for (int i = 0; i < jsonSprites.size(); i++) {
            sprites[i] = Sprite.fromJson(jsonSprites.get(i).getAsJsonObject());
        }

        return new SpriteAtlas(atlasId, location, name, sprites);
    }

    /**
     * <p>
     *     Parses an atlas that is marked as an auto type.
     * </p>
     * <p>
     *     Auto types make writing sprite atlases easier as you only have to specify the proportions of each sprite
     *     once at the top-level.
     * </p>
     * <p>
     *     Use the following structure for such atlases:
     *     <pre>
     *     {@code {
     *         "atlasId": "namespace:atlas_name",
     *         "location": "./sprites/someatlas.png",
     *         "inJar": true,
     *         "isAuto": true, // Optional, because defaults to true!
     *         "startX": 0, // Starts scanning atlas from X
     *         "startY": 0, // Starts scanning atlas from Y
     *         "baseWidth": 1024, // Width of each texture
     *         "baseHeight": 1024, // Height of each texture
     *         "spriteIdNamespace": "some_default_id",
     *         "spriteIdPaths": [
     *              // Will work with spriteIdNamespace and
     *              // form e.g. "some_default_id:sword"
     *
     *              // define path from each sprite id
     *              // (from left to right, top to bottom) here... example:
     *             ["sword", "axe", "hoe"], // rowIndex 1
     *             ["shovel", "spade", "pickaxe"], // rowIndex 2
     *             ...
     *         ]
     *     }
     *     }
     *     </pre>
     * </p>
     * <p>
     *     If customization is necessary, such as when each texture uses a different size, using it is not viable,
     *     instead use the manual loader {@link SpriteAtlas#fromJson(JsonObject)}
     * </p>
     * @param jsonAtlas The file contents parsed in a JsonObject
     * @return The parsed SpriteAtlas
     * @throws IllegalArgumentException If one or multiple argument are not present/invalid or if the sprite atlas JSON does
     * not represent an auto type
     * @throws NotImplementedException If the sprite atlas is expecting files from outside the JAR-Resources because
     * that feature is not yet implemented.
     */
    public static SpriteAtlas fromJsonAuto(JsonObject jsonAtlas) throws IllegalArgumentException, NotImplementedException {

        if (!hasBaseFields(jsonAtlas))
            throw new IllegalArgumentException("Invalid SpriteAtlas JSON: Missing required base fields");

        Identifier atlasId = Identifier.parse(jsonAtlas.get(KEY_ATLAS_ID).getAsString());
        String location = jsonAtlas.get(KEY_LOCATION).getAsString();
        boolean inJar = jsonAtlas.get(KEY_IN_JAR).getAsBoolean();

        if (!inJar)
            throw new NotImplementedException("SpriteAtlas loading from file system is not implemented yet!");

        // First check if the atlas is of type auto.
        /* isAuto defaults to true, so if it exists evaluate further: If the field says "false" (meaning: manual type)
         * then throw
         */
        if (jsonAtlas.has(KEY_AUTOTYPE) && !jsonAtlas.get(KEY_AUTOTYPE).getAsBoolean())
            throw new IllegalArgumentException("Tried loading manual sprite atlas using auto type loader!");

        if (!hasAutoFields(jsonAtlas))
            throw new IllegalArgumentException("Invalid SpriteAtlas JSON: Missing required fields for auto type");

        int startX = jsonAtlas.get(KEY_START_X).getAsInt();
        int startY = jsonAtlas.get(KEY_START_Y).getAsInt();

        int baseWidth = jsonAtlas.get(KEY_BASE_WIDTH).getAsInt();
        int baseHeight = jsonAtlas.get(KEY_BASE_HEIGHT).getAsInt();

        String idNamespace = jsonAtlas.get(KEY_ID_NAMESPACE).getAsString();

        JsonArray rowArray = jsonAtlas.get(KEY_ID_PATHS).getAsJsonArray();
        int[] rowSizes = new int[rowArray.size()];

        // Checks how many entries there are on each rowIndex
        for (int i = 0; i < rowArray.size(); i++) {
            JsonArray row = rowArray.get(i).getAsJsonArray(); // Throws IllegalArgumentException automatically if is other type
            rowSizes[i] = row.size();
        }

        // Calculate how many entries there will be in total
        RawSpriteData[] sprites = new RawSpriteData[IntStream.of(rowSizes).sum()];

        // Construct each sprite from the known data
        int entryCount = 0; // Could calculate from rowSizes, but more complicated and less performant. This is just easier and better.
        for (int rowIndex = 0; rowIndex < rowArray.size(); rowIndex++) {
            JsonArray row = rowArray.get(rowIndex).getAsJsonArray();
            for (int colIndex = 0; colIndex < rowSizes[rowIndex]; colIndex++) {
                sprites[entryCount] = new RawSpriteData(
                        Identifier.fromNamespaceAndPath(idNamespace, row.get(colIndex).getAsString()),
                        startX + (baseWidth * colIndex),
                        startY + (baseHeight * rowIndex),
                        baseWidth,
                        baseHeight
                );

                entryCount++;
            }
        }

        String name = jsonAtlas.has("name") ? jsonAtlas.get("name").getAsString() : atlasId.toString();

        return new SpriteAtlas(atlasId, location, name, sprites);
    }

    /**
     * Checks if the provided sprite atlas JSON contains the required base fields
     */
    private static boolean hasBaseFields(JsonObject jsonAtlas) {
        return jsonAtlas.has(KEY_LOCATION) &&
                jsonAtlas.has(KEY_IN_JAR) &&
                jsonAtlas.has(KEY_ATLAS_ID);
    }

    /**
     * Checks if the provided sprite atlas JSON contains the required fields for the auto type
     */
    private static boolean hasAutoFields(JsonObject jsonAtlas) {
        return jsonAtlas.has(KEY_ID_PATHS) &&
                jsonAtlas.has(KEY_ID_NAMESPACE) &&
                jsonAtlas.has(KEY_BASE_WIDTH) &&
                jsonAtlas.has(KEY_BASE_HEIGHT) &&
                jsonAtlas.has(KEY_START_X) &&
                jsonAtlas.has(KEY_START_Y);
    }

    /**
     * Checks if the provided sprite atlas JSON contains the required fields for the manual type
     */
    private static boolean hasManualFields(JsonObject jsonAtlas) {
        return jsonAtlas.has(KEY_SPRITES) &&
                jsonAtlas.has(KEY_AUTOTYPE);
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
