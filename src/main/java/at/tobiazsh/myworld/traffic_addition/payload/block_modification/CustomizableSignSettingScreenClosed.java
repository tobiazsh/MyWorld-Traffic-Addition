package at.tobiazsh.myworld.traffic_addition.payload.block_modification;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record CustomizableSignSettingScreenClosed(BlockPos masterSignPos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CustomizableSignSettingScreenClosed> Id = new Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "customizable_sign_setting_screen_closed"));
    public static final StreamCodec<ByteBuf, CustomizableSignSettingScreenClosed> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CustomizableSignSettingScreenClosed::masterSignPos,
            CustomizableSignSettingScreenClosed::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }

}
