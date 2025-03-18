package at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OpenSignSelectionPayload(BlockPos pos, int selection_type) implements CustomPayload {

    public static final CustomPayload.Id<OpenSignSelectionPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "open_sign_selection_screen"));
    public static final PacketCodec<ByteBuf, OpenSignSelectionPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, OpenSignSelectionPayload::pos,
        PacketCodecs.INTEGER, OpenSignSelectionPayload::selection_type,
        OpenSignSelectionPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return Id; }
}
