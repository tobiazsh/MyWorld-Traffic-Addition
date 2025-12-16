package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 14:06
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
public record SetSignPositionsCustomizableSignBlockPayload(BlockPos pos, byte[] signDistances) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetSignPositionsCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_sign_positions_customizable_sign_block_payload"));

    public static final StreamCodec<ByteBuf, SetSignPositionsCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetSignPositionsCustomizableSignBlockPayload::pos,
            ByteBufCodecs.BYTE_ARRAY, SetSignPositionsCustomizableSignBlockPayload::signDistances,
            SetSignPositionsCustomizableSignBlockPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
