package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ArrayTools;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Windows.SignEditor.*;

public class ElementsWindow {

    /**
     * Flag indicating whether the elements window should be rendered
     */
    public static boolean shouldRender = false;

    /**
     * List of elements to be removed
     */
    public static List<BaseElement> removeElementList = new ArrayList<>();

    /**
     * Renders the elements window if the "shouldRender" flag is set to true.
     * The window displays a list of elements and provides controls for each element.
     */
    public static void render() {
        if (!shouldRender) return;

        ImGui.pushFont(ImGuiImpl.DejaVuSans);
        if (ImGui.begin("Elements", ImGuiWindowFlags.NoNavInputs)) {
            for (int i = 0; i < elementOrder.size(); i++) {
                BaseElement element = elementOrder.get(i);
                ElementEntry entry = new ElementEntry(element.name, element.getId(), element) {
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
                        ElementPropertyWindow.initVars(element, signRatio);
                    }
                };

                entry.render(ImGui.getWindowWidth(), ImGui.getStyle().getWindowPaddingX(), element == elementOrder.getFirst(), element == elementOrder.getLast(), selectedElement); // Check if the element is first/last in the list because then there's nothing to move up/down

                if (entry.removeMyself) {
                    removeElementList.add(element);
                    entry.removeMyself = false;
                }

                if (elementOrder.getLast() != element) ImGui.separator();
            }

            for (BaseElement element : removeElementList) {
                elementOrder.remove(element);
            }
        }

        ImGui.end();
        ImGui.popFont();
    }

    /**
     * Toggles the "shouldRender" boolean to show/hide the elements window
     */
    public static void toggle() {
        shouldRender = !shouldRender;
    }
}
