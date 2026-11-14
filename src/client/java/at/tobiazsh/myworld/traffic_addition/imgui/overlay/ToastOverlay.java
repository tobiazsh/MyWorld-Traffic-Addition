package at.tobiazsh.myworld.traffic_addition.imgui.overlay;

import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.imgui.toast.Toaster;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import dev.tobiazsh.imguib3d.client.overlay.ImGuiOverlay;

import java.util.function.Supplier;

public class ToastOverlay implements ImGuiOverlay {

    private final Supplier<Toaster> toasterProvider;
    private final ImGuiFontScope fontScope = ImGuiFontScope.create();

    public ToastOverlay(Supplier<Toaster> toasterProvider) {
        this.toasterProvider = toasterProvider;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void draw() {
        if (!toasterProvider.get().hasToasts())
            return; // Prevent unnecessary pushes/pops to font scope

        fontScope.push(DefaultFonts.RobotoNormal, DefaultFonts.NORMAL_SIZE);
        toasterProvider.get().render();
        fontScope.pop();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String getId() {
        return "toast_overlay";
    }
}
