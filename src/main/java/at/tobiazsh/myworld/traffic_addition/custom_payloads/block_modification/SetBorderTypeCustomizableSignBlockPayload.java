package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 13/09/2024 (DD/MM/YYYY) - 23:55
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public record SetBorderTypeCustomizableSignBlockPayload(BlockPos pos, String borders) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetBorderTypeCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_border_type_customizable_sign_block_payload"));
    public static final StreamCodec<ByteBuf, SetBorderTypeCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetBorderTypeCustomizableSignBlockPayload::pos,
            ByteBufCodecs.STRING_UTF8, SetBorderTypeCustomizableSignBlockPayload::borders,
            SetBorderTypeCustomizableSignBlockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
