package at.tobiazsh.myworld.traffic_addition.network;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SmartPayload<T extends CustomPacketPayload> {

    public enum RECEIVE_ENVIRONMENT {
        CLIENT,
        SERVER
    }

    public CustomPacketPayload.Type<@NotNull T> id;
    public PayloadReceivedAction<T> onReceive;
    public StreamCodec<@NotNull ByteBuf, @NotNull T> codec;
    public RECEIVE_ENVIRONMENT env;

    public SmartPayload(CustomPacketPayload.Type<@NotNull T> id, PayloadReceivedAction<T> onReceive, StreamCodec<@NotNull ByteBuf, @NotNull T> codec, RECEIVE_ENVIRONMENT env) {
        this.id = id;
        this.onReceive = onReceive;
        this.codec = codec;
        this.env = env;
    }

    @FunctionalInterface
    public interface PayloadReceivedAction<T extends CustomPacketPayload>  {
        void onReceive(T payload, ServerPlayNetworking.Context context);
    }

    public static <T extends CustomPacketPayload> void registerGlobalReceiver(SmartPayload<T> r) {
        if (r.env == RECEIVE_ENVIRONMENT.CLIENT)
            MyWorldTrafficAddition.LOGGER.atError().log("Cannot register a client receiver (payload) in server side! Please use the client side method!");
        else
            ServerPlayNetworking.registerGlobalReceiver(r.id, (payload, context) -> context.server().execute(() -> r.onReceive.onReceive(payload, context)));
    }

    public static void bulkRegisterGlobalReceivers(List<SmartPayload<? extends CustomPacketPayload>> list) {
        if (list.stream().anyMatch(r -> r.env == RECEIVE_ENVIRONMENT.CLIENT))
            MyWorldTrafficAddition.LOGGER.atError().log("Bulk Register failed! Cannot register a client receivers (payload) in server side! Please use the client side method!");
        else
            list.forEach(SmartPayload::registerGlobalReceiver);
    }

    public void registerPayload() {
        if (this.env == RECEIVE_ENVIRONMENT.SERVER)
            PayloadTypeRegistry.serverboundPlay().register(this.id, this.codec);
        else if (this.env == RECEIVE_ENVIRONMENT.CLIENT)
            PayloadTypeRegistry.clientboundPlay().register(this.id, this.codec);
        else
            MyWorldTrafficAddition.LOGGER.atError().log("Cannot register a payload without a receiving environment!");
    }

    public static void bulkRegisterPayloads(List<SmartPayload<? extends CustomPacketPayload>> list) {
        list.forEach(SmartPayload::registerPayload);
    }
}