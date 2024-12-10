package at.tobiazsh.myworld.traffic_addition.ImGui.Screens;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 21:24
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.MODVER;

public class AboutScreen {

    public static String title = MyWorldTrafficAddition.MOD_ID_HUMAN;
    public static String author = "Tobiazsh (Tobias)";
    public static String description = "A Minecraft Mod for better roads in Minecraft";
    public static String[] other = {"Made in Austria", "While I am writing this, I should probably study for school but eh ¯\\_(^_^)_/¯"};

    private static final ImGui imgui = new ImGui();

    public static void render() {
        ImGui.pushFont(ImGuiImpl.DejaVuSans);
        ImGui.begin("About " + title, ImGuiWindowFlags.MenuBar);

        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("Window")) {
                if (ImGui.menuItem("Quit to Minecraft")) {
                    ImGuiRenderer.showAboutWindow = false;
                }
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.DejaVuSansBoldBig);

        float titleWidth = imgui.calcTextSize(title).x;
        float windowWidth = ImGui.getWindowWidth();
        ImGui.setCursorPosX((windowWidth - titleWidth) / 2);
        ImGui.text(title);

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.DejaVuSans);

        ImGui.newLine();

        ImGui.text("Name: " + title);
        ImGui.text("Version: " + MODVER);
        ImGui.text("Author: " + author);
        ImGui.text("Description: " + description);

        ImGui.newLine();

        ImGui.text("Other:");
        ImGui.sameLine();
        for (String string : other) {
            ImGui.setCursorPosX((imgui.calcTextSize("Other:").x + 10));
            ImGui.text(string);
        }

        ImGui.newLine();

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.DejaVuSansBold);

        ImGui.text("THANK YOU FOR DOWNLOADING <3!");

        ImGui.popFont();

        ImGui.end();
    }
}
