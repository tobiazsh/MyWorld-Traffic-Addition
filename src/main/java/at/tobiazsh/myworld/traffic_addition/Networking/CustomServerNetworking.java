package at.tobiazsh.myworld.traffic_addition.Networking;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class CustomServerNetworking extends CustomNetworking<BiConsumer<ServerPlayerEntity, byte[]>> {

    @Override
    public void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ChunkedDataPayload.Id, (payload, context) -> processChunkedPayload(
                payload,
                (protocolId, data, handler) -> context.server().execute(() -> handler.accept(context.player(), data))
        ));
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(32); // Thread pool for sending data

    /**
     * Send a byte array to the server
     * @param player The player to send it to
     * @param channelName The name of the channel. Has to be the same on server & client
     * @param dataBytes The data to send
     * @param rate The speed of the transfer in milliseconds. If the rate is 200, then every 200 ms a chunk is sent. Use anything smaller than 0 to send all chunks immediately. This is to help network starvation.
     * @param chunkSize The maximum size of a chunk in bytes. Anything higher than 32 KB (32000) or lower than 1 Byte will automatically set it to 32 KB. Together with the rate of transmission, you can control the speed of transmission in KB/ms.
     */
    public void sendBytesToClient(ServerPlayerEntity player, Identifier channelName, byte[] dataBytes, long rate, int chunkSize) {
        UUID transferId = UUID.randomUUID();

        int effectiveChunkSize = chunkSize < 1 || chunkSize > 32000 ? CHUNK_SIZE : chunkSize;
        int chunks = (int) Math.ceil((double) dataBytes.length / effectiveChunkSize);

        ChunkedDataPayload metadataPayload = ChunkedDataPayload.createMetadata(channelName, transferId, chunks, dataBytes.length);
        ServerPlayNetworking.send(player, metadataPayload);

        executor.submit(() -> {
            try {
                for (int i = 0; i < chunks; i++) {
                    int start = i * effectiveChunkSize;
                    int end = Math.min(start + effectiveChunkSize, dataBytes.length);
                    byte[] chunk = Arrays.copyOfRange(dataBytes, start, end);

                    ChunkedDataPayload chunkPayload = ChunkedDataPayload.createChunk(channelName, transferId, i, chunk);
                    ServerPlayNetworking.send(player, chunkPayload);

                    if (rate > 0) {
                        Thread.sleep(rate);
                    }
                }
            } catch (InterruptedException e) {
                MyWorldTrafficAddition.LOGGER.error("Error while sending data to client: ");
                MyWorldTrafficAddition.LOGGER.error(e.getMessage());
            }
        });
    }

    /**
     * Send a string to the server
     * @param player The player to send the data to
     * @param identifier The name of the channel (ex. Identifier.of(MyWorldTrafficAddition.MOD_ID, "some_data_sender")
     * @param data The data to send
     * Does not limit transmission speed or chunk size! Use {@link #sendBytesToClient(ServerPlayerEntity, Identifier, byte[], long, int)} for that.
     */
    public void sendStringToClient(ServerPlayerEntity player, Identifier identifier, String data) {
        sendBytesToClient(player, identifier, data.getBytes(), -1, -1);
    }

    // Singleton instance
    private static CustomServerNetworking INSTANCE;

    public static CustomServerNetworking getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CustomServerNetworking();

        return INSTANCE;
    }
}
