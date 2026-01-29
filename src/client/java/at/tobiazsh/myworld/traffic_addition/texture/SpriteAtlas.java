package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.exception.SpriteNotFoundException;
import at.tobiazsh.myworld.traffic_addition.exception.TextureNotLoadedException;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.HashSet;

public class SpriteAtlas implements AutoCloseable {

    /**
     * Raw data for a sprite in the atlas without UV coordinates as those depend on the atlas size
     * @param spriteId Identifier of the sprite
     * @param x the x coordinate of the sprite in the atlas
     * @param y the y coordinate of the sprite in the atlas
     * @param width width of the sprite
     * @param height height of the sprite
     */
    public record RawSpriteData(Identifier spriteId, int x, int y, int width, int height) {}

    private final String locationInJar; // String for simplicity. Otherwise, use Identifier if refactor is necessary
    private final boolean isResource;
    private final DynamicTexture texture;
    private final HashSet<Sprite> sprites = new HashSet<>();
    private final HashSet<RawSpriteData> uninitializedSprites = new HashSet<>();

    /**
     * Creates a SpriteAtlas instance
     * Starts at root of jar
     * @param locationInJar Location in jar starting with /assets/modid/... (e.g. /assets/myworld_traffic_addition/textures/atlas/sprites.png)
     */
    public SpriteAtlas(String locationInJar, RawSpriteData ...sprites) {
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

    public HashSet<Sprite> getSprites() {
        return sprites;
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

    /**
     * Initializes all uninitialized sprites by calculating their UV coordinates based on the atlas size
     * @throws TextureNotLoadedException if the texture is not loaded yet
     */
    public void initializeSprites() throws TextureNotLoadedException {
        int atlasWidth = getAtlasWidth();
        int atlasHeight = getAtlasHeight();

        for (RawSpriteData rawSprite : uninitializedSprites) {
            Sprite sprite = new Sprite(
                    rawSprite.spriteId,
                    rawSprite.x,
                    rawSprite.y,
                    rawSprite.width,
                    rawSprite.height,
                    atlasWidth,
                    atlasHeight
            );

            sprites.add(sprite);
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
    public HashSet<RawSpriteData> getUninitializedSprites() {
        return uninitializedSprites;
    }

    /**
     * Gets a sprite by its Identifier
     * @param id Identifier of the sprite
     * @return Sprite with the given Identifier
     * @throws SpriteNotFoundException if no sprite with the given Identifier is found
     */
    public Sprite getSpriteById(Identifier id) throws SpriteNotFoundException {
        Sprite spr = sprites.stream().filter(sprite -> sprite.spriteId.equals(id)).findFirst().orElse(null);
        if (spr == null)
            throw new SpriteNotFoundException("Sprite with id " + id.toString() + " not found in SpriteAtlas " + locationInJar);

        return spr;
    }

    @Override
    public void close() throws Exception {
        if (texture != null)
            texture.close();
    }
}
