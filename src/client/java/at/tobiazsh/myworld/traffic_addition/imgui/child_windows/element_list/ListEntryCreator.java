package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ListEntryCreator {

    private final ListEntryParent parent;
    private final String dragDropDataType;
    private final Consumer<ElementId> onSelect;
    private final Consumer<ElementListEntry> onNewEntry;
    private final Consumer<ElementListEntry> onEntryRemoved;
    private final Function<ElementId, @Nullable ElementListEntry> listEntryLookup;

    ListEntryCreator(
            ListEntryParent parent,
            String dragDropDataType,
            Consumer<ElementId> onSelect,
            Consumer<ElementListEntry> onNewEntry,
            Consumer<ElementListEntry> onEntryRemoved,
            Function<ElementId, @Nullable ElementListEntry> listEntryLookup
    ) {
        this.parent = parent;
        this.dragDropDataType = dragDropDataType;
        this.onSelect = onSelect;
        this.onNewEntry = onNewEntry;
        this.onEntryRemoved = onEntryRemoved;
        this.listEntryLookup = listEntryLookup;
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
                    this.listEntryLookup
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
                    this.listEntryLookup
            );
        }

        onNewEntry.accept(entry);
        return entry;
    }
}
