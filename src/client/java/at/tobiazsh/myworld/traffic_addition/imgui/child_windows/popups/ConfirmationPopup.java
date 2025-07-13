package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import imgui.ImGui;

import java.util.function.Consumer;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class ConfirmationPopup {

    private static String text;
    private static String information;
    private static boolean confirmed = false;
    private static boolean shouldOpen = false;
    public static boolean waitingOnInput = false;

    public static void render() {
        if (ImGui.beginPopupModal(tr("Global", "Warning") + "##Popup")) {
            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(text);
            ImGui.popFont();
            ImGui.text(information);

            if (ImGui.button(tr("Global", "Yes"))) {
                confirmed = true;
                waitingOnInput = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button(tr("Global", "No"))) {
                confirmed = false;
                waitingOnInput = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(tr("Global", "Warning") + "##Popup");
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
