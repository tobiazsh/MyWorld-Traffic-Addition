package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 14:27
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
public record SetRenderStateCustomizableSignBlockPayload(BlockPos pos, boolean renderState) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetRenderStateCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_render_state_customizable_sign_block_payload"));

    public static final StreamCodec<ByteBuf, SetRenderStateCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetRenderStateCustomizableSignBlockPayload::pos,
            ByteBufCodecs.BOOL, SetRenderStateCustomizableSignBlockPayload::renderState,
            SetRenderStateCustomizableSignBlockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
