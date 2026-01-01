package at.tobiazsh.myworld.traffic_addition.payload.block_modification;


/*
 * @created 21/09/2024 (DD/MM/YYYY) - 00:35
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
public record SetSignPolePositionsCustomizableSignBlockPayload(BlockPos pos, byte[] bytes) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetSignPolePositionsCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_sign_pole_positions_customizable_sign_block_payload"));

    public static final StreamCodec<ByteBuf, SetSignPolePositionsCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SetSignPolePositionsCustomizableSignBlockPayload::pos,
        ByteBufCodecs.BYTE_ARRAY, SetSignPolePositionsCustomizableSignBlockPayload::bytes,
        SetSignPolePositionsCustomizableSignBlockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
