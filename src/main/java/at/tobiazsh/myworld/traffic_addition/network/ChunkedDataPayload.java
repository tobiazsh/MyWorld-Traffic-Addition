package at.tobiazsh.myworld.traffic_addition.network;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record ChunkedDataPayload(Identifier protocolId, UUID transferId, int chunkIndex, int totalChunks, int dataSize, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ChunkedDataPayload> Id = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "chunked_data")
    );

    public static final StreamCodec<ByteBuf, ChunkedDataPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ChunkedDataPayload::protocolId,
            UUIDUtil.STREAM_CODEC, ChunkedDataPayload::transferId,
            ByteBufCodecs.INT, ChunkedDataPayload::chunkIndex,
            ByteBufCodecs.INT, ChunkedDataPayload::totalChunks,
            ByteBufCodecs.INT, ChunkedDataPayload::dataSize,
            ByteBufCodecs.BYTE_ARRAY, ChunkedDataPayload::data,
            ChunkedDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }

    public static ChunkedDataPayload createMetadata(Identifier protocolId, UUID transferId, int totalChunks, int totalSize) {
        return new ChunkedDataPayload(protocolId, transferId, -1, totalChunks, totalSize, new byte[0]);
    }

    public static ChunkedDataPayload createChunk(Identifier protocolId, UUID transferId, int chunkIndex, byte[] chunkData) {
        return new ChunkedDataPayload(protocolId, transferId, chunkIndex, 0, chunkData.length, chunkData);
    }
}
