package at.tobiazsh.myworld.traffic_addition.networking;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class CustomNetworking<T> {
    public static final int CHUNK_SIZE = 32000; // Slightly under 32 KB of data per Chunk

    private final Map<UUID, ChunkTransferData> activeTransfers = new HashMap<>();
    private final Map<Identifier, T> protocolHandlers = new HashMap<>();

    /**
     * Register a handler for a specific protocol ID
     */
    public void registerProtocolHandler(Identifier protocolId, T handler) {
        protocolHandlers.put(protocolId, handler);
    }

    /**
     * Process a received chunked data payload
     * @param payload The received payload
     * @param executeHandler Function to execute the handler when transfer is complete
     */
    public void processChunkedPayload(ChunkedDataPayload payload, TriConsumer<Identifier, byte[], T> executeHandler) {
        UUID transferId = payload.transferId();
        Identifier protocolId = payload.protocolId();

        // Metadata packet has -1 as chunk index
        if (payload.chunkIndex() == -1) {
            activeTransfers.put(transferId, new ChunkTransferData(protocolId, payload.totalChunks(), payload.dataSize()));
            return;
        }

        // Handle data chunk
        if (!activeTransfers.containsKey(transferId)) return; // Ignore chunks for unknown transfers

        ChunkTransferData transfer = activeTransfers.get(transferId);
        transfer.addChunk(payload.chunkIndex(), payload.data());

        // Check if transfer is complete
        if (transfer.isComplete()) {
            byte[] completeData = transfer.assembleData();
            T handler = protocolHandlers.get(transfer.getProtocolId());

            activeTransfers.remove(transferId);

            if (handler != null)
                executeHandler.accept(transfer.getProtocolId(), completeData, handler);
        }
    }

    /**
     * Helper interface for executing handlers with three parameters
     */
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    /**
     * Registers a receiver for the chunked data payloads
     */
    public abstract void registerReceivers();
}
