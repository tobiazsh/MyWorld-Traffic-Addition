package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads;


/*
 * @created 22/09/2024 (DD/MM/YYYY) - 17:34
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

// IMPORTANT INFORMATION
// If either height or width is -1, then it's counted as null and will not be set!
// Continue whatever your doing here but continue with caution.

public record SetSizeCustomizableSignPayload(BlockPos pos, int height, int width) implements CustomPayload {
    public static final CustomPayload.Id<SetSizeCustomizableSignPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_size_customizable_sign_payload"));

    public static final PacketCodec<PacketByteBuf, SetSizeCustomizableSignPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetSizeCustomizableSignPayload::pos,
            PacketCodecs.INTEGER, SetSizeCustomizableSignPayload::height,
            PacketCodecs.INTEGER, SetSizeCustomizableSignPayload::width,
            SetSizeCustomizableSignPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
