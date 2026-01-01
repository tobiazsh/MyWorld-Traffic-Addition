package at.tobiazsh.myworld.traffic_addition.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GlobalReceiverClient<T extends CustomPacketPayload> {
    public CustomPacketPayload.Type<@NotNull T> payloadId;
    public PayloadReceivedActionClient<T> onReceive;

    public GlobalReceiverClient(CustomPacketPayload.Type<@NotNull T> id, PayloadReceivedActionClient<T> runnable) {
        this.payloadId = id;
        this.onReceive = runnable;
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface PayloadReceivedActionClient<T extends CustomPacketPayload>  {
        void onReceive(T payload);
    }

    public static <T extends CustomPacketPayload> void registerGlobalReceiverClient(GlobalReceiverClient<T> r) {
        ClientPlayNetworking.registerGlobalReceiver(r.payloadId, (payload, context) -> context.client().execute(() -> r.onReceive.onReceive(payload)));
    }

    public static void bulkRegisterGlobalReceiversClient(List<GlobalReceiverClient<? extends CustomPacketPayload>> list) {
        list.forEach(GlobalReceiverClient::registerGlobalReceiverClient);
    }
}
