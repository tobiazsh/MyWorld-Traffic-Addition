package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;

import java.util.List;

public interface ListEntryParent {
    /**
     * Removes the child from this parent. Does not update the element manager.
     * Child will be readded next time the list of children is refreshed, unless it is removed from the element manager
     * manually.
     * @param child The child to remove from this parent.
     */
    void removeChild(ElementListEntry child);

    /**
     * Removes the child from this group and adds it to the parent of this group.
     * <p>Does not remove children recursively, only the direct children of this group are affected.
     * @param child The child to remove from this group.
     */
    void deleteChild(ElementListEntry child);

    /**
     * Adds element to element manager, but does not add it to the list of children of this parent.
     * <p>Refreshes the list of children after adding the element.
     * @param element The element to add.
     */
    void addElement(ClientElementInterface element);

    /**
     * Adds element to element manager at position <code>index</code>, but does not add it to the list of children of
     * this parent.
     * <p>Refreshes the list of children after adding the element.
     * @param element The element to add.
     * @param index The index at which to add the element in the element manager.
     */
    void addElement(ClientElementInterface element, int index);

    /**
     * Adds a child element to the associated element container of this parent.
     * <p>Refreshes the list of children after adding the element.
     * @param element The element to add.
     */
    void addElementChild(ClientElementInterface element);

    /**
     * Adds a child element to the associated element container of this parent at position <code>index</code>.
     * @param element The element to add.
     * @param index The index at which to add the element in the element container.
     */
    void addElementChild(ClientElementInterface element, int index);

    /**
     * Adds multiple child elements to the associated element container of this parent without refreshing the list of
     * children after each addition.
     * <p>Refreshes the list of children after all elements have been added.
     * @param elements The elements to add.
     */
    void addElementsChild(List<ClientElementInterface> elements);

    /**
     * Adds multiple child elements to the associated element container of this parent, starting at <code>index</code>,
     * without refreshing the list of children after each addition.
     * <p>Refreshes the list of children after all elements have been added.
     * @param elements The elements to add.
     * @param index The index at which to add the elements in the element container.
     */
    void addElementsChild(List<ClientElementInterface> elements, int index);

    void refresh();
    ElementId getId();
}
