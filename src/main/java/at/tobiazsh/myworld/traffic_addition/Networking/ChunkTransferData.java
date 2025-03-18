package at.tobiazsh.myworld.traffic_addition.Networking;

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
        byte[] result = new byte[totalSize];
        int position = 0;

        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, position, chunk.length);
            position += chunk.length;
        }

        return result;
    }

    public Identifier getProtocolId() {
        return protocolId;
    }
}