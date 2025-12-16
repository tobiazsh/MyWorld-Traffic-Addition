package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 18:40
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public record SetShouldRenderSignPolePayload(BlockPos pos, boolean value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetShouldRenderSignPolePayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_should_render_sign_pole_payload"));
    public static final StreamCodec<ByteBuf, SetShouldRenderSignPolePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetShouldRenderSignPolePayload::pos,
            ByteBufCodecs.BOOL, SetShouldRenderSignPolePayload::value,
            SetShouldRenderSignPolePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
