package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.clipboard.ElementClipboard;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ListEntryCreator {

    private final ListEntryParent parent;
    private final String dragDropDataType;
    private final BiConsumer<ElementId, Boolean> onSelect;
    private final Consumer<ElementListEntry> onNewEntry;
    private final Consumer<ElementListEntry> onEntryRemoved;
    private final ListEntryLookup listEntryLookup;
    private final ElementClipboard clipboard;

    ListEntryCreator(
            ListEntryParent parent,
            String dragDropDataType,
            BiConsumer<ElementId, Boolean> onSelect,
            Consumer<ElementListEntry> onNewEntry,
            Consumer<ElementListEntry> onEntryRemoved,
            ListEntryLookup listEntryLookup,
            ElementClipboard clipboard
    ) {
        this.parent = parent;
        this.dragDropDataType = dragDropDataType;
        this.onSelect = onSelect;
        this.onNewEntry = onNewEntry;
        this.onEntryRemoved = onEntryRemoved;
        this.listEntryLookup = listEntryLookup;
        this.clipboard = clipboard;
    }

    public ElementListEntry create(ClientElementInterface element) {
        ElementListEntry entry;

        if (element instanceof GroupElementClient) {
            entry = new GroupListEntry(
                    element,
                    element.getId(),
                    dragDropDataType,
                    this.parent,
                    this.onSelect,
                    this.onNewEntry,
                    this.onEntryRemoved,
                    this.listEntryLookup,
                    this.clipboard
            );
        } else {
            entry = new ElementListEntry(
                    element,
                    element.getId(),
                    dragDropDataType,
                    this.parent,
                    this.onSelect,
                    this.onNewEntry,
                    this.onEntryRemoved,
                    this.listEntryLookup,
                    this.clipboard
            );
        }

        onNewEntry.accept(entry);
        return entry;
    }
}
