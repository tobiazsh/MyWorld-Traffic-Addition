package at.tobiazsh.myworld.traffic_addition.payload.client_actions;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ClearCSBETextureRenderState(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClearCSBETextureRenderState> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "clear_csbe_texture_render_state"));
    public static StreamCodec<ByteBuf, ClearCSBETextureRenderState> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClearCSBETextureRenderState::pos,
            ClearCSBETextureRenderState::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
