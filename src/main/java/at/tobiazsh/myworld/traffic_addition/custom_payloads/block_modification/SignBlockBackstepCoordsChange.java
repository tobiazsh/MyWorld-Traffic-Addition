package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 22:41
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SignBlockBackstepCoordsChange(BlockPos pos, float x, float y, float z, Direction direction) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SignBlockBackstepCoordsChange> Id = new CustomPacketPayload.Type<>(Identifier.parse(MyWorldTrafficAddition.MOD_ID + ".sign_block_backstep_coords_change"));
    public static final StreamCodec<ByteBuf, SignBlockBackstepCoordsChange> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SignBlockBackstepCoordsChange::pos,
            ByteBufCodecs.FLOAT, SignBlockBackstepCoordsChange::x,
            ByteBufCodecs.FLOAT, SignBlockBackstepCoordsChange::y,
            ByteBufCodecs.FLOAT, SignBlockBackstepCoordsChange::z,
            Direction.STREAM_CODEC, SignBlockBackstepCoordsChange::direction,
            SignBlockBackstepCoordsChange::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}