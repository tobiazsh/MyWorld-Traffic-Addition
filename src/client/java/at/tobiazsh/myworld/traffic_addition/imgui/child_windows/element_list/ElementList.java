package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.clipboard.ElementClipboard;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import imgui.ImGui;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * A window that's supposed to list all current elements on the canvas, as well as groups
 */
public class ElementList {

    private boolean isVisible = false;

    private final String id;

    private final ElementListEntryRoot rootEntry;

    public ElementList(
            String id,
            ClientElementManager manager,
            ElementSelectionCallback selectionCallback,
            ElementClipboard clipboard
    ) {
        this.id = id;
        this.rootEntry = new ElementListEntryRoot(id, manager, selectionCallback, clipboard);
        manager.elementListChangedEvent.addListener(rootEntry::refresh);
    }

    public void render() {
        if (!isVisible) return;

        if (ImGui.begin(trIC("text.mwta.element-list.title", id, "elementListWindow"))) {
            final float itemPaddingX = ImGui.getStyle().getItemSpacingX();

            rootEntry.render(ImGui.getContentRegionAvailY() - getControlBarSize());

            ImGui.setCursorPosY(ImGui.getWindowContentRegionMaxY() - getControlBarSize() + itemPaddingX);
            renderControlBar();

            ImGui.end();
        }
    }

    private final float controlBarButtonSize = 25;
    private final String addMenuId = "##addMenu";
    private void renderControlBar() {
        renderAddMenu();

        if (ImGui.button("+", controlBarButtonSize, controlBarButtonSize)) {
            ImGui.openPopup(addMenuId);
        }
    }

    private void renderAddMenu() {
        if (ImGui.beginPopupContextItem(addMenuId)) {
            if (ImGui.menuItem("New Group")) {
                System.out.println("new group!");
            }

            if (ImGui.menuItem("New Text Element")) {
                System.out.println("new text!");
            }

            if (ImGui.menuItem("New Image Element")) {
                System.out.println("new image!");
            }
            ImGui.endPopup();
        }
    }

    private float getControlBarSize() {
        return ImGui.getStyle().getItemSpacingY() * 2 + controlBarButtonSize;
    }

    public void toggleVisibility() {
        this.isVisible = !this.isVisible;
    }

    public void show() {
        this.isVisible = true;
    }

    public void hide() {
        this.isVisible = false;
    }
}
