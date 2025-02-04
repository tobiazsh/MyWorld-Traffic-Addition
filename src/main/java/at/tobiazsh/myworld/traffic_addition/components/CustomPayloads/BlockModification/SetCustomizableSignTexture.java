package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.BlockModification;


/*
 * @created 07/10/2024 (DD/MM/YYYY) - 21:50
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SetCustomizableSignTexture(BlockPos pos, String json) implements CustomPayload {

	public static final CustomPayload.Id<SetCustomizableSignTexture> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block_sign_texture_change"));

	public static final PacketCodec<ByteBuf, SetCustomizableSignTexture> CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, SetCustomizableSignTexture::pos,
			PacketCodecs.STRING, SetCustomizableSignTexture::json,
			SetCustomizableSignTexture::new
	);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return Id;
	}
}
