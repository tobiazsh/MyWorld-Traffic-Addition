package at.tobiazsh.myworld.traffic_addition.ImGui;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 18:45
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.AboutWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.PreferencesWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.SignEditor;
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
            ImGui.pushFont(ImGuiImpl.DejaVuSans); // Use default font

            ErrorPopup.render(); // Render error popup
            ConfirmationPopup.render(); // Render confirmation popup

            if (showDemoWindow) { ImGui.showDemoWindow(); ImGui.showAboutWindow(); } // If demo window should be shown, do so
            if (showAboutWindow) AboutWindow.render(); // If about window should be shown, do so
            if (PreferencesWindow.show) PreferencesWindow.render(); // If pref window should be shown, do so

            if (ImGui.isKeyPressed(ImGuiKey.F12)) {
                // ... Do something
            }

            if (shouldSnap) {
                float width = MinecraftClient.getInstance().getWindow().getWidth();
                float height = MinecraftClient.getInstance().getWindow().getHeight();

                ImGui.setNextWindowPos(0, 0);
                ImGui.setNextWindowSize(width, height);
            }

            if (showSignEditor) SignEditor.render(); // If the sign editor has to be rendered, do so

            ImGui.popFont(); // Pop default font
        });
    }
}
