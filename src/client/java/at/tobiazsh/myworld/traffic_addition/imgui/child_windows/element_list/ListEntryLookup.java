package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;

import java.util.Optional;

public interface ListEntryLookup {
    /**
     * Looks up an entry by its ID.
     * @param id The id of the entry to look up.
     * @return The entry with the given id, or <code>Optional.empty()</code> if no such entry exists.
     */
    Optional<ElementListEntry> getEntryById(ElementId id);

    /**
     * Looks up the index of an entry by its ID.
     * @param id The id of the entry to look up.
     * @return The index of the entry with the given id, or -1 if no such entry exists.
     */
    int getIndexOfEntry(ElementId id);

    /**
     * Looks up an entry by its index.
     * @param index The index of the child entry to look up.
     * @return The child entry at the given index, or <code>Optional.empty()</code> if no such entry exists.
     */
    Optional<ElementListEntry> getEntryByIndex(int index);
}
