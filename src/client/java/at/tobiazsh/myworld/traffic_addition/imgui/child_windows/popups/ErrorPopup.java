package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Textures;
import imgui.ImGui;
import net.minecraft.text.Text;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class ErrorPopup {

    private static boolean shouldOpen = false;
    private static String text;
    private static String message;
    private static final String errorIconPath = "/assets/myworld_traffic_addition/textures/imgui/icons/info.png";
    private static Runnable onClose;

    public static void render() {
        if (ImGui.beginPopupModal(tr("Global", "Error") + "##Popup")) {

            ImGui.pushFont(ImGuiImpl.RobotoBold);

            ImGui.image(Textures.smartRegisterTexture(errorIconPath).getTextureId(), 20, 20);

            ImGui.sameLine();
            ImGui.spacing();
            ImGui.sameLine();

            ImGui.text(text);
            ImGui.separator();

            ImGui.text("%s:".formatted(tr("Global", "Message")));

            ImGui.popFont();

            ImGui.textWrapped(message);

            ImGui.separator();

            if (ImGui.button(tr("Global", "Close"))) {
                ImGui.closeCurrentPopup();
                onClose.run();
            }

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(tr("Global", "Error") + "##Popup");
            shouldOpen = false;
        }
    }

    public static void open(String text, String message, Runnable close) {
        ErrorPopup.shouldOpen = true;
        ErrorPopup.text = text;
        ErrorPopup.message = message;
        ErrorPopup.onClose = close;
    }
}
