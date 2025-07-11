package at.tobiazsh.myworld.traffic_addition.components.custom_payloads.block_modification;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 22:02
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

public record SignBlockRotationPayload(BlockPos pos, int rotation) implements CustomPayload {

    public static final CustomPayload.Id<SignBlockRotationPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID + "sign_block_rotation_change"));
    public static final PacketCodec<ByteBuf, SignBlockRotationPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SignBlockRotationPayload::pos,
            PacketCodecs.INTEGER, SignBlockRotationPayload::rotation,
            SignBlockRotationPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return Id;
    }
}
