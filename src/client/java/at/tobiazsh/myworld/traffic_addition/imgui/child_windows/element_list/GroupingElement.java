package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

/**
 * Elements, which can group other elements, should implement this interface.
 * <p>Used to provide functionality for the methods in list entries, which are otherwise not available for non-grouping
 * elements.
 */
interface GroupingElement {
    /**
     * Moves the given child out of this group and passes it to the parent of this group.
     * @param child The child to remove from this group.
     */
    void removeFromGroup(ElementListEntry child);

    /**
     * Adds the given child to this group. Does not remove it from its previous owner.
     * @param child The child to add to this group.
     */
    void addToGroup(ElementListEntry child);
}
