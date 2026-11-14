package at.tobiazsh.myworld.traffic_addition.imgui.toast;

import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public class Toaster {
    private final List<Toast> toasts = new ArrayList<>();

    private static final int TOAST_PADDING = 10;

    public void addToast(Toast toast) {
        toasts.add(toast);
    }

    private void sweep() {
        toasts.removeIf(Toast::isExpired);
    }

    public void render() {
        if (toasts.isEmpty()) return; // Nothing to render

        sweep();

        float windowWidth = ImGui.getIO().getDisplaySizeX();
        float windowHeight = ImGui.getIO().getDisplaySizeY();
        float maxWidth = windowWidth * .33f; // Max one third of screen width

        if (windowWidth < 200) {
            maxWidth = windowWidth - 2 * TOAST_PADDING; // Whole screen for REALLY small windows
        } else if (windowWidth < 400) {
            maxWidth = windowWidth * .5f; // Half the screen for small windows
        }

        float y = windowHeight - TOAST_PADDING;

        for (Toast toast : toasts.reversed()) {
            float toastWidth = toast.getWidth(maxWidth);
            float toastHeight = toast.getHeight(maxWidth);

            y -= toastHeight;

            toast.render(windowWidth - toastWidth - TOAST_PADDING, y, maxWidth);

            y -= TOAST_PADDING;
        }
    }
}
