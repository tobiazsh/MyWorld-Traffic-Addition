package at.tobiazsh.myworld.traffic_addition.imgui.overlay;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import dev.tobiazsh.imguib3d.client.overlay.ImGuiOverlay;

public final class EditorOverlay implements ImGuiOverlay {

    private final ImGuiFontScope fontScope = ImGuiFontScope.create();

    public final ErrorPopup errorPopup = new ErrorPopup("editor_error_popup");
    private final SignEditor signEditor = new SignEditor("main_sign_editor", errorPopup);

    private boolean isVisible = false;

    public void open() {
        isVisible = true;
    }

    public void close() {
        isVisible = false;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void draw() {
        fontScope.push(DefaultFonts.RobotoNormal);
        errorPopup.render();
        signEditor.render();
        fontScope.pop();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String getId() {
        return "editor_overlay";
    }
}
