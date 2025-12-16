package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 16:53
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public record SetRotationCustomizableSignBlockPayload(BlockPos pos, int rotation) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetRotationCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_rotation_customizable_sign_block_rotation"));

    public static final StreamCodec<ByteBuf, SetRotationCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetRotationCustomizableSignBlockPayload::pos,
            ByteBufCodecs.INT, SetRotationCustomizableSignBlockPayload::rotation,
            SetRotationCustomizableSignBlockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
