package at.tobiazsh.myworld.traffic_addition.mixin.client;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientAccessor {
    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo ci) {
        MyWorldTrafficAdditionClient.onStopGame();
    }
}
