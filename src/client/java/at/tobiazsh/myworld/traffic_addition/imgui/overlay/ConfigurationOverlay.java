package at.tobiazsh.myworld.traffic_addition.imgui.overlay;

import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.AboutWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.PreferencesWindow;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import dev.tobiazsh.imguib3d.client.overlay.ImGuiOverlay;

public class ConfigurationOverlay implements ImGuiOverlay {

    private final PreferencesWindow preferencesWindow = new PreferencesWindow("main_preferences_window");
    private final AboutWindow aboutWindow = new AboutWindow("main_about_window");

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
        preferencesWindow.render();
        aboutWindow.render();
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
