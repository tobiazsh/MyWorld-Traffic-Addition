package at.tobiazsh.myworld.traffic_addition.mixin.client;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.tracy.TracyFrameCapturer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemAccessor {
	@Inject(method = "flipFrame", at = @At("HEAD"))
	private static void render(long window, TracyFrameCapturer capturer, CallbackInfo ci) {
		ImGuiRenderer.render();
	}
}