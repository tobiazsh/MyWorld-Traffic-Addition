package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class ConfirmationPopup {

    private String text;
    private String information;
    private boolean confirmed = false;
    private boolean shouldOpen = false;

    public boolean waitingOnInput = false;

    private final ImGuiFontScope fontScope;
    private final String id;

    public ConfirmationPopup(String id, ImGuiFontScope fontScope) {
        this.id = id;
        this.fontScope = fontScope;
    }

    public void render() {
        if (ImGui.beginPopupModal(tr("Global", "Warning") + "##Popup" + id)) {

            fontScope.push(DefaultFonts.RobotoBold);
            ImGui.text(text);
            fontScope.pop();

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

    public void show(String warn, String info, Consumer<Boolean> callback) {
        text = warn;
        information = info;

        shouldOpen = true;
        waitingOnInput = true;

        new Thread(() -> {
           while (waitingOnInput) {
               try { Thread.sleep(500); } catch (InterruptedException ignore) {}
           }

           callback.accept(confirmed);
        }).start();
    }
}
