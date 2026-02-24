package at.tobiazsh.myworld.traffic_addition.data;

import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.utils.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@NullMarked
public class CustomizableSignTextureData {

    public static final int SIGN_DATA_SERIALIZE_VERSION = 2;

    private static final String KEY_SERIALIZE_VERSION = "serializeVersion";
    private static final String KEY_BACKGROUND = "background";
    private static final String KEY_ELEMENTS = "elements";

    private Background background;
    private final CustomSignElementContainer elementContainer;

    public CustomizableSignTextureData(Background background, List<BaseElement> elements) {
        this.background = background;
        this.elementContainer = new CustomSignElementContainer(elements);
    }

    public static CustomizableSignTextureData empty() {
        return new CustomizableSignTextureData(new Background(new Color(0, 0, 0, 0)), List.of()); // Transparent background with no elements
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
     * Returns a copy of the reference of elementContainer
     */
    public CustomSignElementContainer getElementContainer() {
        return elementContainer;
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
    public static CustomizableSignTextureData fromJson(JsonObject jsonObject) throws IllegalArgumentException {
        int version = jsonObject.has(KEY_SERIALIZE_VERSION) ? jsonObject.get(KEY_SERIALIZE_VERSION).getAsInt() : 1; // If it has version, get it, else assume version 1
        return deserialize(jsonObject, version);
    }

    /**
     * Determines whether the input JsonObject is the newest version available
     */
    public static boolean isNewestVersion(JsonObject jsonObject) {
        if (!jsonObject.has(KEY_SERIALIZE_VERSION)) return false;
        return jsonObject.get(KEY_SERIALIZE_VERSION).getAsInt() == SIGN_DATA_SERIALIZE_VERSION;
    }

    /**
     * Deserializes a CustomSignData from a JsonObject of the given version.
     * @param jsonObject The JsonObject to deserialize
     * @param version The version of the JsonObject
     * @return The deserialized CustomSignData
     * @throws IllegalArgumentException If any required fields are missing or any field is invalid
     */
    private static CustomizableSignTextureData deserialize(JsonObject jsonObject, int version) throws IllegalArgumentException {
        return switch(version) {
            case 1 -> deserializeV1(jsonObject);
            case 2 -> deserializeV2(jsonObject);
            // Add future versions here
            default -> throw new IllegalArgumentException("Unsupported CustomSignData serialize version: " + version);
        };
    }

    /**
     * Deserializes a CustomSignData from a JsonObject of version 1. Thrown together from old code parts, not the most
     * beautiful or performant!
     * @apiNote Before version 2, JSON entries are stored with PascalCase. From version 2 onwards, camelCase is used.
     * @param jsonObject The JsonObject to deserialize
     * @return The deserialized CustomSignData
     * @throws IllegalArgumentException If any required fields are missing or any field is invalid
     */
    @SuppressWarnings("Duplicates")
    private static CustomizableSignTextureData deserializeV1(JsonObject jsonObject) throws IllegalArgumentException {
        final String KEY_STYLE = "Style";
        final String KEY_ELEMENTS = "Elements";

        if (!jsonObject.has(KEY_STYLE) || !jsonObject.has(KEY_ELEMENTS))
            throw new IllegalArgumentException("Invalid CustomSignData JSON: Missing required fields");

        String styleString = jsonObject.get("Style").getAsString();

        if (jsonObject.get("Style").getAsString().contains(";")) {
            String[] styleParts = styleString.split("\\*"); // Split each one by '*'
            String firstPart = styleParts[0]; // Get the first part because we only need one here
            String[] splitStyle = firstPart.split(";"); // Split the first part by ';'

            String pathStr = splitStyle[3]; // Get the path
            Path path = Path.of(pathStr).getParent(); // Convert the path to a Path object
            String newStyle = path.toString(); // Convert the relative path to a string
            styleString = newStyle.replace("\\", "/"); // Replace backslashes with forward slashes
        }

        styleString = styleString.startsWith("/") ? styleString.substring(1) : styleString;

        String[] splitStyleString = styleString.split("/");

        String country = splitStyleString[splitStyleString.length - 2];
        String type = splitStyleString[splitStyleString.length - 1];

        type = type.equals("normal") ? "default" : type;
        Background background = new Background(String.format("%s:%s", country, type));
        List<BaseElement> elements = toElements(jsonObject.getAsJsonArray(KEY_ELEMENTS));

        return new CustomizableSignTextureData(background, elements);
    }

    /**
     * Deserializes a CustomSignData from a JsonObject of version 2.
     * @apiNote Previous version used PascalCase for JSON entries. This version uses camelCase.
     * @param jsonObject The JsonObject to deserialize
     * @return The deserialized CustomSignData
     * @throws IllegalArgumentException If any required fields are missing or any field is invalid
     */
    @SuppressWarnings("Duplicates")
    private static CustomizableSignTextureData deserializeV2(JsonObject jsonObject) throws IllegalArgumentException {
        if (!jsonObject.has(KEY_BACKGROUND) || !jsonObject.has(KEY_ELEMENTS)) {
            throw new IllegalArgumentException("Invalid CustomSignData JSON: Missing required fields");
        }

        Background background = Background.fromJson(jsonObject.getAsJsonObject(KEY_BACKGROUND));
        List<BaseElement> elements = toElements(jsonObject.getAsJsonArray(KEY_ELEMENTS));

        return new CustomizableSignTextureData(background, elements);
    }

    /**
     * Deserializes a list of BaseElements from a JsonArray.
     * @param jsonArray The JsonArray to deserialize
     * @apiNote Always assumes the latest version for elements.
     * @return The deserialized list of BaseElements
     */
    private static List<BaseElement> toElements(JsonArray jsonArray) throws IllegalArgumentException {
        List<BaseElement> elements = new ArrayList<>();
        jsonArray.forEach(elementJson -> {
            BaseElement element = BaseElementInterface.fromJson(elementJson.getAsJsonObject());

            if (element == null)
                throw new IllegalArgumentException("Invalid element in CustomSignData JSON: Could not deserialize element"); // Safeguard

            elements.add(element);
        });
        return elements;
    }
}
