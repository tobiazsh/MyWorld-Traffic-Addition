package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.clipboard.ElementClipboard;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupListEntry extends ElementListEntry implements ListEntryParent, GroupingElement {

    private static @Nullable ImGuiTexture groupThumbnailTexture;

    private boolean isExpanded = false;
    private final CopyOnWriteArrayList<ElementListEntry> children;

    private final ListEntryCreator listEntryCreator;
    private final GenericDropSpace<ElementListEntryDragDropPayload> dropSpace;

    private static final float DROP_SPACE_HEIGHT = 10;
    private static final int DROP_SPACE_COLOUR = ImGui.getColorU32(1, 1, 1, .0f);

    private final AtomicBoolean shouldRefresh = new AtomicBoolean(false);

    public GroupListEntry(
            ClientElementInterface element,
            ElementId id,
            String dragDropDataType,
            ListEntryParent parent,
            BiConsumer<ElementId, Boolean> onSelect,
            Consumer<ElementListEntry> onNewEntry,
            Consumer<ElementListEntry> onEntryRemoved,
            ListEntryLookup listEntryLookup,
            ElementClipboard clipboard
    ) {
        if (!(element instanceof GroupElementClient groupElement))
            throw new RuntimeException("GroupListEntry can only be created with a GroupElementClient, but got: " + element.getClass().getName());

        super(groupElement, id, dragDropDataType, parent, onSelect, onNewEntry, onEntryRemoved, listEntryLookup, clipboard);
        if (groupThumbnailTexture == null) createGroupThumbnailTexture();
        this.thumbnailTexture = groupThumbnailTexture;

        this.listEntryCreator = new ListEntryCreator(
                this,
                dragDropDataType,
                onSelect,
                onNewEntry,
                onEntryRemoved,
                listEntryLookup,
                clipboard
        );

        this.dropSpace = new GenericDropSpace<>(
                DROP_SPACE_HEIGHT,
                DROP_SPACE_COLOUR,
                dragDropDataType,
                ElementListEntryDragDropPayload.class,
                this::onDrop
        );

        this.children = new CopyOnWriteArrayList<>();

        for (int i = 0; i < groupElement.getClientElements().size(); i++) {
            var child = groupElement.getClientElements().get(i);

            ElementListEntry previousEntry = i > 0 ? children.get(i - 1) : null;

            var entry = new ElementListEntry(
                    child,
                    child.getId(),
                    dragDropDataType,
                    this,
                    onSelect,
                    onNewEntry,
                    onEntryRemoved,
                    listEntryLookup,
                    clipboard
            );

            entry.setPreviousNeighbour(previousEntry);
            children.add(entry);

            if (previousEntry != null)
                previousEntry.setNextNeighbour(entry);
        }

        this.overrideSelectableHeight = true;
    }

    @Override
    protected void renderContextMenuContent(Set<ElementId> selectedIds) {
        super.renderContextMenuContent(selectedIds);

        if (ImGui.menuItem("Dissolve Group")) {
            dissolveGroup();
        }
    }

    protected void dissolveGroup() {
        List<ClientElementInterface> childElements = children.stream()
                .map(ElementListEntry::getElement)
                .toList();

        int groupIndex = listEntryLookup.getIndexOfEntry(getId());

        for (int i = 0; i < childElements.size(); i++) {
            parent.addElementChild(childElements.get(i), groupIndex + i);
        }

        deleteChild(this);
    }

    @Override
    protected void onElementDropped(@NonNull ElementListEntryDragDropPayload payload) {
        var droppedElementEntry = listEntryLookup.getEntryById(payload.elementEntryId());

        if (droppedElementEntry.isEmpty())
            return;

        // Remove the dropped element from its current parent
        droppedElementEntry.get().parent.deleteChild(droppedElementEntry.get());

        // Add the dropped element to this group
        this.addElementChild(droppedElementEntry.get().element);
    }

    @Override
    protected void renderContent(Set<ElementId> selectedElementIds) {
        float startY = ImGui.getCursorPosY();

        renderPreview();

        float previewHeight = ImGui.getCursorPosY() - startY;
        float framePaddingY = ImGui.getStyle().getItemSpacingY();
        float textHeight = ImGui.getFrameHeight() + framePaddingY;

        ImGui.sameLine();

        ImGui.setCursorPosY(startY + (previewHeight - textHeight) / 2);
        renderName(ImGui.getCursorPosX(), ImGui.getCursorPosY());

        final float arrowButtonSize = ImGui.getFontSize() + ImGui.getStyle().getFramePadding().y * 2;
        ImGui.setCursorPosY(startY + (previewHeight - textHeight) / 2);
        ImGui.setCursorPosX(ImGui.getWindowSizeX() - arrowButtonSize - ImGui.getStyle().getItemSpacingX() - ImGui.getStyle().getScrollbarSize());
        if (ImGui.arrowButton("dropDownToggler", isExpanded ? ImGuiDir.Down : ImGuiDir.Left))
            isExpanded = !isExpanded;

        ImGui.setCursorPosY(startY + previewHeight);

        float endY = ImGui.getCursorPosY();
        this.selectableHeight = (int) (endY - startY);

        ImGui.dummy(0, 0);

        if (isExpanded) {
            try {
                ImGui.beginGroup();
                renderChildren(selectedElementIds);
                ImGui.dummy(0, 0);
            } finally {
                ImGui.endGroup();
            }
        }
    }

    private void onDrop(ElementListEntryDragDropPayload payload, int toIndex) {
        var entry = children.stream()
                .filter(child -> child.id == payload.elementEntryId())
                .findFirst();

        if (entry.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.error("Dropped element entry not found: {}", payload.elementEntryId());
            return;
        }

        int fromIndex = this.children.indexOf(entry.get());

        if (toIndex == -1) {
            MyWorldTrafficAddition.LOGGER.error(
                    "Element not found in ClientElementManager: {}",
                    entry.get().element.getId()
            );
            return;
        }

        if (toIndex == fromIndex || toIndex == fromIndex + 1)
            return;

        if (fromIndex < toIndex) {
            toIndex--; // Adjust the target fromIndex if the element is being moved forward in the list
        }

        moveChild(toIndex, fromIndex);
    }

    protected void renderChildren(Set<ElementId> selectedElementIds) {
        ImGui.indent();

        int index = 0;
        dropSpace.renderNew(index);

        for (ElementListEntry child : children) {
            index++;
            child.render(selectedElementIds);
            dropSpace.renderNew(index);
        }

        ImGui.unindent();
    }

    @Override
    protected void preRender() {
        if (shouldRefresh.compareAndSet(true, false))
            refresh();
    }

    private void createGroupThumbnailTexture() {
        try {
            groupThumbnailTexture = ImGuiTextureFactory.fromStream(
                    Objects.requireNonNull(getClass().getResourceAsStream("/assets/myworld_traffic_addition/textures/imgui/icons/group.png")),
                    "group_thumbnail_texture"
            );
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to load group thumbnail texture", e);
        }
    }

    @Override
    public void refresh() {
        var groupElement = (GroupElementClient) element;
        var elementChildren = groupElement.getClientElements();

        Map<ElementId, ElementListEntry> existingEntries = children.stream()
                .collect(Collectors.toMap(
                        ElementListEntry::getId,
                        Function.identity()
                ));

        List<ElementListEntry> refreshedChildren = new ArrayList<>(elementChildren.size());

        for (int i = 0; i < elementChildren.size(); i++) {
            ClientElementInterface element = elementChildren.get(i);
            ElementListEntry childEntry = existingEntries.remove(element.getId());
            ElementListEntry previous = i > 0 ? refreshedChildren.get(i-1) : null;

            if (childEntry == null) {
                childEntry = listEntryCreator.create(element);
                onNewEntry.accept(childEntry);
            }

            childEntry.setPreviousNeighbour(previous);

            if (previous != null)
                previous.setNextNeighbour(childEntry);

            refreshedChildren.add(childEntry);
        }

        for (ElementListEntry removedEntry : existingEntries.values()) {
            onEntryRemoved.accept(removedEntry);
        }

        children.clear();
        children.addAll(refreshedChildren);
    }

    @Override
    public void removeChild(ElementListEntry child) {
        this.children.remove(child);
    }

    @Override
    public void deleteChild(ElementListEntry child) {
        parent.deleteChild(child);
    }

    @Override
    public boolean moveChild(int from, int to) {
        if (!(this.element instanceof GroupElementClient groupElementClient))
            return false;

        var entry = groupElementClient.getClientElements().get(from);
        groupElementClient.removeClientElement(from);
        groupElementClient.addClientElement(to, entry);
        refresh();

        return true;
    }

    @Override
    public void addElement(ClientElementInterface element) {
        this.parent.addElement(element);
    }

    @Override
    public void addElement(ClientElementInterface element, int index) {
        this.parent.addElement(element, index);
    }

    @Override
    public void addElementChild(ClientElementInterface element) {
        if (!(this.element instanceof GroupElementClient groupElementClient))
            return;

        groupElementClient.addClientElement(element);
        // No need to add as child to this list, as the refresh() method will handle that when the group is refreshed.

        // No need to refresh the list of children here, as the group will be refreshed automatically when the element
        // is added to the group.
    }

    @Override
    public void addElementChild(ClientElementInterface element, int index) {
        if (!(this.element instanceof GroupElementClient groupElementClient))
            return;

        groupElementClient.addClientElement(index, element);
        // No need to add as child to this list, as the refresh() method will handle that when the group is refreshed.

        // No need to refresh the list of children here, as the group will be refreshed automatically when the element
        // is added to the group.
    }

    @Override
    public void addElementsChild(List<ClientElementInterface> elements) {
        if (!(this.element instanceof GroupElementClient groupElementClient))
            return;

        groupElementClient.addAllElements(elements);
        // No need to add as children to this list, as the refresh() method will handle that when the group is refreshed.

        // No need to refresh the list of children here, as the group will be refreshed automatically when the elements
        // are added to the group.
    }

    @Override
    public void addElementsChild(List<ClientElementInterface> elements, int index) {
        if (!(this.element instanceof GroupElementClient groupElementClient))
            return;

        groupElementClient.addAllElements(index, elements);
        // No need to add as children to this list, as the refresh() method will handle that when the group is refreshed.

        // No need to refresh the list of children here, as the group will be refreshed automatically when the elements
        // are added to the group.
    }

    @Override
    public void removeFromGroup(ElementListEntry child) {
        if (!(this.element instanceof GroupElementClient groupElementClient))
            return;

        groupElementClient.removeClientElement(child.getElement());
        parent.addElementChild(child.getElement());
    }

    @Override
    public void addToGroup(ElementListEntry child) {
        this.addElementChild(child.getElement());
    }

    // DO NOT dispose the groupThumbnailTexture here, as it is shared among all instances of GroupListEntry.
    // It will be disposed automatically when the application exits, as stated by the ImGuiB3D documentation.
}
