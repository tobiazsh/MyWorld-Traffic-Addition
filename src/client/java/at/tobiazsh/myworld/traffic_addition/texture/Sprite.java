package at.tobiazsh.myworld.traffic_addition.texture;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Sprite {

    /**
     * Raw data for a sprite in the atlas without UV coordinates as those depend on the atlas size
     * @param spriteId Identifier of the sprite
     * @param x the x coordinate of the sprite in the atlas
     * @param y the y coordinate of the sprite in the atlas
     * @param width width of the sprite
     * @param height height of the sprite
     */
    public record RawSpriteData(Identifier spriteId, int x, int y, int width, int height) {}

    public static final String KEY_SPRITE_ID = "spriteId";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";

    public final Identifier spriteId;
    public final int x, y, width, height;
    public final float u1, v1, u2, v2;

    /**
     * Creates a sprite instance
     * @param spriteId Identifier of the sprite
     * @param x Starting X-Coordinate of Sprite in {@link SpriteAtlas}
     * @param y Starting Y-Coordinate of Sprite in {@link SpriteAtlas}
     * @param width Width of the sprite
     * @param height Height of the sprite
     * @param atlasWidth Width of the {@link SpriteAtlas} the sprite is located in
     * @param atlasHeight Height of the {@link SpriteAtlas} the sprite is located in
     */
    public Sprite(Identifier spriteId, int x, int y, int width, int height, int atlasWidth, int atlasHeight) {
        this.spriteId = spriteId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u1 = (float) x / (float) atlasWidth;
        this.v1 = (float) y / (float) atlasHeight;
        this.u2 = (float) (x + width) / (float) atlasWidth;
        this.v2 = (float) (y + height) / (float) atlasHeight;
    }

    /**
     * <p>
     *     Deserializes a Sprite from its JSON representation
     * </p>
     * <p>
     *     Structure:
     * </p>
     * <pre>
     * {@code
     * {
     *     "spriteId": "namespace:sprite_name",
     *     "x": 0,
     *     "y": 0,
     *     "width": 0,
     *     "height": 0
     * }
     * }
     * </pre>
     * @param jsonSprite JsonObject representing the sprite
     * @return Deserialized RawSpriteData
     */
    public static RawSpriteData fromJson(JsonObject jsonSprite) throws IllegalArgumentException {
        if (
                !jsonSprite.has(KEY_SPRITE_ID) ||
                !jsonSprite.has(KEY_X) ||
                !jsonSprite.has(KEY_Y) ||
                !jsonSprite.has(KEY_WIDTH) ||
                !jsonSprite.has(KEY_HEIGHT)
        ) {
            throw new IllegalArgumentException("Invalid sprite JSON: Missing required fields (spriteId, x, y, width, height)");
        }

        Identifier spriteId = Identifier.parse(jsonSprite.get(KEY_SPRITE_ID).getAsString());
        int x = jsonSprite.get(KEY_X).getAsInt();
        int y = jsonSprite.get(KEY_Y).getAsInt();
        int width = jsonSprite.get(KEY_WIDTH).getAsInt();
        int height = jsonSprite.get(KEY_HEIGHT).getAsInt();

        if (
                x < 0 || y < 0 || width < 0 || height < 0
        ) {
            throw new IllegalArgumentException("Invalid sprite JSON: Invalid fields (spriteId, x, y, width, height)");
        }

        return new RawSpriteData(
                spriteId,
                x,
                y,
                width,
                height
        );
    }

    /**
     * Serializes this Sprite to its JSON representation
     * @return JsonObject representing the sprite
     */
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(KEY_SPRITE_ID, spriteId.toString());
        jsonObject.addProperty(KEY_X, x);
        jsonObject.addProperty(KEY_Y, y);
        jsonObject.addProperty(KEY_WIDTH, width);
        jsonObject.addProperty(KEY_HEIGHT, height);

        return jsonObject;
    }
}
