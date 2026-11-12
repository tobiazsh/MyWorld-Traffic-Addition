package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.tr;

public class ElementListEntry {

    protected final ClientElementInterface element;
    protected final ElementId id;
    protected final String idStr;
    protected final ImString elementName = new ImString(512);
    protected final String dragDropDataType;
    protected final ListEntryParent parent; // Null if this is a top-level element, otherwise points to the parent group entry

    protected final Consumer<ElementId> onSelect;

    protected final ListEntryLookup listEntryLookup;

    protected final Consumer<ElementListEntry> onNewEntry;
    protected final Consumer<ElementListEntry> onEntryRemoved;

    protected @Nullable ImGuiTexture thumbnailTexture; // Not final because GroupListEntry overrides it with a different texture

    protected int previewSize = 50;
    protected boolean overrideSelectableHeight = false; // So that the selectable height does not expand over children in e.g. group entries.
    protected int selectableHeight = 0;

    protected static @Nullable ImGuiTexture genericThumbnailTexture;

    public ElementListEntry(
            ClientElementInterface element,
            ElementId id,
            String dragDropDataType,
            ListEntryParent parent,
            Consumer<ElementId> onSelect,
            Consumer<ElementListEntry> onNewEntry,
            Consumer<ElementListEntry> onEntryRemoved,
            ListEntryLookup listEntryLookup
    ) {
        this.element = element;
        this.id = id;
        this.idStr = id.toString();
        this.dragDropDataType = dragDropDataType;
        this.thumbnailTexture = element.getThumbnailTexture();
        this.elementName.set(element.getName());
        this.parent = parent;

        this.onSelect = onSelect;

        this.listEntryLookup = listEntryLookup;

        this.onNewEntry = onNewEntry;
        this.onEntryRemoved = onEntryRemoved;
    }

    public ElementId getId() {
        return id;
    }

    public ClientElementInterface getElement() {
        return element;
    }

    public void render(
        Set<ElementId> selectedIds
    ) {
        ImGui.pushID(idStr);
        try {
            boolean isSelected = selectedIds.contains(id);

            ImVec2 startPos = ImGui.getCursorPos();
            float spacingY = ImGui.getStyle().getItemSpacingY();

            renderContent(selectedIds);

            ImVec2 endPos = ImGui.getCursorPos();

            if (!overrideSelectableHeight)
                selectableHeight = (int) (endPos.y - startPos.y - spacingY);

            ImGui.setCursorPos(startPos);

            ImGui.selectable(
                    "##" + id,
                    isSelected,
                    ImGuiSelectableFlags.AllowOverlap | ImGuiSelectableFlags.SpanAllColumns,
                    new ImVec2(0, selectableHeight)
            );

            renderContextMenu(selectedIds);

            ImGui.setCursorPosY(endPos.y);

            if (ImGui.isItemHovered()) {
                if (ImGui.isMouseClicked(ImGuiMouseButton.Left))
                    onSelect.accept(id);
                else if (ImGui.isMouseClicked(ImGuiMouseButton.Right))
                    ImGui.openPopup("###context_menu");

                if (ImGui.beginDragDropSource()) {
                    sendDragDropPayload();
                    ImGui.endDragDropSource();
                }
            }

            if (ImGui.beginDragDropTarget()) {
                acceptDragDropPayload();
                ImGui.endDragDropTarget();
            }
        } finally {
            ImGui.popID();
        }
    }

    protected void renderContextMenu(Set<ElementId> selectedIds) {
        if (ImGui.beginPopupContextItem("###context_menu")) {
            renderContextMenuContent(selectedIds);
            ImGui.endPopup();
        }
    }

    protected void renderContextMenuContent(Set<ElementId> selectedIds) {
        if (ImGui.menuItem("Delete")) {
            parent.deleteChild(this);
        }

        if (ImGui.menuItem("Cut")) {

        }

        if (ImGui.menuItem("Copy")) {

        }

        if (ImGui.menuItem("Duplicate")) {
            var copy = element.copy();
            parent.addElementChild(copy, 0);
        }

        if (ImGui.menuItem("Group selected")) {
            groupSelected(selectedIds);
        }

        if (ImGui.menuItem("Group with below")) {
            groupWithBelow();
        }

        if (ImGui.beginItemTooltip()) {
            ImGui.text("Group with below. Won't do anything if there is no element below.");
            ImGui.endTooltip();
        }

        if (parent instanceof GroupingElement groupingElement && ImGui.menuItem("Free from group")) {
            groupingElement.removeFromGroup(this);
        }
    }

    private void groupWithBelow() {
        int index = listEntryLookup.getIndexOfEntry(id);
        int indexBelow = index + 1;
        var elementBelow = listEntryLookup.getEntryByIndex(indexBelow);

        // TODO: If you want to make this any better, you could disable the menu item if there is no element below
        //  Doing this simply with listEntryLookup.getIndexOfEntry(id) is too expensive. Must figure out a way to
        //  get the current index more efficiently to achieve this behaviour.
        if (elementBelow.isEmpty())
            return;

        parent.deleteChild(this);
        elementBelow.get().parent.deleteChild(elementBelow.get());

        float maxX = Math.max(this.element.getX() + this.element.getWidth(), elementBelow.get().element.getX() + elementBelow.get().element.getWidth());
        float maxY = Math.max(this.element.getY() + this.element.getHeight(), elementBelow.get().element.getY() + elementBelow.get().element.getHeight());
        float minX = Math.min(this.element.getX(), elementBelow.get().element.getX());
        float minY = Math.min(this.element.getY(), elementBelow.get().element.getY());

        var group = new GroupElementClient(
                minX,
                minY,
                maxX - minX,
                maxY - minY,
                0f,
                ElementId.random(),
                parent.getId().toParent()
        );

        group.setParentId(parent.getId().toParent());
        group.addClientElement(this.element);
        group.addClientElement(elementBelow.get().element);

        parent.addElement(group);
        // No need to manually refresh, as the above call to addElement will trigger a refresh of the list of children
        // in the parent. 'Cause addElement() will add the group to the ClientElementManager, which has a callback to
        // refresh the list of children in the parent.
    }

    /**
     * Groups selected elements. Does not guarantee that this element will be part of the group.
     * <p>All selected elements must have the same parent, otherwise the grouping will not be performed.
     * @param selectedIds The set of selected element IDs to group together.
     */
    private void groupSelected(Set<ElementId> selectedIds) {
        var elements = selectedIds.stream()
                .map(listEntryLookup::getEntryById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        if (!parentMatches(elements)) {
            // TODO: Information to user that elements must have the same parent to be grouped together
            return;
        }

        elements.forEach(entry -> entry.parent.deleteChild(entry));

        float maxX = (float) elements.stream()
                .mapToDouble(entry -> entry.element.getX() + entry.element.getWidth())
                .max().orElse(0d);

        float minX = (float) elements.stream()
                .mapToDouble(entry -> entry.element.getX())
                .min().orElse(0d);

        float maxY = (float) elements.stream()
                .mapToDouble(entry -> entry.element.getY() + entry.element.getHeight())
                .max().orElse(0d);

        float minY = (float) elements.stream()
                .mapToDouble(entry -> entry.element.getY())
                .min().orElse(0d);

        // TODO: Respect the rotation of the elements when creating the group. Make a method "getBoundingBox" in
        //  base element interface.

        var group = new GroupElementClient(
                minX,
                minY,
                maxX - minX,
                maxY - minY,
                0f,
                ElementId.random(),
                parent.getId().toParent()
        );

        group.setParentId(parent.getId().toParent());
        elements.forEach(entry -> group.addClientElement(entry.element));

        parent.addElementChild(group);
    }

    private static boolean parentMatches(Set<ElementListEntry> entries) {
        if (entries.isEmpty())
            return true;

        var firstParent = entries.iterator().next().parent;

        for (var entry : entries) {
            if (entry.parent != firstParent)
                return false;
        }

        return true;
    }

    protected void renderContent(Set<ElementId> selectedElementIds) {
        if (this.thumbnailTexture == null)
            updateThumbnailTexture();

        float startY = ImGui.getCursorPosY();

        renderPreview();

        float previewHeight = ImGui.getCursorPosY() - startY;
        float framePaddingY = ImGui.getStyle().getItemSpacingY();
        float textHeight = ImGui.getFrameHeight() + framePaddingY;

        ImGui.sameLine();

        ImGui.setCursorPosY(startY + (previewHeight - textHeight) / 2);
        renderName(ImGui.getCursorPosX(), ImGui.getCursorPosY());
        ImGui.setCursorPosY(startY + previewHeight);
    }

    protected void sendDragDropPayload() {
        ImGui.setDragDropPayload(dragDropDataType, new ElementListEntryDragDropPayload(id));
        renderPreview();
        ImGui.sameLine();
        ImGui.text(element.getName());
    }

    protected void acceptDragDropPayload() {
        var payload = ImGui.acceptDragDropPayload(dragDropDataType, ElementListEntryDragDropPayload.class);

        if (payload == null)
            return;

        if (payload.elementEntryId().equals(id))
            return;

        onElementDropped(payload);
    }

    /**
     * Called when an element is dropped onto this entry. Override this method to implement custom behavior, such as
     * creating a group or reordering elements.
     * @param payload The received drag-and-drop payload.
     */
    protected void onElementDropped(@NonNull ElementListEntryDragDropPayload payload) {
        groupWith(payload.elementEntryId());
    }

    private void groupWith(ElementId other) {
        var droppedElementEntry = listEntryLookup.getEntryById(other);

        if (droppedElementEntry.isEmpty())
            return;

        droppedElementEntry.get().parent.deleteChild(droppedElementEntry.get());
        parent.deleteChild(this);

        float minX = Math.min(droppedElementEntry.get().getElement().getX(), this.getElement().getX());
        float minY = Math.min(droppedElementEntry.get().getElement().getY(), this.getElement().getY());
        float maxX = Math.max(droppedElementEntry.get().getElement().getX(), this.getElement().getX());
        float maxY = Math.max(droppedElementEntry.get().getElement().getY(), this.getElement().getY());

        var groupElement = new GroupElementClient(
                minX,
                minY,
                maxX - minX,
                maxY- minY,
                0,
                ElementId.random(),
                parent.getId().toParent()
        );

        parent.addElementChild(groupElement);
    }

    protected void renderPreview() {
        if (genericThumbnailTexture == null)
            createTextures();

        // If specific thumbnail available, render this; otherwise render generic thumbnail
        // If all else fails, render a placeholder text
        if (thumbnailTexture != null && thumbnailTexture.isUsable()) {
            ImGui.image(thumbnailTexture.getTextureId(), previewSize, previewSize);
        } else if (genericThumbnailTexture != null && genericThumbnailTexture.isUsable()) {
            ImGui.image(genericThumbnailTexture.getTextureId(), previewSize, previewSize);
        } else {
            renderNoThumbnailPlaceholder();
        }

        ImGui.setItemTooltip(tr(element.getGenericTranslatableName()));
    }

    protected void updateThumbnailTexture() {
        this.thumbnailTexture = element.getThumbnailTexture();
    }

    protected void renderName(float posX, float posY) {
        ImGui.setCursorPos(posX, posY);

        float width = ImGui.calcTextSizeX(elementName.get()) + 20; // Add some padding for the input box
        ImGui.setNextItemWidth(width);

        if (ImGui.inputText("##nameInput", elementName, ImGuiInputTextFlags.EnterReturnsTrue))
            element.setName(elementName.get());
    }

    protected void renderNoThumbnailPlaceholder() {
        if (ImGui.beginChild("##no_thumbnail", previewSize, previewSize)) {
            ImGui.textWrapped("No thumbnail available");
            ImGui.endChild();
        }
    }

    protected static void createTextures() {
        try {
            genericThumbnailTexture = ImGuiTextureFactory.fromStream(
                    Objects.requireNonNull(ElementListEntry.class.getResourceAsStream(
                            "/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/icons/element_icon.png"
                    )),
                    "generic_element_thumbnail"
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load generic element thumbnail texture", e);
        }
    }

    protected void dispose() {
        if (thumbnailTexture != null)
            thumbnailTexture.dispose();
    }
}
