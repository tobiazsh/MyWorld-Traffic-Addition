package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import imgui.ImGui;

public class ConfirmationPopup {

    private static String text;
    private static String information;
    private static Runnable confirm;
    private static Runnable cancel;
    private static boolean shouldOpen = false;

    public static void render() {
        if (ImGui.beginPopupModal("Warning##Popup")) {
            ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
            ImGui.text(text);
            ImGui.popFont();
            ImGui.text(information);

            if (ImGui.button("Yes")) {
                confirm.run();
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button("No")) {
                cancel.run();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup("Warning##Popup");
            shouldOpen = false;
        }
    }

    public static void show(String warn, String info, Runnable onConfirm, Runnable onCancel) {
        text = warn;
        confirm = onConfirm;
        cancel = onCancel;
        information = info;

        ConfirmationPopup.shouldOpen = true;
    }
}
