package at.tobiazsh.myworld.traffic_addition.Networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public class CustomClientNetworking extends CustomNetworking<Consumer<byte[]>> {

    @Override
    public void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ChunkedDataPayload.Id, (payload, context) -> {
            processChunkedPayload(
                    payload,
                    (protocolId, data, handler) -> context.client().execute(() -> handler.accept(data))
            );
        });
    }

    public void sendBytesToServer(Identifier channelName, byte[] dataBytes) {
        UUID transferId = UUID.randomUUID();
        int chunks = (int) Math.ceil((double) dataBytes.length / CHUNK_SIZE);

        ChunkedDataPayload metadataPayload = ChunkedDataPayload.createMetadata(channelName, transferId, chunks, dataBytes.length);
        ClientPlayNetworking.send(metadataPayload);

        for (int i = 0; i < chunks; i++) {
            int start = i * CHUNK_SIZE;
            int end = Math.min(start + CHUNK_SIZE, dataBytes.length);
            byte[] chunk = Arrays.copyOfRange(dataBytes, start, end);

            ChunkedDataPayload chunkPayload = ChunkedDataPayload.createChunk(channelName, transferId, i, chunk);
            ClientPlayNetworking.send(chunkPayload);
        }
    }

    /**
     * Send a string to the server
     * @param identifier The name of the channel (ex. Identifier.of(MyWorldTrafficAddition.MOD_ID, "some_data_sender")
     * @param data The data to send
     */
    public void sendStringToServer(Identifier identifier, String data) {
        sendBytesToServer(identifier, data.getBytes());
    }

    // Singleton instance
    private static CustomClientNetworking INSTANCE;

    public static CustomClientNetworking getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CustomClientNetworking();

        return INSTANCE;
    }
}
