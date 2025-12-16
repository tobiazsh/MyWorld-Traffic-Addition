package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 21:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record SignBlockTextureChangePayload(BlockPos pos, String texturePath, ResourceKey<Level> worldRegistryKey) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SignBlockTextureChangePayload> Id = new CustomPacketPayload.Type<>(Identifier.parse(MyWorldTrafficAddition.MOD_ID + ".sign_block_texture_change"));
    public static final StreamCodec<ByteBuf, SignBlockTextureChangePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SignBlockTextureChangePayload::pos,
            ByteBufCodecs.STRING_UTF8, SignBlockTextureChangePayload::texturePath,
            ResourceKey.streamCodec(Registries.DIMENSION), SignBlockTextureChangePayload::worldRegistryKey,
            SignBlockTextureChangePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
