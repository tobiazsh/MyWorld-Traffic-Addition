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
import net.minecraft.text.Text;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.MODVER;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;

public class AboutWindow {

    public static String title = MyWorldTrafficAddition.MOD_ID_HUMAN;
    public static String author = "Tobiazsh (Tobias)";
    public static String description = Text.translatable("mwta.description").getString(); // Description
    public static String[] other = {Text.translatable("mwta.made_in_austria").getString(), Text.translatable("mwta.funny_text_in_about_window_1").getString()}; // Other information

    public static void render() {
        ImGui.pushFont(ImGuiImpl.Roboto);
        ImGui.begin(Text.translatable("mwta.imgui.sign.editor.about").getString() + " " + title, ImGuiWindowFlags.MenuBar); // "About MyWorld Traffic Addition" title

        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.window").getString())) { // "Window" menu
                if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.quit_to_minecraft").getString())) { // "Quit to Minecraft" menu item
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

        ImGui.text(Text.translatable("mwta.imgui.sign.editor.name").getString() + ": " + title); // "Name: MyWorld Traffic Addition"
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.version").getString() + ": " + MODVER); // "Version: ..."
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.author").getString() + ": " + author); // Author: Tobias
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.description") + ": " + description); // Description: -

        ImGui.newLine();

        ImGui.text(Text.translatable("mwta.imgui.sign.editor.other").getString() + ":"); // "Other:"
        ImGui.sameLine();
        for (String string : other) {
            ImGui.setCursorPosX((imgui.calcTextSize(Text.translatable("mwta.imgui.sign.editor.other").getString() + ":").x + 10));
            ImGui.text(string);
        }

        ImGui.newLine();

        ImGui.popFont();
        ImGui.pushFont(ImGuiImpl.RobotoBold);

        ImGui.text(Text.translatable("mwta.imgui.sign.editor.thanks_for_downloading").getString()); // "THANK YOU FOR DOWNLOADING <3!"

        ImGui.popFont();

        ImGui.end();
    }
}
