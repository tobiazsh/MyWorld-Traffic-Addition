package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;

public interface ListEntryParent {
    void removeChild(ElementListEntry child);
    void deleteChild(ElementListEntry child);
    void addChild(ElementListEntry child);

    /**
     * Adds element to element manager, but does not add it to the list of children of this parent.
     * Refreshes the list of children after adding the element.
     * @param element The element to add.
     */
    void addElement(ClientElementInterface element);

    void refresh();
    ElementId getId();
}
