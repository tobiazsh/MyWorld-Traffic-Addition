package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 14:06
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

public record SetSignPositionsCustomizableSignBlockPayload(BlockPos pos, String blockPosString) implements CustomPayload {
    public static final CustomPayload.Id<SetSignPositionsCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_sign_positions_customizable_sign_block_payload"));

    public static final PacketCodec<ByteBuf, SetSignPositionsCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetSignPositionsCustomizableSignBlockPayload::pos,
            PacketCodecs.STRING, SetSignPositionsCustomizableSignBlockPayload::blockPosString,
            SetSignPositionsCustomizableSignBlockPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return Id;
    }
}
