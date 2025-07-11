package at.tobiazsh.myworld.traffic_addition.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GlobalReceiverClient<T extends CustomPayload> {
    public CustomPayload.Id<T> payloadId;
    public PayloadReceivedActionClient<T> onReceive;

    public GlobalReceiverClient(CustomPayload.Id<T> id, PayloadReceivedActionClient<T> runnable) {
        this.payloadId = id;
        this.onReceive = runnable;
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface PayloadReceivedActionClient<T extends CustomPayload>  {
        void onReceive(T payload);
    }

    public static <T extends CustomPayload> void registerGlobalReceiverClient(GlobalReceiverClient<T> r) {
        ClientPlayNetworking.registerGlobalReceiver(r.payloadId, (payload, context) -> context.client().execute(() -> r.onReceive.onReceive(payload)));
    }

    public static void bulkRegisterGlobalReceiversClient(List<GlobalReceiverClient<? extends CustomPayload>> list) {
        list.forEach(GlobalReceiverClient::registerGlobalReceiverClient);
    }
}
