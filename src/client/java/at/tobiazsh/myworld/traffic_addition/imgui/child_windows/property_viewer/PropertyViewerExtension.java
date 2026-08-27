package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;

/**
 * Extension interface for {@link ElementPropertyViewer} for each Element type to implement. This allows for custom
 * properties, which are not displayed in the generic property viewer.
 *
 * @param <T> The type of the element to represent
 */
public interface PropertyViewerExtension {
    void render(String id);
    void setElement(ClientElementInterface element);
}
