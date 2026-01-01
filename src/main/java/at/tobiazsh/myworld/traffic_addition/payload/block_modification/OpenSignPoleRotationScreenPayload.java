package at.tobiazsh.myworld.traffic_addition.payload.block_modification;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record OpenSignPoleRotationScreenPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenSignPoleRotationScreenPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "open_sign_pole_rotation_screen"));
    public static final StreamCodec<ByteBuf, OpenSignPoleRotationScreenPayload> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, OpenSignPoleRotationScreenPayload::pos, OpenSignPoleRotationScreenPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
