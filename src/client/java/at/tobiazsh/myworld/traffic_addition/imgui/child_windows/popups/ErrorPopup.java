package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.texture.Textures;
import imgui.ImGui;
import net.minecraft.util.Pair;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import at.tobiazsh.myworld.traffic_addition.error.Error;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class ErrorPopup {

    private static final String errorIconPath = "/assets/myworld_traffic_addition/textures/imgui/icons/info.png";
    private static Runnable onClose;
    private static final Queue<Pair<Error, Runnable>> errorQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicReference<String> text = new AtomicReference<>("");
    private static final AtomicReference<String> message = new AtomicReference<>("");

    public static void render() {
        if (ImGui.beginPopupModal(tr("Global", "Error") + "##Popup")) {

            ImGui.pushFont(ImGuiImpl.RobotoBold);

            ImGui.image(Textures.smartRegisterTexture(errorIconPath).getTextureId(), 20, 20);

            ImGui.sameLine();
            ImGui.spacing();
            ImGui.sameLine();

            ImGui.text(text.get());
            ImGui.separator();

            ImGui.text("%s:".formatted(tr("Global", "Message")));

            ImGui.popFont();

            ImGui.textWrapped(message.get());

            ImGui.separator();

            if (ImGui.button(tr("Global", "Close"))) {
                ImGui.closeCurrentPopup();

                if (onClose != null)
                    onClose.run();
            }

            ImGui.endPopup();
        }

        if (hasErrors()) {
            nextError();
            ImGui.openPopup(tr("Global", "Error") + "##Popup");
        }
    }

    public static void open(Error error, Runnable close) {
        errorQueue.add(new Pair<>(error, close));
    }

    public static boolean hasErrors() {
        return !errorQueue.isEmpty();
    }

    private static void nextError() {
        Pair<Error, Runnable> p = errorQueue.poll();
        if (p == null || p.getLeft() == null) {
            return;
        }

        Error e = p.getLeft();
        text.set(e.getTitle() != null ? e.getTitle() : "");
        message.set(e.getMessage() != null ? e.getMessage() : "");
        onClose = p.getRight();
    }
}
