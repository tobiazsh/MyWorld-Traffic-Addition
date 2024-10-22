package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads;


/*
 * @created 21/09/2024 (DD/MM/YYYY) - 00:35
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SetSignPolePositionsCustomizableSignBlockPayload(BlockPos pos, String blockPosString) implements CustomPayload {

    public static final CustomPayload.Id<SetSignPolePositionsCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_sign_pole_positions_customizable_sign_block_payload"));

    public static final PacketCodec<PacketByteBuf, SetSignPolePositionsCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, SetSignPolePositionsCustomizableSignBlockPayload::pos,
        PacketCodecs.STRING, SetSignPolePositionsCustomizableSignBlockPayload::blockPosString,
        SetSignPolePositionsCustomizableSignBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
