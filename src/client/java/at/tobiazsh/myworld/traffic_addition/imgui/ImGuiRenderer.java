package at.tobiazsh.myworld.traffic_addition.imgui;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 18:45
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.AboutWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.PreferencesWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignSelector;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import net.minecraft.client.MinecraftClient;

public class ImGuiRenderer {

    public static boolean showAboutWindow = false;
    public static boolean showDemoWindow = false;
    public static boolean showSignEditor = false;

    public static boolean shouldSnap = false;

    /**
     * Renders ImGui
     */
    public static void render() {
        ImGuiImpl.draw(io -> {
            ImGui.pushFont(ImGuiImpl.Roboto); // Use default font

            ErrorPopup.render(); // Render error popup

            if (showDemoWindow) { ImGui.showDemoWindow(); ImGui.showAboutWindow(); } // If demo window should be shown, do so
            if (showAboutWindow) AboutWindow.render(); // If about window should be shown, do so
            if (PreferencesWindow.show) PreferencesWindow.render(); // If pref window should be shown, do so

            if (shouldSnap) {
                float width = MinecraftClient.getInstance().getWindow().getWidth();
                float height = MinecraftClient.getInstance().getWindow().getHeight();

                ImGui.setNextWindowPos(0, 0);
                ImGui.setNextWindowSize(width, height);
            }

            if (showSignEditor) SignEditor.render(); // If the sign editor has to be rendered, do so

            SignSelector.signSelectors.forEach(SignSelector::render); // Render all sign selectors

            ImGui.popFont(); // Pop default font
        });
    }
}
