package at.tobiazsh.myworld.traffic_addition.imgui.utils;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;

public class ImGuiTools {

    public static void drawLineMaxX() {
        ImGui.sameLine();
        ImGui.getWindowDrawList().addRect(
                new ImVec2(ImGui.getCursorScreenPos().x, ImGui.getCursorScreenPos().y + (float) ImGui.getFontSize() / 2),
                new ImVec2(ImGui.getCursorScreenPos().x + ImGui.getContentRegionAvailX(), ImGui.getCursorScreenPos().y + (float) ImGui.getFontSize() / 2 + 1),
                0xFFFFFFFF, 0, 0, 1
        );
        ImGui.newLine();
    }

    public static void drawRect(int w, int h, ImVec2 posTopLeft, ImVec4 color) {
        ImGui.getWindowDrawList().addRectFilled(
                new ImVec2(posTopLeft.x, posTopLeft.y),
                new ImVec2(posTopLeft.x + w, posTopLeft.y + h),
                ImGui.colorConvertFloat4ToU32(color)
        );
    }
}
