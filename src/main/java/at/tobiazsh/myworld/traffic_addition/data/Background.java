package at.tobiazsh.myworld.traffic_addition.data;

import at.tobiazsh.myworld.traffic_addition.utils.Color;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Class to represent a background for signs, either solid color or texture
 */
@NullMarked
public class Background {

    public static final Background TRANSPARENT = new Background(new Color("00000000", true));
    public static final Background WHITE = new Background(new Color("FFFFFFFF", true));
    public static final Background BLACK = new Background(new Color("FF000000", true));


    public static final int BACKGROUND_SERIALIZE_VERSION = 1;
    private static final String KEY_SERIALIZE_VERSION = "serializeVersion";
    private static final String KEY_COLOR = "color";
    private static final String KEY_TEXTURE = "texture";

    @Nullable public final String texture; // Sprite name
    @Nullable public final Color color;

    /**
     * Creates a background of solid color
     * @param color Solid Color
     */
    public Background(Color color) {
        this.color = color;
        this.texture = null;
    }

    /**
     * Creates a background with a texture
     * @param texture Texture path
     */
    public Background(String texture) {
        this.texture = texture;
        this.color = null;
    }

    public JsonObject toJson() throws IllegalStateException {
        JsonObject background = new JsonObject();
        background.addProperty(KEY_SERIALIZE_VERSION, BACKGROUND_SERIALIZE_VERSION);
        if (isSolidColor() && color != null)
            background.addProperty(KEY_COLOR, color.toHexString());
        else if(!isSolidColor() && texture != null)
            background.addProperty(KEY_TEXTURE, texture);
        else
            throw new IllegalStateException("Background must have either a color or a texture");

        return background;
    }

    public boolean isSolidColor() {
        return color != null;
    }

    public static Background fromJson(JsonObject json) throws IllegalArgumentException {
        int version = json.has(KEY_SERIALIZE_VERSION) ? json.get(KEY_SERIALIZE_VERSION).getAsInt() : 1; // If it has version, get it, else assume version 1
        return deserialize(json, version);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static Background deserialize(JsonObject json, int version) throws IllegalArgumentException {
        return switch (version) {
            case 1 -> deserializeV1(json);
            // ... future versions here
            default -> throw new IllegalArgumentException("Unsupported Background serialize version: " + version);
        };
    }

    private static Background deserializeV1(JsonObject json) throws IllegalArgumentException {
        boolean isSolidColor = json.has(KEY_COLOR);
        if (isSolidColor) {
            // Not checking for KEY_COLOR existence here, as isSolidColor already did that
            String colorHex = json.get(KEY_COLOR).getAsString();
            Color color = new Color(colorHex, true);
            return new Background(color);
        } else {
            if (!json.has(KEY_TEXTURE))
                throw new IllegalArgumentException("Background JSON missing required key: " + KEY_TEXTURE); // Safeguard

            String texture = json.get(KEY_TEXTURE).getAsString();
            return new Background(texture);
        }
    }
}