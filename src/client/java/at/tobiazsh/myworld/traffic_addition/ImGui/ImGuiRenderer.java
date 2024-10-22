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

    public static void render() {
        ImGuiImpl.draw(io -> {
            ImGui.pushFont(ImGuiImpl.DejaVuSans);

            if (initOnce) {
                loadMainTextures();
            }

            if (showDemoWindow) { ImGui.showDemoWindow(); ImGui.showAboutWindow(); }
            if (showAboutWindow) AboutScreen.render();

            if (showSignEditor) SignEditorScreen.render();

            ImGui.popFont();
        });
    }
}
