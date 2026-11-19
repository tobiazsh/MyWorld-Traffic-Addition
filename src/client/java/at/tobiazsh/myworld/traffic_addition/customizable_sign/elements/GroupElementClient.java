package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.sign.elements.GroupElement;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;

import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImGui;
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


    /// Preview of the element itself. Useful when you want the user to see how the transformation looks without
    /// transforming it yet.
    private @Nullable ClientElementInterface preview = null;

    private boolean manipulatingChildren = false; // Used for suppression of events while manipulating children

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
        // TODO: REMOVE DEBUG SHIT
        float originX = ImGui.getCursorScreenPosX();
        float originY = ImGui.getCursorScreenPosY();

        var drawList = ImGui.getWindowDrawList();

        int col = ImGui.getColorU32(1f, .2f, .2f, 1f);
        drawList.addRect(
                originX + x * scale,
                originY + y * scale,
                originX + (x + width) * scale,
                originY + (y + height) * scale,
                col,
                0f,
                0,      // flags
                4f      // thickness
        );

        createTextures();
        this.getClientElements().reversed().forEach(element -> element.renderImGui(scale));

        if (this.preview != null) // Guard technically not necessary, but makes it more obvious
            renderPreview(scale);
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
        if (manipulatingChildren) return; // Do not do anything while we're manually manipulating children
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

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (ClientElementInterface element : clientElements) {
            float x = element.getX();
            float y = element.getY();
            float w = element.getWidth();
            float h = element.getHeight();

            double angle = Math.toRadians(element.getRotation());
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Absolute pivot position
            float px = x + w * (pivotX + 1.0f) * 0.5f;
            float py = y + h * (pivotY + 1.0f) * 0.5f;

            // Rectangle center relative to pivot
            float dx = x + w * 0.5f - px;
            float dy = y + h * 0.5f - py;

            // Rotate center around pivot (signed cos/sin)
            float cx = px + dx * cos - dy * sin;
            float cy = py + dx * sin + dy * cos;

            // Extents of the rotated rectangle (abs only here)
            float absCos = Math.abs(cos);
            float absSin = Math.abs(sin);
            float extentX = (w * absCos + h * absSin) * 0.5f;
            float extentY = (w * absSin + h * absCos) * 0.5f;

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
        GroupElementClient copy = new GroupElementClient(x, y, width, height, rotation, ElementId.random(), parentId);
        copy.setName(this.getName());
        copy.setColor(this.getColor());
        copy.setFactor(this.getFactor());

        clientElements.forEach(e -> copy.addClientElement(e.copy()));

        return copy;
    }

    private void manipulateChildren(Runnable r) {
        manipulatingChildren = true;
        try {
            r.run();
        } finally {
            manipulatingChildren = false;
        }
        onChildrenModified();
    }

    @Override
    public void setX(float newX) {
        final float dx = newX - this.x;
        if (dx == 0) return;
        this.x = newX;
        manipulateChildren(() -> clientElements.forEach(e -> e.setX(e.getX() + dx)));
        // X will additionally be updated in calculateBounds because of the callback
    }

    @Override
    public void setY(float newY) {
        final float dy = newY - this.y;
        if (dy == 0) return;
        this.y = newY;
        manipulateChildren(() -> clientElements.forEach(e -> e.setY(e.getY() + dy)));
        // Y will additionally be updated in calculateBounds because of the callback
    }

    @Override
    public void setHeight(float height) {
        float oldHeight = getHeight();
        if (oldHeight <= 0) return; // Prevent division by zero
        float scale = height / oldHeight; // ik, it's a duplicate; Ion care tbh

        float originY = this.getY(); // Calc origin (Top-left corner of the group)

        manipulateChildren(() -> clientElements.forEach(element -> {
            element.setY(originY + (element.getY() - originY) * scale);
            element.setHeight(element.getHeight() * scale); // Update element height
        }));

        // No need to set height as height is determined by children (even callback calculates values for us)
    }

    @Override
    public void setWidth(float width) {
        final float oldWidth = getWidth();
        if (oldWidth <= 0) return; // Prevent division by zero
        float scale = width / oldWidth; // ik, it's a duplicate; Ion care tbh

        float originX = this.getX(); // Calc origin (Top-left corner of the group)

        manipulateChildren(() -> clientElements.forEach(element -> {
            element.setX(originX + (element.getX() - originX) * scale);
            element.setWidth(element.getWidth() * scale); // Update element height
        }));

        // No need to set height as height is determined by children (even callback calculates values for us)
    }

    @Override
    public void setSize(float width, float height) {
        // Calls the event callback twice but I think that's an acceptable trade-off for such clean code
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void setPosition(float x, float y) {
        // Calls the event callback twice but I think that's an acceptable trade-off for such clean code
        setX(x);
        setY(y);
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
    public void usePreview(boolean force) {
        if (this.preview != null && !force)
            return;

        this.preview = this.copy();
    }

    @Override
    public void destroyPreview() {
        this.preview = null;
    }

    /**
     * Returns the preview of the element. A preview is basically a copy of the element (and, as for the group element,
     * its children too), which can be modified, without having to add an extra entry to the client element list and
     * without modifying the actual element. It will be rendered on top of the actual element, while still rendering
     * the original element itself.
     * @return Either null, if no preview as been created, or the preview itself.
     */
    @Override
    public @Nullable ClientElementInterface getPreview() {
        return preview;
    }

    @Override
    public void renderPreview(float scale) {
        if (this.preview == null)
            return;

        this.preview.renderImGui(scale);
    }

    @Override
    public String getGenericTranslatableName() {
        return "Group Element"; // TODO: Add translation support
    }
}
