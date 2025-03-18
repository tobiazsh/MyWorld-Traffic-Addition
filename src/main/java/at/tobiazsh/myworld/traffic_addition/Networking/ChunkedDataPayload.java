package at.tobiazsh.myworld.traffic_addition.Networking;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record ChunkedDataPayload(Identifier protocolId, UUID transferId, int chunkIndex, int totalChunks, int dataSize, byte[] data) implements CustomPayload {

    public static final CustomPayload.Id<ChunkedDataPayload> Id = new CustomPayload.Id<>(
            Identifier.of(MyWorldTrafficAddition.MOD_ID, "chunked_data")
    );

    public static final PacketCodec<ByteBuf, ChunkedDataPayload> CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, ChunkedDataPayload::protocolId,
            Uuids.PACKET_CODEC, ChunkedDataPayload::transferId,
            PacketCodecs.INTEGER, ChunkedDataPayload::chunkIndex,
            PacketCodecs.INTEGER, ChunkedDataPayload::totalChunks,
            PacketCodecs.INTEGER, ChunkedDataPayload::dataSize,
            PacketCodecs.BYTE_ARRAY, ChunkedDataPayload::data,
            ChunkedDataPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }

    public static ChunkedDataPayload createMetadata(Identifier protocolId, UUID transferId, int totalChunks, int totalSize) {
        return new ChunkedDataPayload(protocolId, transferId, -1, totalChunks, totalSize, new byte[0]);
    }

    public static ChunkedDataPayload createChunk(Identifier protocolId, UUID transferId, int chunkIndex, byte[] chunkData) {
        return new ChunkedDataPayload(protocolId, transferId, chunkIndex, 0, chunkData.length, chunkData);
    }
}
