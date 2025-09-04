package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 21:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record SignBlockTextureChangePayload(BlockPos pos, String texturePath, RegistryKey<World> worldRegistryKey) implements CustomPayload {

    public static final CustomPayload.Id<SignBlockTextureChangePayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID + ".sign_block_texture_change"));
    public static final PacketCodec<ByteBuf, SignBlockTextureChangePayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SignBlockTextureChangePayload::pos,
            PacketCodecs.STRING, SignBlockTextureChangePayload::texturePath,
            RegistryKey.createPacketCodec(RegistryKeys.WORLD), SignBlockTextureChangePayload::worldRegistryKey,
            SignBlockTextureChangePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return Id;
    }
}
