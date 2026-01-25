package at.tobiazsh.myworld.traffic_addition.data;

import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.utils.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class CustomSignData {

    public static final int SIGN_DATA_SERIALIZE_VERSION = 1;

    private static final String KEY_SERIALIZE_VERSION = "SerializeVersion";
    private static final String KEY_BACKGROUND = "Background";
    private static final String KEY_ELEMENTS = "Elements";

    private Background background;
    private final CustomSignElementContainer elementContainer;

    public CustomSignData(Background background, List<BaseElement> elements) {
        this.background = background;
        this.elementContainer = new CustomSignElementContainer(elements);
    }

    public static CustomSignData empty() {
        return new CustomSignData(new Background(new Color(0, 0, 0, 0)), List.of()); // Transparent background with no elements
    }

    /**
     * Returns the current {@link Background} of the sign.
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Sets the background of the sign.
     * @param background New background to set
     */
    public void setBackground(Background background) {
        this.background = background;
    }


    /**
     * Serializes this CustomSignData to a JsonObject.
     * @return Serialized JsonObject
     */
    public JsonObject toJson() {
        JsonArray serializedElements = new JsonArray();
        elementContainer.forEach(element -> serializedElements.add(element.toJson()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(KEY_SERIALIZE_VERSION, SIGN_DATA_SERIALIZE_VERSION);
        jsonObject.add(KEY_BACKGROUND, background.toJson());
        jsonObject.add(KEY_ELEMENTS, serializedElements);

        return jsonObject;
    }

    /**
     * Deserializes a CustomSignData from a JsonObject.
     * @param jsonObject The JsonObject to deserialize
     * @return The deserialized CustomSignData
     * @throws IllegalArgumentException If any required fields are missing or any field is invalid
     */
    public static CustomSignData fromJson(JsonObject jsonObject) throws IllegalArgumentException {
        int version = jsonObject.has(KEY_SERIALIZE_VERSION) ? jsonObject.get(KEY_SERIALIZE_VERSION).getAsInt() : 1; // If it has version, get it, else assume version 1
        return deserialize(jsonObject, version);
    }

    /**
     * Deserializes a CustomSignData from a JsonObject of the given version.
     * @param jsonObject The JsonObject to deserialize
     * @param version The version of the JsonObject
     * @return The deserialized CustomSignData
     * @throws IllegalArgumentException If any required fields are missing or any field is invalid
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static CustomSignData deserialize(JsonObject jsonObject, int version) throws IllegalArgumentException {
        return switch(version) {
            case 1 -> deserializeV1(jsonObject);
            // ... future versions here
            default -> throw new IllegalArgumentException("Unsupported CustomSignData serialize version: " + version);
        };
    }

    /**
     * Deserializes a CustomSignData from a JsonObject of version 1.
     * @param jsonObject The JsonObject to deserialize
     * @return The deserialized CustomSignData
     * @throws IllegalArgumentException If any required fields are missing or any field is invalid
     */
    private static CustomSignData deserializeV1(JsonObject jsonObject) throws IllegalArgumentException {
        if (!jsonObject.has(KEY_BACKGROUND) || !jsonObject.has(KEY_ELEMENTS)) {
            throw new IllegalArgumentException("Invalid CustomSignData JSON: Missing required fields");
        }

        Background background = Background.fromJson(jsonObject.getAsJsonObject(KEY_BACKGROUND));

        List<BaseElement> elements = new ArrayList<>();
        jsonObject.getAsJsonArray(KEY_ELEMENTS).forEach(elementJson -> {
            BaseElement element = BaseElementInterface.fromJson(elementJson.getAsJsonObject());

            if (element == null)
                throw new IllegalArgumentException("Invalid element in CustomSignData JSON: Could not deserialize element"); // Safeguard

            elements.add(element);
        });

        return new CustomSignData(background, elements);
    }
}
