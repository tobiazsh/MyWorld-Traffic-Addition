package at.tobiazsh.myworld.traffic_addition.Utils.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupElement extends BaseElement {

    private List<BaseElement> elements = new CopyOnWriteArrayList<>();
    private boolean expanded = false;

    public GroupElement(float x, float y, float width, float height, String parentId) {
        super(x, y, width, height, 1, parentId);
    }

    public GroupElement(float x, float y, float width, float height, float rotation, String parentId) {
        super(x, y, width, height, 1, rotation, parentId);
    }

    public GroupElement(float x, float y, float width, float height, float rotation, String id, String name, String parentId) {
        super(x, y, width, height, rotation, 1, id, new float[]{255, 255, 255, 255}, name, parentId);
    }

    public GroupElement(float x, float y, float width, float height, float rotation, String name, String parentId) {
        super(x, y, width, height, rotation, 1, new float[]{255, 255, 255, 255}, name, parentId);
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

    // Nullpoint = Lowest x and y value of all elements
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

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setChildrenParentElementId() {
        this.elements.forEach(element -> element.setParentId(this.id));
    }

    @Override
    public BaseElement setWidth(float width) {
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

        // Update the group's width using the superclass method
        super.setWidth(width);
        return this;
    }

    @Override
    public BaseElement setHeight(float height) {
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
        return this;
    }

    @Override
    public void setX(float x) {
        float nullX = super.getX();

        elements.forEach(element -> element.setX(element.getX() - nullX + x));

        setBounds();
    }

    @Override
    public void setY(float y) {
        float nullY = super.getY();

        elements.forEach(element -> element.setY(element.getY() - nullY + y));
        setBounds();
    }

    @Override
    public void setRotation(float angle) {
        elements.forEach(element -> element.setRotation(element.getRotation() + angle - super.getRotation()));

        super.setRotation(angle);
        setBounds();
    }

    @Override
    public BaseElement copy() {
        GroupElement group = new GroupElement(x, y, width, height, rotation, name, parentId);

        elements.forEach(element -> group.addElement(element.copy()));

        return group;
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
