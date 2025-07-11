package at.tobiazsh.myworld.traffic_addition.components.custom_payloads.block_modification;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UpdateTextureVarsCustomizableSignBlockPayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<UpdateTextureVarsCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID + ".update_texture_vars_customizable_sign_block"));
    public static final PacketCodec<ByteBuf, UpdateTextureVarsCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, UpdateTextureVarsCustomizableSignBlockPayload::pos,
            UpdateTextureVarsCustomizableSignBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
