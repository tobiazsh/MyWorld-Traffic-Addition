package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Utils.ArrayTools;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.GroupElement;
import imgui.ImGui;

import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.SignEditor.*;

public class ElementsWindow {

    /**
     * Flag indicating whether the elements window should be rendered
     */
    public static boolean shouldRender = false;

    /**
     * Renders the elements window if the "shouldRender" flag is set to true.
     * The window displays a list of elements and provides controls for each element.
     */
    public static void render() {
        if (!shouldRender) return;

        ImGui.pushFont(ImGuiImpl.DejaVuSans);
        if (ImGui.begin("Elements")) {
            for (int i = 0; i < elementOrder.size(); i++) {
                BaseElement element = elementOrder.get(i);
                ElementEntry entry = new ElementEntry(element.getName(), element.getId(), element, elementOrder, "MAIN") {
                    @Override
                    public void moveEntryUp() {
                        elementOrder = ArrayTools.moveElementUpBy(elementOrder, elementOrder.indexOf(element), 1);
                    }

                    @Override
                    public void moveEntryDown() {
                        elementOrder = ArrayTools.moveElementDownBy(elementOrder, elementOrder.indexOf(element), 1);
                    }

                    @Override
                    public void elementSelectedAction() {
                        selectedElement = element;
                        ElementPropertyWindow.initVars(element, elementOrder, signRatio);
                    }
                };

                entry.render(ImGui.getWindowWidth(), ImGui.getStyle().getWindowPaddingX(), element == elementOrder.getFirst(), element == elementOrder.getLast(), selectedElement); // Check if the element is first/last in the list because then there's nothing to move up/down

                if (elementOrder.getLast() != element) ImGui.separator();
            }
        }

        ImGui.end();
        ImGui.popFont();

        removeElements();
    }

    /**
     * Toggles the "shouldRender" boolean to show/hide the elements window
     */
    public static void toggle() {
        shouldRender = !shouldRender;
    }

    private static void removeElements() {
        removeRemovableElementsList(elementOrder);
        removeRemovableElementsGroups(elementOrder);
    }

    private static void removeRemovableElementsList(List<BaseElement> elements) {
        elements.removeIf(BaseElement::shouldRemove);
    }

    private static void removeRemovableElementsGroups(List<BaseElement> elements) {
        elements.stream().filter(element -> element instanceof GroupElement).forEach(element -> {
            elements.forEach(insideElement -> {

                if (insideElement instanceof GroupElement)
                    removeRemovableElementsGroups(((GroupElement) insideElement).getElements());

                ((GroupElement) element).getElements().removeIf(BaseElement::shouldRemove);
            });
        });
    }
}
