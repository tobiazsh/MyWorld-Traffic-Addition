package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;

import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Utils.ArrayTools;
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

        ImGui.pushFont(ImGuiImpl.Roboto);
        if (ImGui.begin("Elements")) {
            for (int i = 0; i < ClientElementManager.getInstance().totalElements(); i++) {
                ClientElementInterface element = ClientElementManager.getInstance().getElement(i);
                ElementEntry entry = new ElementEntry(element, ClientElementInterface.MAIN_CANVAS_ID) {
                    @Override
                    public void moveEntryUp() {
                        ClientElementManager.getInstance().setElements(
                                ArrayTools.moveElementUpBy(
                                        ClientElementManager.getInstance().getElements(),
                                        ClientElementManager.getInstance().indexOfElement(element),
                                        1
                                )
                        );
                    }

                    @Override
                    public void moveEntryDown() {
                        ClientElementManager.getInstance().setElements(
                                ArrayTools.moveElementDownBy(
                                        ClientElementManager.getInstance().getElements(),
                                        ClientElementManager.getInstance().indexOfElement(element),
                                        1
                                )
                        );
                    }

                    @Override
                    public void elementSelectedAction() {
                        selectedElement = element;
                        ElementPropertyWindow.initVars(element, signRatio);
                    }

                    @Override
                    public ClientElementInterface getElement(int i) {
                        return ClientElementManager.getInstance().getElement(i);
                    }

                    @Override
                    public void addElementFirst(ClientElementInterface element) {
                        ClientElementManager.getInstance().addElementFirst(element);
                    }

                    @Override
                    public int indexOfElement(ClientElementInterface element) {
                        return ClientElementManager.getInstance().indexOfElement(element);
                    }

                    @Override
                    public int sizeOfList() {
                        return ClientElementManager.getInstance().totalElements();
                    }

                    @Override
                    public void addElement(ClientElementInterface element) {
                        ClientElementManager.getInstance().addElement(element);
                    }

                    @Override
                    public void addElement(int index, ClientElementInterface element) {
                        ClientElementManager.getInstance().addElement(index, element);
                    }

                    @Override
                    public void addAllElements(List<ClientElementInterface> elements) {
                        ClientElementManager.getInstance().addAllElements(elements);
                    }

                    @Override
                    public void addAllElements(int index, List<ClientElementInterface> elements) {
                        ClientElementManager.getInstance().addAllElements(index, elements);
                    }

                    @Override
                    public void removeElement(ClientElementInterface element) {
                        ClientElementManager.getInstance().removeElement(element);
                    }

                    @Override
                    public void removeElement(int index) {
                        ClientElementManager.getInstance().removeElement(index);
                    }

                    @Override
                    public void deleteElement(ClientElementInterface element) {
                        ClientElementManager.getInstance().removeElement(element);
                    }
                };

                entry.render(ImGui.getWindowWidth(), ImGui.getStyle().getWindowPaddingX(), element == ClientElementManager.getInstance().getFirstElement(), element == ClientElementManager.getInstance().getLastElement(), selectedElement); // Check if the element is first/last in the list because then there's nothing to move up/down

                if (ClientElementManager.getInstance().getLastElement() != element) ImGui.separator();
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
