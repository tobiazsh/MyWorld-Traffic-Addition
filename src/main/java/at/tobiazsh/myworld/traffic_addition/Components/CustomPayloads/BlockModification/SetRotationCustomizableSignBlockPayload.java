package at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 16:53
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

public record SetRotationCustomizableSignBlockPayload(BlockPos pos, int rotation) implements CustomPayload {
    public static final CustomPayload.Id<SetRotationCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_rotation_customizable_sign_block_rotation"));

    public static final PacketCodec<ByteBuf, SetRotationCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetRotationCustomizableSignBlockPayload::pos,
            PacketCodecs.INTEGER, SetRotationCustomizableSignBlockPayload::rotation,
            SetRotationCustomizableSignBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
