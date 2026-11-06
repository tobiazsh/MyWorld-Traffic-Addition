package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImGui;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupListEntry extends ElementListEntry implements ListEntryParent {

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
            Function<ElementId, @Nullable ElementListEntry> listEntryLookup
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
    }

    @Override
    protected void renderContextMenu() {
        if (ImGui.beginPopupContextItem("###context_menu")) {
            if (ImGui.menuItem("TestGroup")) {
                System.out.println("Test menu item clicked for element: " + element.getName());
            }
        }
    }


    @Override
    protected void onElementDropped(@NonNull ElementListEntryDragDropPayload payload) {
        var droppedElementEntry = listEntryLookup.apply(payload.elementEntryId());

        if (droppedElementEntry == null)
            return;

        // Remove the dropped element from its current parent
        droppedElementEntry.parent.removeChild(droppedElementEntry);

        // Add the dropped element to this group
        this.addChild(droppedElementEntry);
    }

    @Override
    protected void renderContent(Set<ElementId> selectedElementIds) {
        // TODO: Maybe center the button

        if (ImGui.button((isExpanded ? "▼"  : "▶") + "###expand_button")) {
            isExpanded = !isExpanded;
        }

        ImGui.sameLine();
        super.renderContent(selectedElementIds);

        if (isExpanded) {
            renderChildren(Set.of());
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
    public void addChild(ElementListEntry child) {
        this.children.addFirst(child);
    }

    // DO NOT dispose the groupThumbnailTexture here, as it is shared among all instances of GroupListEntry.
    // It will be disposed automatically when the application exits, as stated by the ImGuiB3D documentation.
}
