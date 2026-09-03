package at.tobiazsh.myworld.traffic_addition.imgui.overlay;

import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignSelector;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import dev.tobiazsh.imguib3d.client.overlay.ImGuiOverlay;

public class SignSelectionOverlay implements ImGuiOverlay {

    private final SignSelector signSelector = new SignSelector("main_sign_selector");
    private final ImGuiFontScope fontScope = ImGuiFontScope.create();

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
        signSelector.render();
        fontScope.pop();
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
