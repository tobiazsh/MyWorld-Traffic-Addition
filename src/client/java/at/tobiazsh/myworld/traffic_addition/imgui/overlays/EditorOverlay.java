package at.tobiazsh.myworld.traffic_addition.imgui.overlays;

import at.tobiazsh.myworld.traffic_addition.imgui.fonts.FontHelper;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import dev.tobiazsh.imguib3d.client.overlay.ImGuiOverlay;
import imgui.ImGui;

public final class EditorOverlay implements ImGuiOverlay {
    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void draw() {
        boolean pushedFont = false;

        if (FontHelper.allLoaded()) {
            ImGui.pushFont(FontHelper.getRobotoNormal().getImFont(), FontHelper.NORMAL_SIZE);
            pushedFont = true;
        }

        ErrorPopup.render(); // Render error popup
        SignEditor.render(); // If the sign editor has to be rendered, do so

        if (pushedFont)
            ImGui.popFont();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String getId() {
        return "";
    }
}
