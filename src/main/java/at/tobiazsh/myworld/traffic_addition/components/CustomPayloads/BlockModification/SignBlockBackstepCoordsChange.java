package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.BlockModification;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 22:41
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
import net.minecraft.util.math.Direction;

public record SignBlockBackstepCoordsChange(BlockPos pos, float x, float y, float z, Direction direction) implements CustomPayload {

    public static final CustomPayload.Id<SignBlockBackstepCoordsChange> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID + ".sign_block_backstep_coords_change"));
    public static final PacketCodec<ByteBuf, SignBlockBackstepCoordsChange> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SignBlockBackstepCoordsChange::pos,
            PacketCodecs.FLOAT, SignBlockBackstepCoordsChange::x,
            PacketCodecs.FLOAT, SignBlockBackstepCoordsChange::y,
            PacketCodecs.FLOAT, SignBlockBackstepCoordsChange::z,
            Direction.PACKET_CODEC, SignBlockBackstepCoordsChange::direction,
            SignBlockBackstepCoordsChange::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return Id;
    }
}