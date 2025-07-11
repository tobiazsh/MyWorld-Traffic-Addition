package at.tobiazsh.myworld.traffic_addition.networking;

import net.minecraft.util.Identifier;

public class ChunkTransferData {
    private final Identifier protocolId;
    private final int totalChunks;
    private final int totalSize;
    private final byte[][] chunks;
    private int receivedChunks = 0;

    public ChunkTransferData(Identifier protocolId, int totalChunks, int totalSize) {
        this.protocolId = protocolId;
        this.totalChunks = totalChunks;
        this.totalSize = totalSize;
        this.chunks = new byte[totalChunks][];
    }

    public void addChunk(int index, byte[] data) {
        if (index > totalChunks || chunks[index] != null)
            return; // If index is bigger than the total amount of chunks or the chunk is already received, return early

        chunks[index] = data;
        receivedChunks++;
    }

    public boolean isComplete() {
        return receivedChunks == totalChunks;
    }

    public byte[] assembleData() {
        byte[] data = new byte[totalSize]; // Ensure the array is sized to dataSize
        int offset = 0;

        for (int i = 0; i < chunks.length; i++) {
            byte[] chunk = chunks[i];
            if (chunk == null) {
                throw new IllegalStateException("Missing chunk at index " + i);
            }

            // Calculate the number of bytes to copy for the current chunk
            int lengthToCopy = Math.min(chunk.length, totalSize - offset);

            // Copy the chunk into the destination array
            System.arraycopy(chunk, 0, data, offset, lengthToCopy);
            offset += lengthToCopy;

            // Stop copying if we've reached the total data size
            if (offset >= totalSize) {
                break;
            }
        }

        return data;
    }

    public Identifier getProtocolId() {
        return protocolId;
    }
}