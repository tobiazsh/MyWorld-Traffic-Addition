package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 17:34
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

// IMPORTANT INFORMATION
// If either height or width is -1, then it's counted as null and will not be set!
// Continue whatever your doing here but continue with caution.

@NullMarked
public record SetSizeCustomizableSignPayload(BlockPos pos, int height, int width) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetSizeCustomizableSignPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_size_customizable_sign_payload"));

    public static final StreamCodec<ByteBuf, SetSizeCustomizableSignPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetSizeCustomizableSignPayload::pos,
            ByteBufCodecs.INT, SetSizeCustomizableSignPayload::height,
            ByteBufCodecs.INT, SetSizeCustomizableSignPayload::width,
            SetSizeCustomizableSignPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
