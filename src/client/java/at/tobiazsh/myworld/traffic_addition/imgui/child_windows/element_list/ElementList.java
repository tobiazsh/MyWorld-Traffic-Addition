package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.clipboard.ElementClipboard;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiPopupFlags;
import imgui.flag.ImGuiStyleVar;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * A window that's supposed to list all current elements on the canvas, as well as groups
 */
public class ElementList {

    private boolean isVisible = false;

    private final String id;

    private final ElementListEntryRoot rootEntry;
    private final ClientElementManager manager;

    public ElementList(
            String id,
            ClientElementManager manager,
            ElementSelectionCallback selectionCallback,
            ElementClipboard clipboard
    ) {
        this.id = id;
        this.rootEntry = new ElementListEntryRoot(id, manager, selectionCallback, clipboard);
        this.manager = manager;
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

        ImGui.sameLine();
        ImGui.dummy(controlBarButtonSize, 0);
        ImGui.sameLine();

        renderUpDownButton();
    }

    private void renderUpDownButton() {
        float textHeight = ImGui.getTextLineHeight();
        float arrowButtonPadding = (controlBarButtonSize - textHeight) / 2f;

        var selectedIds = rootEntry.getSelectedIds();
        boolean oneSelected = selectedIds.size() == 1;
        var child = oneSelected ? this.rootEntry.getChild(selectedIds.iterator().next()) : null;
        int selectedIndex = oneSelected && child != null ? child.getParent().indexOf(child) : -1;
        int size = child != null ? child.getParent().size() : 0;
        boolean canMoveUp = selectedIndex > 0;
        boolean canMoveDown = selectedIndex < size - 1;

        if (!canMoveUp)
            ImGui.beginDisabled();

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, arrowButtonPadding, arrowButtonPadding);
        if (ImGui.arrowButton("##moveElementUp", ImGuiDir.Up) && child != null) {
            child.getParent().moveChild(selectedIndex, selectedIndex - 1);
        }
        ImGui.popStyleVar();

        if (!canMoveUp)
            ImGui.endDisabled();

        ImGui.sameLine();

        if (!canMoveDown)
            ImGui.beginDisabled();

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, arrowButtonPadding, arrowButtonPadding);
        if (ImGui.arrowButton("##moveElementDown", ImGuiDir.Down) && child != null) {
            child.getParent().moveChild(selectedIndex, selectedIndex + 1);
        }
        ImGui.popStyleVar();

        if (!canMoveDown)
            ImGui.endDisabled();
    }

    private void renderAddMenu() {
        if (ImGui.beginPopupContextItem(addMenuId, ImGuiPopupFlags.NoOpenOverExistingPopup)) {
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
