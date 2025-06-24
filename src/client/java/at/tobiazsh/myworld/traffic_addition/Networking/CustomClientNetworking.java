package at.tobiazsh.myworld.traffic_addition.Networking;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public class CustomClientNetworking extends CustomNetworking<Consumer<byte[]>> {

    @Override
    public void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ChunkedDataPayload.Id, (payload, context) -> processChunkedPayload(
                payload,
                (protocolId, data, handler) -> context.client().execute(() -> handler.accept(data))
        ));
    }

    /**
     * Send a byte array to the server
     * @param channelName The name of the channel. Has to be the same on server & client
     * @param dataBytes The data to send
     * @param rate The speed of the transfer in milliseconds. If the rate is 200, then every 200 ms a chunk is sent. Use anything smaller than 0 to send all chunks immediately. This is to help network starvation.
     * @param chunkSize The maximum size of a chunk in bytes. Anything higher than 32 KB (32000) or lower than 1 Byte will automatically set it to 32 KB. Together with the rate of transmission, you can control the speed of transmission in KB/ms.
     */
    public void sendBytesToServer(Identifier channelName, byte[] dataBytes, long rate, int chunkSize) {
        UUID transferId = UUID.randomUUID();

        int effectiveChunkSize = chunkSize < 1 || chunkSize > 32000 ? CHUNK_SIZE : chunkSize;
        int chunks = (int) Math.ceil((double) dataBytes.length / effectiveChunkSize);

        ChunkedDataPayload metadataPayload = ChunkedDataPayload.createMetadata(channelName, transferId, chunks, dataBytes.length); // Set up metadata
        ClientPlayNetworking.send(metadataPayload); // Send metadata

        Thread sender = new Thread(() -> {
            try {
                for (int i = 0; i < chunks; i++) {
                    int start = i * effectiveChunkSize; // If chunkSize is bigger than 32000 or smaller than 1, use the default maximum chunk size, otherwise, do as directed; Start of the current chunk.
                    int end = Math.min(start + effectiveChunkSize, dataBytes.length);  // End of the current chunk
                    byte[] chunk = Arrays.copyOfRange(dataBytes, start, end); // Get chunk

                    ChunkedDataPayload chunkPayload = ChunkedDataPayload.createChunk(channelName, transferId, i, chunk); // Make Payload
                    ClientPlayNetworking.send(chunkPayload); // Send to Server

                    if (rate > 0) {
                        Thread.sleep(rate); // Sleep to control the rate of transmission
                    }
                }
            } catch (InterruptedException e) {
                MyWorldTrafficAddition.LOGGER.error("Error while sending data to server: ");
                MyWorldTrafficAddition.LOGGER.error(e.getMessage());
            }
        });

        sender.setName("ChunkedDataTransfer-" + channelName.toString());
        sender.start();
    }

    /**
     * Send a string to the server
     * @param identifier The name of the channel (ex. Identifier.of(MyWorldTrafficAddition.MOD_ID, "some_data_sender")
     * @param data The data to send
     */
    public void sendStringToServer(Identifier identifier, String data) {
        sendBytesToServer(identifier, data.getBytes(), -1, -1); // In full speed
    }

    // Singleton instance
    private static CustomClientNetworking INSTANCE;

    public static CustomClientNetworking getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CustomClientNetworking();

        return INSTANCE;
    }
}
