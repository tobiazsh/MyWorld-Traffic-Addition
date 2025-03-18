package at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 18:40
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SetShouldRenderSignPolePayload(BlockPos pos, boolean value) implements CustomPayload {

    public static final CustomPayload.Id<SetShouldRenderSignPolePayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_should_render_sign_pole_payload"));
    public static final PacketCodec<ByteBuf, SetShouldRenderSignPolePayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetShouldRenderSignPolePayload::pos,
            PacketCodecs.BOOLEAN, SetShouldRenderSignPolePayload::value,
            SetShouldRenderSignPolePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
