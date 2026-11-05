package at.tobiazsh.myworld.traffic_addition.mixin.client;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 16:23
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;

import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftClientAccessor {

    // ---- Font ----

    // Injects after fontManager has been initialized
    @Inject(method = "updateFontOptions", at = @At("TAIL"))
    private void createTTFRenderer(CallbackInfo ci) {
        //CustomMinecraftFont.initFonts();
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
