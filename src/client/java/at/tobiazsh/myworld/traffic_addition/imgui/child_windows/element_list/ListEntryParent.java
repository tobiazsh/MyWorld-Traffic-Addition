package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;

public interface ListEntryParent {
    void removeChild(ElementListEntry child);
    void addChild(ElementListEntry child);
    void refresh();
    ElementId getId();
}
