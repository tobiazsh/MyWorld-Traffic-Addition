package at.tobiazsh.myworld.traffic_addition.utils.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.BasicFont;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public interface BaseElementInterface {

    // ------------- STATIC VARIABLES ------------------------------------------------

    UUID MAIN_CANVAS_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");


    // ------------- METHODS --------------------------------------------------------

    UUID getId();
    void setId(UUID id);

    UUID getParentId();
    void setParentId(UUID parentId);

    void setClicked(boolean clicked);

    void setPosition(float x, float y);

    void setX(float x);
    void setY(float y);

    float getX();
    float getY();

    // Return Vec2f for easier implementation in client element (mainly GroupElement)
    void setSize(float width, float height);

    // Return width for easier implementation in client element (mainly GroupElement)
    void setWidth(float width);
    // Return height for easier implementation in client element (mainly GroupElement)
    void setHeight(float height);

    float getWidth();
    float getHeight();

    float calcBlocks(float value);

    BaseElement scalePercentSize(float percent);

    void scaleSize(float pixels, boolean scaleByHeight);

    BaseElement scaleHeight(float factor);
    BaseElement scaleWidth(float factor);

    String getName();
    void setName(String name);

    float getFactor();
    void setFactor(float factor);

    float getRotation();
    void setRotation(float rotation);

    float[] getColor();
    void setColor(float[] color);

    JsonObject getJson();

    JsonObject toJson();

    // -------------- STATIC METHODS ------------------------------------------------------

    static float calcBlocks(float value, float scale) {
        return value / scale;
    }

    Map<String, UUID> uuidMigrationMap = new HashMap<>(); // For migration purposes, to map old IDs to new ones

    /**
     * Converts a JSON Object to an element of the type specified in the json object
     * @param object The JSON Object to convert
     * @return The converted Object inherited from BaseElement
     */
    static BaseElement fromJson(JsonObject object) {
        BaseElement element;

        // Array

        JsonArray jsonColor = object.getAsJsonArray("Color");
        float[] color = new float[]{jsonColor.get(0).getAsFloat(), jsonColor.get(1).getAsFloat(), jsonColor.get(2).getAsFloat(), jsonColor.get(3).getAsFloat()}; // R, G, B, A

        JsonArray jsonSize = object.getAsJsonArray("Size");
        float[] size = new float[]{jsonSize.get(0).getAsFloat(), jsonSize.get(1).getAsFloat()}; // Width, Height;

        JsonArray jsonElementPosition = object.getAsJsonArray("ElementPosition");
        float[] elementPosition = new float[]{jsonElementPosition.get(0).getAsFloat(), jsonElementPosition.get(1).getAsFloat()}; // X, Y

        // Non-Array

        float rotation = object.get("Rotation").getAsFloat();
        float factor = object.get("Factor").getAsFloat();
        String name = object.get("Name").getAsString();

        JsonElement idObj = object.get("Id");

        if (idObj == null) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't read element with name {} from JSON because it has no ID!", name);
            return null;
        }

        String idStr = object.get("Id").getAsString();
        UUID id;

        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            MyWorldTrafficAddition.LOGGER.debug("Couldn't read element with name {} from JSON because the ID {} is not a valid UUID! Generating one!", name, idStr);
            id = UUID.randomUUID(); // Generate a new ID if the old one is not valid
            if (!Objects.equals(idStr, "null")) uuidMigrationMap.put(idStr, id); // Store the old ID to new ID mapping for migration purposes
        }

        BaseElement.ELEMENT_TYPE type = BaseElement.ELEMENT_TYPE.values()[object.get("ElementType").getAsInt()];

        switch(type) {
            case IMAGE_ELEMENT -> element = new ImageElement(
                    elementPosition[0], elementPosition[1],
                    size[0], size[1],
                    factor,
                    rotation,
                    object.get("Texture").getAsString(),
                    MAIN_CANVAS_ID,
                    id
            );

            case TEXT_ELEMENT -> element = new TextElement(
                    elementPosition[0], elementPosition[1],
                    size[0], size[1],
                    rotation,
                    factor,

                    new BasicFont(
                            object.get("FontPath").getAsString(),
                            object.get("FontSize").getAsFloat()
                    ),

                    object.get("Text").getAsString(),
                    false,
                    MAIN_CANVAS_ID,
                    id
            );

            case GROUP_ELEMENT -> {
                element = new GroupElement(
                        elementPosition[0], elementPosition[1],
                        size[0], size[1],
                        rotation,
                        name,
                        MAIN_CANVAS_ID,
                        id
                );

                JsonArray elementsArray = object.getAsJsonArray("Elements");
                for (int i = 0; i < elementsArray.size(); i++) {
                    JsonObject elementObject = elementsArray.get(i).getAsJsonObject();
                    BaseElement childElement = fromJson(elementObject);

                    if (childElement == null) {
                        MyWorldTrafficAddition.LOGGER.error("Couldn't recognize ChildElement with name {} from GroupElement with ID {} because it could not be read from JSON! It is likely that no ID for this element has been found and thus couldn't target the right element!", elementObject.get("Name").getAsString(), id);
                        continue;
                    }

                    ((GroupElement) element).addElement(childElement);
                }

                ((GroupElement) element).setChildrenParentElementId();
            }

            case ONLINE_IMAGE_ELEMENT -> element = new OnlineImageElement(
                    elementPosition[0], elementPosition[1],
                    size[0], size[1],
                    factor,
                    rotation,
                    UUID.fromString(object.get("PictureReference").getAsString()),
                    MAIN_CANVAS_ID,
                    id
            );

            default -> {
                MyWorldTrafficAddition.LOGGER.error("Error: Couldn't deconstruct elements to JSON! Element type is invalid.");
                return null;
            }
        }

        // Migration from old codebase (old codebase didn't have the parent id stored; introduced because of ungrouping)
        // Another migration happening here: Old elements used String as their ID, new elements use UUID.
        if (object.has("ParentId")) {
            String parentIdStr = object.get("ParentId").getAsString();
            UUID parentId;

            try {
                parentId = UUID.fromString(parentIdStr);
            } catch (IllegalArgumentException e) {
                MyWorldTrafficAddition.LOGGER.error("Couldn't read element with name {} from JSON because the ParentId {} is not a valid UUID!", name, parentIdStr);

                if (uuidMigrationMap.containsKey(parentIdStr)) {
                    parentId = uuidMigrationMap.get(parentIdStr); // Use the migrated ID if available
                } else {
                    MyWorldTrafficAddition.LOGGER.warn("Using default MAIN_CANVAS_ID for element with name {} due to invalid ParentId.", name);
                    parentId = MAIN_CANVAS_ID; // Default to MAIN_CANVAS_ID if the parent ID is not valid
                }
            }

            element.setParentId(parentId);
        }

        element.setName(name);
        element.setColor(color);

        return element;
    }

    /**
     * Unpacks all elements recursively. Kinda like a zip file
     * @param elements The list of all elements
     * @return The unpacked elements
     */
    static List<BaseElement> unpackList(List<BaseElement> elements) {
        List<BaseElement> resolvedElements = new ArrayList<>();

        for (BaseElement element : elements) {
            if (element instanceof GroupElement)
                resolvedElements.addAll(((GroupElement) element).unpack());
            else
                resolvedElements.add(element);
        }

        return resolvedElements;
    }

}
