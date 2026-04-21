package at.tobiazsh.myworld.traffic_addition.mixin.client;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        ImGuiImpl.beginImGuiRendering();
        ImGuiRenderer.render();
        ImGuiImpl.endImGuiRendering();
    }
}
