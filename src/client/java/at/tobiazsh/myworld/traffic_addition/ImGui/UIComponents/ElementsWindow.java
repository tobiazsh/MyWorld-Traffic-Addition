package at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents;

import at.tobiazsh.myworld.traffic_addition.ImGui.Utilities.ArrayTools;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen.*;

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

        if (ImGui.begin("Elements", ImGuiWindowFlags.NoNavInputs)) {
            for (int i = 0; i < baseElementDrawOrder.size(); i++) {
                BaseElement element = baseElementDrawOrder.get(i);
                ElementEntry entry = new ElementEntry(element.name, element.getId(), element) {
                    @Override
                    public void moveEntryUp() {
                        baseElementDrawOrder = ArrayTools.moveElementUpBy(baseElementDrawOrder, baseElementDrawOrder.indexOf(element), 1);
                    }

                    @Override
                    public void moveEntryDown() {
                        baseElementDrawOrder = ArrayTools.moveElementDownBy(baseElementDrawOrder, baseElementDrawOrder.indexOf(element), 1);
                    }

                    @Override
                    public void elementSelectedAction() {
                        selectedElement = element;
                        ElementPropertyWindow.initVars(element, ratioedSignSize);
                    }
                };

                entry.render(ImGui.getWindowWidth(), ImGui.getStyle().getWindowPaddingX(), element == baseElementDrawOrder.getFirst(), element == baseElementDrawOrder.getLast(), selectedElement); // Check if the element is first/last in the list because then there's nothing to move up/down

                if (entry.removeMyself) {
                    removeElementList.add(element);
                    entry.removeMyself = false;
                }

                if (baseElementDrawOrder.getLast() != element) ImGui.separator();
            }

            for (BaseElement element : removeElementList) {
                baseElementDrawOrder.remove(element);
            }

            ImGui.end();
        }
    }

    /**
     * Toggles the "shouldRender" boolean to show/hide the elements window
     */
    public static void toggle() {
        shouldRender = !shouldRender;
    }
}
