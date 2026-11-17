package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.sign.elements.GroupElement;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;

import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;

import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupElementClient extends GroupElement implements ClientElementInterface {

    private static @Nullable ImGuiTexture groupIcon = null;
    private static boolean triedCreateTexture = false; // Flag to ensure we only try to create the texture once

    private final List<ClientElementInterface> clientElements;
    private List<ClientElementInterface> clientElementsView = List.of(); // Unmodifiable view of the client elements list
    private Map<ElementId, AutoCloseable> geometryListeners = new HashMap<>();
    private boolean expanded = false;

    public GroupElementClient(
            float x,
            float y,
            float width,
            float height,
            float rotation,
            ElementId id,
            ParentElementId parentId
    ) {
        super(x, y, width, height, rotation, id, parentId);
        clientElements = new CopyOnWriteArrayList<>();
    }

    @Override
    public void renderImGui(float scale) {
        createTextures();
        this.getClientElements().reversed().forEach(element -> element.renderImGui(scale));
    }

    // Was not necessary in older mod versions because Groups were unpacked in CustomizableBlockEntity, but
    // as this does not happen anymore and the texture is stored in a simple CustomizableSignTextureData, we need
    // to render each element of the group element. If performance becomes a problem, unpack all on texture update
    // and store inside CustomizableSignTextureData. Then re-unpack each time the list changes (detected in-class).
    @Override
    public void renderMinecraft(SubmitNodeCollector queue, int indexInList, int csbeHeight, PoseStack matrices, int light, Direction facing) {
        int index = 0;
        for (var elem : clientElements.reversed()) {
            elem.renderMinecraft(queue, indexInList + index, csbeHeight, matrices, light, facing);

            if (elem instanceof GroupElementClient element)
                index += element.clientElements.size();
            else
                index++;
        }
    }

    /**
     * Returns an unmodifiable view of the client elements list.
     */
    public List<ClientElementInterface> getClientElements() {
        return clientElementsView;
    }

    private void onChildrenModified() {
        setBounds();
        clientElementsView = List.copyOf(clientElements); // Update the unmodifiable view
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
        element.setParentId(this.getId().toParent());
        clientElements.add(element);
        initializeElement(element);
        onChildrenModified();
    }

    public void addClientElement(int index, ClientElementInterface element) {
        element.setParentId(this.getId().toParent());
        clientElements.add(index, element);
        initializeElement(element);
        onChildrenModified();
    }

    public void addAllElements(List<ClientElementInterface> elements) {
        elements.forEach(e -> {
            e.setParentId(this.getId().toParent());
            initializeElement(e);
        });

        clientElements.addAll(elements);
        onChildrenModified();
    }

    public void addAllElements(int index, List<ClientElementInterface> elements) {
        elements.forEach(e -> {
            e.setParentId(this.getId().toParent());
            initializeElement(e);
        });

        clientElements.addAll(index, elements);
        onChildrenModified();
    }

    public void setClientElements(List<ClientElementInterface> elements) {
        uninitializeAllElements();
        clientElements.clear();

        elements.forEach(e -> {
            e.setParentId(this.getId().toParent());
            initializeElement(e);
        });

        clientElements.addAll(elements);
        onChildrenModified();
    }

    public void removeClientElement(ClientElementInterface element) {
        clientElements.remove(element);
        uninitializeElement(element);
        onChildrenModified();
    }

    public void removeClientElement(int index) {
        var element = clientElements.remove(index);
        uninitializeElement(element);
        onChildrenModified();
    }

    /**
     * Tells the class that an element has been added so it can register all kinds of required stuff for it.
     */
    private void initializeElement(ClientElementInterface e) {
        geometryListeners.put(e.getId(), e.subscribeGeometryChanged(this::onChildrenModified));
    }

    private void uninitializeAllElements() {
        geometryListeners.clear();
    }

    /**
     * Tells the class that an element has been removed so it can break all ties with the element.
     */
    private void uninitializeElement(ClientElementInterface e) {
        geometryListeners.remove(e.getId());
    }

    @Override
    public void setFactor(float factor) {
        super.setFactor(factor);
        clientElements.forEach(e -> e.setFactor(factor)); // Update factor for all client elements
        onChildrenModified();
    }

    @Override
    public float[] calculateBounds() {
        if (clientElements.isEmpty()) return new float[]{0, 0, 0, 0};

        final float pivotX = 0; // Pivot X in -1...0...1 || TODO: Create variable "pivot" in base element to specify
        final float pivotY = 0; // Pivot Y in -1...0...1 || TODO  the pivot point it should rotate around

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (ClientElementInterface element : clientElements) {
            float x = element.getX();
            float y = element.getY();
            float w = element.getWidth();
            float h = element.getHeight();

            float angle = (float) Math.toRadians(element.getRotation());
            float cos = Math.abs((float) Math.cos(Math.toRadians(angle)));
            float sin = Math.abs((float) Math.sin(Math.toRadians(angle)));

            // Absolute pivot position
            float px = x + w * (pivotX + 1.0f) * 0.5f;
            float py = y + h * (pivotY + 1.0f) * 0.5f;

            // Rectangle center relative to pivot
            float dx = x + w * 0.5f - px;
            float dy = y + h * 0.5f - py;

            // Rotate around pivot
            float cx = px + dx * cos - dy * sin;
            float cy = py + dx * sin - dy * cos;

            // Calc extents
            float extentX = (
                    Math.abs(width * cos) +
                    Math.abs(height * sin)
            ) * 0.5f;

            float extentY = (
                    Math.abs(width * sin) +
                    Math.abs(height * cos)
            ) * 0.5f;

            minX = Math.min(minX, cx - extentX);
            minY = Math.min(minY, cy - extentY);
            maxX = Math.max(maxX, cx + extentX);
            maxY = Math.max(maxY, cy + extentY);
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
        getClientElements().forEach(e -> e.setFactor(ClientElementManager.getInstance().getPixelsPerBlock()));
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

    private void createTextures() {
        if (groupIcon == null && !triedCreateTexture) {
            try {
                groupIcon = ImGuiTextureFactory.fromStream(
                        Objects.requireNonNull(getClass().getResourceAsStream("/assets/myworld_traffic_addition/textures/imgui/icons/group.png")),
                        "Group Icon"
                );
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Could not create group icon texture", e);
            }
        }

        triedCreateTexture = true;
    }

    @Override
    public @Nullable ImGuiTexture getThumbnailTexture() {
        return groupIcon;
    }

    @Override
    public String getGenericTranslatableName() {
        return "Group Element"; // TODO: Add translation support
    }
}
