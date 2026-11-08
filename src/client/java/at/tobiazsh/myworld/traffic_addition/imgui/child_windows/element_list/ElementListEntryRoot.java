package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiColor;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElementListEntryRoot implements ListEntryParent {

    private final ElementSelectionCallback selectionCallback;

    private final CopyOnWriteArrayList<ElementListEntry> children;
    private final HashMap<ElementId, ElementListEntry> entries = new HashMap<>(); // All entries, including children of groups, for easy lookup
    private final Set<ElementId> selectedIds = new HashSet<>();

    private final String dragDropDataType;
    private final ListEntryCreator listEntryCreator;

    private final ClientElementManager clientElementManager;

    private final float dropSpaceHeight = 10;
    private final float[] dropSpaceColor = new float[]{1f, 1f, 1f, 0.2f};

    public ElementListEntryRoot(String id, ClientElementManager manager, ElementSelectionCallback selectionCallback) {
        this.dragDropDataType = "ListEntryDD_" + id;
        this.selectionCallback = selectionCallback;

        if (dragDropDataType.length() > 32) {
            throw new RuntimeException(
                    "DragDropDataType exceeds 32 bytes: " + dragDropDataType + "\nShorten the id to avoid this error."
            );
        }

        this.listEntryCreator = new ListEntryCreator(
                this,
                this.dragDropDataType,
                this::onSelect,
                this::onNewEntry,
                this::onEntryRemoved,
                this::listEntryLookup
        );

        this.clientElementManager = manager;

        this.children = new CopyOnWriteArrayList<>();
        refresh();
    }

    private void onSelect(ElementId id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }

        selectionCallback.getSelectedIds(Set.copyOf(selectedIds));
    }

    private void onNewEntry(ElementListEntry child) {
        entries.put(child.getId(), child);
    }

    private void onEntryRemoved(ElementListEntry child) {
        selectedIds.remove(child.getId());
        entries.remove(child.getId());
    }

    private ElementListEntry listEntryLookup(ElementId id) {
        return entries.get(id);
    }

    public void render() {
        int dropSpaceIndex = 0;

        renderDropSpace(dropSpaceIndex);

        for (ElementListEntry child : children) {
            child.render(selectedIds);
            dropSpaceIndex++;
            renderDropSpace(dropSpaceIndex);
        }
    }

    /**
     * Refreshes the list of children based on the current elements in the provided ClientElementManager.
     */
    @Override
    public void refresh() {
        var elementChildren = this.clientElementManager.getElements();

        Map<ElementId, ElementListEntry> existingEntries = children.stream()
                .collect(Collectors.toMap(
                        ElementListEntry::getId,
                        Function.identity()
                ));

        List<ElementListEntry> refreshedChildren = new ArrayList<>(elementChildren.size());

        for (ClientElementInterface element : elementChildren) {
            var childEntry = existingEntries.remove(element.getId());

            if (childEntry == null) {
                childEntry = listEntryCreator.create(element);
                onNewEntry(childEntry);
            }

            refreshedChildren.add(childEntry);
        }

        for (ElementListEntry removedEntry : existingEntries.values()) {
            onEntryRemoved(removedEntry);
        }

        for (ElementListEntry child : children) {
            if (child instanceof ListEntryParent parent) {
                parent.refresh();
            }
        }

        children.clear();
        children.addAll(refreshedChildren);
    }

    public void renderDropSpace(int index) {
        ImGui.pushID("DropSpace" + index);
        try {
            float maxWidth = ImGui.getContentRegionAvailX();
            ImVec2 cursorPos = ImGui.getCursorScreenPos();
            ImDrawList drawList = ImGui.getWindowDrawList();

            ImVec2 rectMin = new ImVec2(cursorPos.x, cursorPos.y);
            ImVec2 rectMax = new ImVec2(cursorPos.x + maxWidth, cursorPos.y + dropSpaceHeight);

            drawList.addRectFilled(rectMin, rectMax, ImGuiColor.toHexARGB(dropSpaceColor));
            ImGui.dummy(new ImVec2(maxWidth, dropSpaceHeight));

            if (ImGui.beginDragDropTarget()) {
                try {
                    var payload = ImGui.acceptDragDropPayload(dragDropDataType, ElementListEntryDragDropPayload.class);

                    if (payload == null)
                        return;

                    var entry = listEntryLookup(payload.elementEntryId());
                    int elementIndex = this.clientElementManager.indexOfElement(entry.element);

                    if (elementIndex == -1) {
                        MyWorldTrafficAddition.LOGGER.error("Element not found in ClientElementManager: {}", entry.element.getId());
                        return;
                    }

                    if (elementIndex == index)
                        return;

                    this.clientElementManager.moveElement(elementIndex, index);
                } finally {
                    ImGui.endDragDropTarget();
                }
            }
        } finally {
            ImGui.popID();
        }
    }

    /**
     * Returns an immutable copy of the currently selected element IDs.
     */
    public Set<ElementId> getSelectedIds() {
        return Set.copyOf(selectedIds);
    }

    /**
     * Removes a child entry from the root list. Does not inform ClientElementManager, as this is only for the UI
     * representation of the elements.
     * @param child The child entry to remove.
     */
    @Override
    public void removeChild(ElementListEntry child) {
        children.remove(child);
        // Do not update selection here. Items are only removed from the list, not entirely deleted.
        // It may still exist somewhere (maybe in a group)

        // Selection will also be updated on onElementRemoved()!
    }

    /**
     * Deletes a child entry from the root list. Also deletes the corresponding element from the ClientElementManager.
     * @param child The child entry to delete.
     */
    @Override
    public void deleteChild(ElementListEntry child) {
        this.clientElementManager.removeElement(child.element);
        // Manually removing from list is not necessary, as ClientElementManager will trigger a refresh of the list if changes occur

    }

    /**
     * Adds a child entry to the root list. Does not inform ClientElementManager, as this is only for the UI
     * representation of the elements.
     * @param child The child entry to add.
     */
    @Override
    public void addChild(ElementListEntry child) {
        children.add(child);
    }

    @Override
    public ElementId getId() {
        return BaseElementInterface.MAIN_CANVAS_ID.toChild();
    }
}
