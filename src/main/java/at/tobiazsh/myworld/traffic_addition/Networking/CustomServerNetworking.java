package at.tobiazsh.myworld.traffic_addition.Networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiConsumer;

public class CustomServerNetworking extends CustomNetworking<BiConsumer<ServerPlayerEntity, byte[]>> {

    @Override
    public void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ChunkedDataPayload.Id, (payload, context) -> {
            processChunkedPayload(
                    payload,
                    (protocolId, data, handler) -> context.server().execute(() -> handler.accept(context.player(), data))
            );
        });
    }

    public void sendBytesToClient(ServerPlayerEntity player, Identifier channelName, byte[] dataBytes) {
        UUID transferId = UUID.randomUUID();
        int chunks = (int) Math.ceil((double) dataBytes.length / CHUNK_SIZE);

        ChunkedDataPayload metadataPayload = ChunkedDataPayload.createMetadata(channelName, transferId, chunks, dataBytes.length);
        ServerPlayNetworking.send(player, metadataPayload);

        for (int i = 0; i < chunks; i++) {
            int start = i * CHUNK_SIZE;
            int end = Math.min(start + CHUNK_SIZE, dataBytes.length);
            byte[] chunk = Arrays.copyOfRange(dataBytes, start, end);

            ChunkedDataPayload chunkPayload = ChunkedDataPayload.createChunk(channelName, transferId, i, chunk);
            ServerPlayNetworking.send(player, chunkPayload);
        }
    }

    /**
     * Send a string to the server
     * @param player The player to send the data to
     * @param identifier The name of the channel (ex. Identifier.of(MyWorldTrafficAddition.MOD_ID, "some_data_sender")
     * @param data The data to send
     */
    public void sendStringToClient(ServerPlayerEntity player, Identifier identifier, String data) {
        sendBytesToClient(player, identifier, data.getBytes());
    }

    // Singleton instance
    private static CustomServerNetworking INSTANCE;

    public static CustomServerNetworking getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CustomServerNetworking();

        return INSTANCE;
    }
}
