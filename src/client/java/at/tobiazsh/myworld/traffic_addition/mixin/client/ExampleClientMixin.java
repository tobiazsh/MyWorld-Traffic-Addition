package at.tobiazsh.myworld.traffic_addition.mixin.client;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Mixin(RenderSystem.class)
public class ExampleClientMixin {
	@Inject(method = "flipFrame", at = @At("HEAD"))
	private static void render(long window, CallbackInfo ci) {
		ImGuiRenderer.render();
	}
}