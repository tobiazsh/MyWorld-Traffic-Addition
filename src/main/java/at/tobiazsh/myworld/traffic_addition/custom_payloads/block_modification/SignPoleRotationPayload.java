package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public record SignPoleRotationPayload(BlockPos pos, int rotation) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SignPoleRotationPayload> Id = new CustomPacketPayload.Type<>(Identifier.parse((MyWorldTrafficAddition.MOD_ID + ".sign_pole_rotation")));
    public static final StreamCodec<ByteBuf, SignPoleRotationPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SignPoleRotationPayload::pos,
            ByteBufCodecs.INT, SignPoleRotationPayload::rotation,
            SignPoleRotationPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
