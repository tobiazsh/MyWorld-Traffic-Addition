package at.tobiazsh.myworld.traffic_addition.Utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public class SmartPayload<T extends CustomPayload> {

    public enum RECEIVE_ENVIRONMENT {
        CLIENT,
        SERVER
    }

    public CustomPayload.Id<T> id;
    public PayloadReceivedAction<T> onReceive;
    public PacketCodec<ByteBuf, T> codec;
    public RECEIVE_ENVIRONMENT env;

    public SmartPayload(CustomPayload.Id<T> id, PayloadReceivedAction<T> onReceive, PacketCodec<ByteBuf, T> codec, RECEIVE_ENVIRONMENT env) {
        this.id = id;
        this.onReceive = onReceive;
        this.codec = codec;
        this.env = env;
    }

    @FunctionalInterface
    public interface PayloadReceivedAction<T extends CustomPayload>  {
        void onReceive(T payload, ServerPlayNetworking.Context context);
    }

    public static <T extends CustomPayload> void registerGlobalReceiver(SmartPayload<T> r) {
        if (r.env == RECEIVE_ENVIRONMENT.CLIENT)
            MyWorldTrafficAddition.LOGGER.atError().log("Cannot register a client receiver (payload) in server side! Please use the client side method!");
        else
            ServerPlayNetworking.registerGlobalReceiver(r.id, (payload, context) -> context.server().execute(() -> r.onReceive.onReceive(payload, context)));
    }

    public static void bulkRegisterGlobalReceivers(List<SmartPayload<? extends CustomPayload>> list) {
        if (list.stream().anyMatch(r -> r.env == RECEIVE_ENVIRONMENT.CLIENT))
            MyWorldTrafficAddition.LOGGER.atError().log("Bulk Register failed! Cannot register a client receivers (payload) in server side! Please use the client side method!");
        else
            list.forEach(SmartPayload::registerGlobalReceiver);
    }

    public void registerPayload() {
        if (this.env == RECEIVE_ENVIRONMENT.SERVER)
            PayloadTypeRegistry.playC2S().register(this.id, this.codec);
        else if (this.env == RECEIVE_ENVIRONMENT.CLIENT)
            PayloadTypeRegistry.playS2C().register(this.id, this.codec);
        else
            MyWorldTrafficAddition.LOGGER.atError().log("Cannot register a payload without a receiving environment!");
    }

    public static void bulkRegisterPayloads(List<SmartPayload<? extends CustomPayload>> list) {
        list.forEach(SmartPayload::registerPayload);
    }
}