package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupListEntry extends ElementListEntry implements ListEntryParent, GroupingElement {

    private static @Nullable ImGuiTexture groupThumbnailTexture;

    private boolean isExpanded = false;
    private final List<ElementListEntry> children;

    private final ListEntryCreator listEntryCreator;

    public GroupListEntry(
            ClientElementInterface element,
            ElementId id,
            String dragDropDataType,
            ListEntryParent parent,
            Consumer<ElementId> onSelect,
            Consumer<ElementListEntry> onNewEntry,
            Consumer<ElementListEntry> onEntryRemoved,
            ListEntryLookup listEntryLookup
    ) {
        if (!(element instanceof GroupElementClient groupElement))
            throw new RuntimeException("GroupListEntry can only be created with a GroupElementClient, but got: " + element.getClass().getName());

        super(groupElement, id, dragDropDataType, parent, onSelect, onNewEntry, onEntryRemoved, listEntryLookup);
        if (groupThumbnailTexture == null) createGroupThumbnailTexture();
        this.thumbnailTexture = groupThumbnailTexture;

        this.listEntryCreator = new ListEntryCreator(
                this,
                dragDropDataType,
                onSelect,
                onNewEntry,
                onEntryRemoved,
                listEntryLookup
        );

        this.children = groupElement.getClientElements().stream()
                .map(child -> new ElementListEntry(
                        child,
                        child.getId(),
                        dragDropDataType,
                        this,
                        onSelect,
                        onNewEntry,
                        onEntryRemoved,
                        listEntryLookup
                ))
                .collect(Collectors.toList());

        this.overrideSelectableHeight = true;
    }

    @Override
    protected void renderContextMenuContent(Set<ElementId> selectedIds) {
        super.renderContextMenuContent(selectedIds);

        if (ImGui.menuItem("Dissolve Group")) {

        }
    }

    @Override
    protected void onElementDropped(@NonNull ElementListEntryDragDropPayload payload) {
        var droppedElementEntry = listEntryLookup.getEntryById(payload.elementEntryId());

        if (droppedElementEntry.isEmpty())
            return;

        // Remove the dropped element from its current parent
        droppedElementEntry.get().parent.removeChild(droppedElementEntry.get());

        // Add the dropped element to this group
        this.addChild(droppedElementEntry.get());
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

    protected void renderChildren(Set<ElementId> selectedElementIds) {
        if (isExpanded) {
            ImGui.indent();
            for (ElementListEntry child : children) {
                child.render(selectedElementIds);
            }
            ImGui.unindent();
        }
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

        for (ClientElementInterface element : elementChildren) {
            var childEntry = existingEntries.remove(element.getId());

            if (childEntry == null) {
                childEntry = listEntryCreator.create(element);
                onNewEntry.accept(childEntry);
            }

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
        this.deleteChild(child);
        parent.addElementChild(child.getElement());
    }

    @Override
    public void addToGroup(ElementListEntry child) {
        // Does essentially the same as addChild, but is semantically different, so we keep it separate.
        this.addElementChild(child.getElement());
    }

    // DO NOT dispose the groupThumbnailTexture here, as it is shared among all instances of GroupListEntry.
    // It will be disposed automatically when the application exits, as stated by the ImGuiB3D documentation.
}
