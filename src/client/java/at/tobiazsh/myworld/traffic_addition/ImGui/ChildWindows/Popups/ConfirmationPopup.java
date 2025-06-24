package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import imgui.ImGui;

import java.util.function.Consumer;

public class ConfirmationPopup {

    private static String text;
    private static String information;
    private static boolean confirmed = false;
    private static boolean shouldOpen = false;
    public static boolean waitingOnInput = false;

    public static void render() {
        if (ImGui.beginPopupModal("Warning##Popup")) {
            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(text);
            ImGui.popFont();
            ImGui.text(information);

            if (ImGui.button("Yes")) {
                confirmed = true;
                waitingOnInput = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button("No")) {
                confirmed = false;
                waitingOnInput = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup("Warning##Popup");
            shouldOpen = false;
        }
    }

    public static void show(String warn, String info, Consumer<Boolean> callback) {
        text = warn;
        information = info;

        ConfirmationPopup.shouldOpen = true;
        ConfirmationPopup.waitingOnInput = true;

        new Thread(() -> {
           while (ConfirmationPopup.waitingOnInput) {
               try { Thread.sleep(500); } catch (InterruptedException ignore) {}
           }

           callback.accept(confirmed);
        }).start();
    }
}
