package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Textures;
import imgui.ImGui;
import net.minecraft.text.Text;

public class ErrorPopup {

    private static boolean shouldOpen = false;
    private static String text;
    private static String message;
    private static final String errorIconPath = "/assets/myworld_traffic_addition/textures/imgui/icons/info.png";
    private static Runnable onClose;

    public static void render() {
        if (ImGui.beginPopupModal(Text.translatable("mwta.imgui.sign.editor.popups.error").getString() + "##Popup")) {

            ImGui.pushFont(ImGuiImpl.RobotoBold);

            ImGui.image(Textures.smartRegisterTexture(errorIconPath).getTextureId(), 20, 20);

            ImGui.sameLine();
            ImGui.spacing();
            ImGui.sameLine();

            ImGui.text(text);
            ImGui.separator();

            ImGui.text(Text.translatable("mwta.imgui.sign.editor.popups.error.message").getString());

            ImGui.popFont();

            ImGui.textWrapped(message);

            ImGui.separator();

            if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.popups.error.close").getString())) {
                ImGui.closeCurrentPopup();
                onClose.run();
            }

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(Text.translatable("mwta.imgui.sign.editor.popups.error").getString() + "##Popup");
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
