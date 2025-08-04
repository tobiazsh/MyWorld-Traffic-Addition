package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 14:27
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

public record SetRenderStateCustomizableSignBlockPayload(BlockPos pos, boolean renderState) implements CustomPayload {
    public static final CustomPayload.Id<SetRenderStateCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_render_state_customizable_sign_block_payload"));

    public static final PacketCodec<ByteBuf, SetRenderStateCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetRenderStateCustomizableSignBlockPayload::pos,
            PacketCodecs.BOOLEAN, SetRenderStateCustomizableSignBlockPayload::renderState,
            SetRenderStateCustomizableSignBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
