package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;

import java.util.Set;

@FunctionalInterface
public interface ElementSelectionCallback {
    /**
     * Called when an element is selected or deselected in the element list.
     * @param selectedIds The set of currently selected element IDs. Should be an immutable copy of the selected IDs.
     */
    void getSelectedIds(Set<ElementId> selectedIds); // Used to determine whether multiple or only one element is selected
}
