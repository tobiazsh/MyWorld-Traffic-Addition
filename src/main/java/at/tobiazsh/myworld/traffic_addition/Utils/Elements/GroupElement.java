package at.tobiazsh.myworld.traffic_addition.Utils.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupElement extends BaseElement {

    private List<BaseElement> elements = new CopyOnWriteArrayList<>();

    public GroupElement(float x, float y, float width, float height, float rotation, String name, UUID parentId, UUID id) {
        super(x, y, width, height, 1, rotation, new float[]{255, 255, 255, 255}, name, parentId, id);
    }

    public GroupElement(float x, float y, float width, float height, float rotation, UUID parentId, UUID id) {
        super(x, y, width, height, 1, rotation, new float[]{255, 255, 255, 255}, parentId, id);
    }

    public List<BaseElement> getElements() {
        return elements;
    }

    public void setElements(List<BaseElement> elements) {
        this.elements = elements;
        setChildrenParentElementId();
        setBounds();
    }

    public void addElement(BaseElement element) {
        elements.add(element);
        setChildrenParentElementId();
        setBounds();
    }

    public void addElement(int index, BaseElement element) {
        elements.add(index, element);
        setChildrenParentElementId();
    }

    public void removeElement(BaseElement element) {
        elements.remove(element);
        setBounds();
    }

    public void removeElement(int index) {
        BaseElement elementAtIndex = elements.get(index);
        elements.remove(elementAtIndex);
        setBounds();
    }

    public void clearElements() {
        elements.clear();
        setBounds(0, 0);
    }

    public void setElement(int index, BaseElement element) {
        elements.set(index, element);
        setChildrenParentElementId();
    }

    @Override
    public void setFactor(float factor) {
        super.setFactor(factor);
        elements.forEach(element -> element.setFactor(factor));
        calculateBounds();
    }

    public float[] calculateBounds() {
        float minX = 0;
        float minY = 0;
        float maxX = 0;
        float maxY = 0;

        if (elements.isEmpty()) return new float[]{0, 0, 0, 0};

        boolean first = true;
        for (BaseElement element : elements) {

            if (first) {
                minX = element.getX();
                minY = element.getY();
                maxX = element.getX() + element.getWidth();
                maxY = element.getY() + element.getHeight();
                first = false;
                continue;
            }

            if (element.getX() < minX) minX = element.getX();
            if (element.getY() < minY) minY = element.getY();
            if (element.getX() + element.getWidth() > maxX) maxX = element.getX() + element.getWidth();
            if (element.getY() + element.getHeight() > maxY) maxY = element.getY() + element.getHeight();
        }

        return new float[]{minX, minY, maxX, maxY};
    }

    // Null-point = Lowest x and y value of all elements
    public void setBounds() {
        float[] bounds = calculateBounds();

        super.setX(bounds[0]); // X
        super.setY(bounds[1]); // Y
        super.setWidth(bounds[2] - bounds[0]); // Width
        super.setHeight(bounds[3] - bounds[1]); // Height
    }

    public void setBounds(float x, float y) {
        super.setX(x);
        super.setY(y);
    }

    public void setChildrenParentElementId() {
        this.elements.forEach(element -> element.setParentId(this.id));
    }

    /**
     * Returns a list of all children inside combined into one list. Recursively resolves other GroupElements too.
     * @return List of all children
     */
    public List<BaseElement> unpack() {
        List<BaseElement> unpacked = new ArrayList<>();

        elements.forEach(e -> {
            if (e instanceof GroupElement groupElement) {
                unpacked.addAll(groupElement.unpack()); // Recursively unpack GroupElements
            } else {
                unpacked.add(e); // Add non-GroupElement directly
            }
        });

        return unpacked;
    }

    @Override
    public void setWidth(float width) {
        float oldWidth = super.getWidth();
        float scale = width / oldWidth;

        // Calculate origin as the top-left corner of the group (x, y)
        float originX = this.getX();

        // Scale elements' width and adjust their positions proportionally
        elements.forEach(element -> {
            // Update element width
            element.setWidth(element.getWidth() * scale);

            // Adjust element's X position to maintain proportional distance
            float relativeX = element.getX() - originX; // Distance from origin
            element.setX(originX + relativeX * scale);  // Adjusted position
        });

        // Update the group's width
        super.setWidth(width);
    }

    @Override
    public void setHeight(float height) {
        float oldHeight = super.getHeight();
        float scale = height / oldHeight;

        // Calculate origin as the top-left corner of the group (x, y)
        float originY = this.getY();

        // Scale elements' height and adjust their positions proportionally
        elements.forEach(element -> {
            // Update element height
            element.setHeight(element.getHeight() * scale);

            // Adjust element's Y position to maintain proportional distance
            float relativeY = element.getY() - originY; // Distance from origin
            element.setY(originY + relativeY * scale);  // Adjusted position
        });

        // Update the group's height using the superclass method
        super.setHeight(height);
    }

    @Override
    public void setX(float x) {
        float nullX = super.getX();
        this.x = x;
        elements.forEach(element -> element.setX(element.getX() - nullX + x));
    }

    @Override
    public void setY(float y) {
        float nullY = super.getY();
        this.y = y;
        elements.forEach(element -> element.setY(element.getY() - nullY + y));
    }

    @Override
    public void setRotation(float angle) {
        elements.forEach(element -> element.setRotation(element.getRotation() + angle - super.getRotation()));
        super.setRotation(angle);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = super.getJson();

        JsonArray elementsArray = new JsonArray();
        for (BaseElement element : elements) elementsArray.add(element.toJson());
        object.add("Elements", elementsArray);

        object.addProperty("ElementType", ELEMENT_TYPE.GROUP_ELEMENT.ordinal());

        return object;
    }
}
