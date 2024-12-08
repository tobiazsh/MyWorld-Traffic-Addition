package at.tobiazsh.myworld.traffic_addition.ImGui;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 18:45
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.Screens.AboutScreen;
import at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen;
import imgui.ImGui;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen.*;

public class ImGuiRenderer {

    public static boolean initOnce = true;

    public static boolean showAboutWindow = false;
    public static boolean showDemoWindow = false;
    public static boolean showSignEditor = false;

    /**
     * Renders ImGui
     */
    public static void render() {
        ImGuiImpl.draw(io -> {
            ImGui.pushFont(ImGuiImpl.DejaVuSans); // Use default font

            // Load main textures only once
            if (initOnce) {
                loadMainTextures();
            }

            if (showDemoWindow) { ImGui.showDemoWindow(); ImGui.showAboutWindow(); } // If demo window should be shown, do so
            if (showAboutWindow) AboutScreen.render(); // If about window should be shown, do so

            if (showSignEditor) SignEditorScreen.render(); // If the sign editor has to be rendered, do so

            ImGui.popFont(); // Pop default font
        });
    }
}
