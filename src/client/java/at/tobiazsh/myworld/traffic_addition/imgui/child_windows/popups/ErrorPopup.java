package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.error.ErrorReporter;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.texture.Textures;
import at.tobiazsh.myworld.traffic_addition.utils.Tuple;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import at.tobiazsh.myworld.traffic_addition.error.Error;
import imgui.flag.ImGuiWindowFlags;
import org.jetbrains.annotations.NotNull;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

public class ErrorPopup implements ErrorReporter {

    private static final String errorIconPath = "/assets/myworld_traffic_addition/textures/imgui/icons/info.png";
    private Runnable onClose;
    private final Queue<Tuple<@NotNull Error, @NotNull Runnable>> errorQueue = new ConcurrentLinkedQueue<>();
    private final AtomicReference<String> text = new AtomicReference<>("");
    private final AtomicReference<String> message = new AtomicReference<>("");
    private final ImGuiFontScope fontScope = ImGuiFontScope.create();
    private final String id;

    public ErrorPopup(String id) {
        this.id = id;
    }

    public void render() {
        if (ImGui.beginPopupModal(
                trIC("text.mwta.error-popup.title", id, "errorPopup"), ImGuiWindowFlags.AlwaysAutoResize
        )) {

            fontScope.push(DefaultFonts.RobotoBold);

            ImGui.image(Textures.smartRegisterTexture(errorIconPath).getTextureId(), 20, 20);

            ImGui.sameLine();
            ImGui.spacing();
            ImGui.sameLine();

            ImGui.text(text.get());
            ImGui.separator();

            ImGui.text(trIC("text.mwta.error-popup.message", id, "errorMessage") + ":");

            fontScope.pop();

            ImGui.textWrapped(message.get());

            ImGui.separator();

            if (ImGui.button(trIC("text.mwta.error-popup.close", id, "errorCloseBtn"))) {
                ImGui.closeCurrentPopup();

                if (onClose != null)
                    onClose.run();
            }

            ImGui.endPopup();
        }

        if (hasErrors()) {
            nextError();
            ImGui.openPopup(trIC("text.mwta.error-popup.title", id, "errorPopup"));
        }
    }

    public void open(Error error, Runnable close) {
        if (close == null) close = () -> {}; // Avoid null pointer exceptions
        errorQueue.add(new Tuple<>(error, close));
    }

    public boolean hasErrors() {
        return !errorQueue.isEmpty();
    }

    private void nextError() {
        Tuple<@NotNull Error, @NotNull Runnable> p = errorQueue.poll();
        if (p == null) {
            return;
        }

        Error e = p.a();
        text.set(e.getTitle() != null ? e.getTitle() : "");
        message.set(e.getMessage() != null ? e.getMessage() : "");
        onClose = p.b();
    }

    @Override
    public void reportError(Error error, Runnable onClose) {
        open(error, onClose);
    }
}
