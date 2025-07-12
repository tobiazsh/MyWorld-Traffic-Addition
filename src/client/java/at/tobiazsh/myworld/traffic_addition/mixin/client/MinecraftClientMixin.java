package at.tobiazsh.myworld.traffic_addition.mixin.client;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 16:23
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;

import at.tobiazsh.myworld.traffic_addition.utils.CustomMinecraftFont;
import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements MinecraftClientAccessor {

    @Shadow @Final private Window window;

    // ---- ImGui ----

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initImGui(CallbackInfo ci) {
        ImGuiImpl.create(window.getHandle());
    }

    @Inject(method = "close", at = @At("RETURN"))
    public void closeImGui(CallbackInfo ci) {
        ImGuiImpl.dispose();
    }

    // ---- Font ----

    // Injects after fontManager has been initialized
    @Inject(method = "onFontOptionsChanged", at = @At("TAIL"))
    private void createTTFRenderer(CallbackInfo ci) {
        CustomMinecraftFont.initFonts();
    }

    @Shadow
    @Final
    private FontManager fontManager;

    @Override
    public FontManager myworldTrafficAddition$getFontManager() {
        return this.fontManager;
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo ci) {
        MyWorldTrafficAdditionClient.onStopGame();
    }

}
