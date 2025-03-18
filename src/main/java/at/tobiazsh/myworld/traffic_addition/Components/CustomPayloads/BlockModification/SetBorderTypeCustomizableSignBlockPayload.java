package at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification;


/*
 * @created 13/09/2024 (DD/MM/YYYY) - 23:55
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

public record SetBorderTypeCustomizableSignBlockPayload(BlockPos pos, String modelPath) implements CustomPayload {

    public static final CustomPayload.Id<SetBorderTypeCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_border_type_customizable_sign_block_payload"));
    public static final PacketCodec<ByteBuf, SetBorderTypeCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetBorderTypeCustomizableSignBlockPayload::pos,
            PacketCodecs.STRING, SetBorderTypeCustomizableSignBlockPayload::modelPath,
            SetBorderTypeCustomizableSignBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
