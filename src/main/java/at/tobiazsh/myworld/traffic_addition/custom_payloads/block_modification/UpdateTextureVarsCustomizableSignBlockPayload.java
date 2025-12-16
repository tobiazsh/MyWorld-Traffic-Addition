package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public record UpdateTextureVarsCustomizableSignBlockPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull UpdateTextureVarsCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.parse(MyWorldTrafficAddition.MOD_ID + ".update_texture_vars_customizable_sign_block"));
    public static final StreamCodec<@NotNull ByteBuf, @NotNull UpdateTextureVarsCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateTextureVarsCustomizableSignBlockPayload::pos,
            UpdateTextureVarsCustomizableSignBlockPayload::new
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return Id;
    }
}
