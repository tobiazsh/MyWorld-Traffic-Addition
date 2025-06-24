package at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements;

import at.tobiazsh.myworld.traffic_addition.Utils.Elements.GroupElement;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupElementClient extends GroupElement implements ClientElementInterface {

    private final List<ClientElementInterface> clientElements;
    private boolean expanded = false;

    public GroupElementClient(float x, float y, float width, float height, float rotation, UUID id, UUID parentId) {
        super(x, y, width, height, rotation, parentId, id);
        clientElements = new CopyOnWriteArrayList<>();
    }

    @Override
    public void renderImGui(float scale) {
        this.getClientElements().reversed().forEach(element -> element.renderImGui(scale));
    }

    // NOT NECESSARY
    @Override
    public void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

    }

    public List<ClientElementInterface> getClientElements() {
        return clientElements;
    }

    public List<ClientElementInterface> unpackClient() {
        List<ClientElementInterface> unpacked = new ArrayList<>();

        clientElements.forEach(e -> {
            if (e instanceof GroupElementClient groupElementClient) {
                unpacked.addAll(groupElementClient.unpackClient());
            } else {
                unpacked.add(e);
            }
        });

        return unpacked;
    }

    public void addClientElement(ClientElementInterface element) {
        element.setParentId(this.getId());
        clientElements.add(element);
        setBounds();
    }

    public void addClientElement(int index, ClientElementInterface element) {
        element.setParentId(this.getId());
        clientElements.add(index, element);
        setBounds();
    }

    public void addClientElementFirst(ClientElementInterface element) {
        element.setParentId(this.getId());
        clientElements.addFirst(element);
        setBounds();
    }

    public void addAllElements(List<ClientElementInterface> elements) {
        elements.forEach(e -> e.setParentId(this.getId()));
        clientElements.addAll(elements);
        setBounds();
    }

    public void addAllElements(int index, List<ClientElementInterface> elements) {
        elements.forEach(e -> e.setParentId(this.getId()));
        clientElements.addAll(index, elements);
        setBounds();
    }

    public void setClientElements(List<ClientElementInterface> elements) {
        clientElements.clear();
        elements.forEach(e -> e.setParentId(this.getId()));
        clientElements.addAll(elements);
        setBounds();
    }

    public void removeClientElement(ClientElementInterface element) {
        clientElements.remove(element);
        setBounds();
    }

    public void removeClientElement(int index) {
        clientElements.remove(index);
        setBounds();
    }

    @Override
    public void setFactor(float factor) {
        super.setFactor(factor);
        clientElements.forEach(e -> e.setFactor(factor)); // Update factor for all client elements
        calculateBounds(); // Recalculate bounds after factor change
    }

    @Override
    public float[] calculateBounds() {
        if (clientElements.isEmpty()) return new float[]{0, 0, 0, 0};

        ClientElementInterface firstElement = clientElements.getFirst();
        float minX = firstElement.getX();
        float minY = firstElement.getY();
        float maxX = firstElement.getX() + firstElement.getWidth();
        float maxY = firstElement.getY() + firstElement.getHeight();

        for (int i = 1; i < clientElements.size(); i++) {
            ClientElementInterface element = clientElements.get(i);
            minX = Math.min(minX, element.getX());
            minY = Math.min(minY, element.getY());
            maxX = Math.max(maxX, element.getX() + element.getWidth());
            maxY = Math.max(maxY, element.getY() + element.getHeight());
        }

        return new float[]{minX, minY, maxX, maxY};
    }

    @Override
    public void setBounds() {
        float[] bounds = this.calculateBounds();

        super.setX(bounds[0]);
        super.setY(bounds[1]);
        super.setWidth(bounds[2] - bounds[0]);
        super.setHeight(bounds[3] - bounds[1]);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void onPaste() {
        getClientElements().forEach(e -> e.setFactor(ClientElementManager.getInstance().getPixelOfOneBlock()));
        getClientElements().stream().filter(e -> e instanceof GroupElementClient).forEach(ClientElementInterface::onPaste);
        setBounds();
    }

    @Override
    public void onImport() {
    }

    @Override
    public ClientElementInterface copy() {
        GroupElementClient copy = new GroupElementClient(x, y, width, height, rotation, null, parentId); // ID null because it'll be registered when added to the render list
        copy.setName(this.getName());
        copy.setColor(this.getColor());
        copy.setFactor(this.getFactor());

        clientElements.forEach(e -> copy.addClientElement(e.copy()));

        return copy;
    }

    @Override
    public void setX(float newX) {
        clientElements.forEach(e -> e.setX(e.getX() + newX - this.x)); // Update X for all client elements
        this.x = newX;
    }

    @Override
    public void setY(float newY) {
        clientElements.forEach(e -> e.setY(e.getY() + newY - this.y)); // Update Y for all client elements
        this.y = newY;
    }

    @Override
    public void setHeight(float height) {
        float oldHeight = getHeight();
        if (oldHeight == 0) return; // Prevent division by zero
        float scale = height / oldHeight; // ik, it's a duplicate; Ion care tbh

        float originY = this.getY(); // Calc origin (Top-left corner of the group)

        //Scale element's height and adjust their positions accordingly
        clientElements.forEach(element -> {
            element.setHeight(element.getHeight() * scale); // Update element height

            float relativeY = element.getY() - originY; // Distance from origin
            element.setY(originY + relativeY * scale); // Adjusted position
        });

        super.setHeight(height); // Do so for the super class
    }

    @Override
    public void setWidth(float width) {
        float oldWidth = getWidth();
        if (oldWidth == 0) return; // Prevent division by zero
        float scale = width / oldWidth; // ik, it's a duplicate; Ion care tbh

        float originX = this.getX(); // Calc origin (Top-left corner of the group)

        //Scale element's width and adjust their positions accordingly
        clientElements.forEach(element -> {
            element.setWidth(element.getWidth() * scale); // Update element width

            float relativeX = element.getX() - originX; // Distance from origin
            element.setX(originX + relativeX * scale); // Adjusted position
        });

        super.setWidth(width); // Do so for the super class
    }
}
