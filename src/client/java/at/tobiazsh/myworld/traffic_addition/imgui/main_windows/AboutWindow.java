package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 21:24
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.MODVER;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class AboutWindow {

    public static String title = MyWorldTrafficAddition.MOD_ID_HUMAN;
    public static String author = "Tobiazsh (Tobias)";
    public static String description = tr("MWTA", "A Minecraft Mod for improved roads and general canvases in Minecraft"); // Description
    public static String[] other = {
            tr("ImGui.Main.AboutWindow", "Made in Austria"),
            tr("ImGui.Main.AboutWindow", "While I am writing this, I should probably study for school but eh ¯\\_(^_^)_/¯")
    }; // Other information

    public static void render() {
        ImGui.pushFont(ImGuiImpl.Roboto);
        ImGui.begin(tr("Global", "About") + " " + title, ImGuiWindowFlags.MenuBar); // "About MyWorld Traffic Addition" title

        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu(tr("Global", "Window"))) { // "Window" menu
                if (ImGui.menuItem(tr("Global", "Quit to Minecraft"))) { // "Quit to Minecraft" menu item
                    ImGuiRenderer.showAboutWindow = false;
                }
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.RobotoBoldBig);

        float titleWidth = imgui.calcTextSize(title).x;
        float windowWidth = ImGui.getWindowWidth();
        ImGui.setCursorPosX((windowWidth - titleWidth) / 2);
        ImGui.text(title);

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.Roboto);

        ImGui.newLine();

        ImGui.text("%s: %s".formatted(tr("Global", "Name"), title)); // "Name: MyWorld Traffic Addition"
        ImGui.text("%s: %s".formatted(tr("Global", "Version"), MODVER)); // "Version: ..."
        ImGui.text("%s: %s".formatted(tr("Global", "Author"), author)); // Author: Tobias
        ImGui.text("%s: %s".formatted(tr("Global", "Description"), description)); // Description: -

        ImGui.newLine();

        ImGui.text(tr("Global", "Other") + ":"); // "Other:"
        ImGui.sameLine();
        for (String string : other) {
            ImGui.setCursorPosX((imgui.calcTextSize(tr("Global", "Other") + ":").x + 10));
            ImGui.text(string);
        }

        ImGui.newLine();

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.RobotoBold);

        ImGui.text(tr("ImGui.Main.AboutWindow", "THANK YOU FOR DOWNLOADING <3!")); // "THANK YOU FOR DOWNLOADING <3!"

        ImGui.popFont();

        ImGui.end();
    }
}
