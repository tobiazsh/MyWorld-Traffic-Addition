package at.tobiazsh.myworld.traffic_addition.payload.block_modification;


/*
 * @created 13/09/2024 (DD/MM/YYYY) - 23:55
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
import org.jetbrains.annotations.NotNull;

public record SetBorderTypeCustomizableSignBlockPayload(BlockPos pos, String borders) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<@NotNull SetBorderTypeCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_border_type_customizable_sign_block_payload"));
    public static final StreamCodec<@NotNull ByteBuf, @NotNull SetBorderTypeCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetBorderTypeCustomizableSignBlockPayload::pos,
            ByteBufCodecs.STRING_UTF8, SetBorderTypeCustomizableSignBlockPayload::borders,
            SetBorderTypeCustomizableSignBlockPayload::new
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return Id;
    }
}
