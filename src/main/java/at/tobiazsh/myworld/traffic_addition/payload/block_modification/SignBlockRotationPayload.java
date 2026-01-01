package at.tobiazsh.myworld.traffic_addition.payload.block_modification;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 22:02
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
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SignBlockRotationPayload(BlockPos pos, int rotation) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SignBlockRotationPayload> Id = new CustomPacketPayload.Type<>(Identifier.parse(MyWorldTrafficAddition.MOD_ID + "sign_block_rotation_change"));
    public static final StreamCodec<ByteBuf, SignBlockRotationPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SignBlockRotationPayload::pos,
            ByteBufCodecs.INT, SignBlockRotationPayload::rotation,
            SignBlockRotationPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
