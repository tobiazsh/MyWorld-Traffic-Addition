package at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification;


/*
 * @created 08/09/2024 (DD/MM/YYYY) - 00:29
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OpenCustomizableSignEditScreen(BlockPos pos) implements CustomPayload {

    public static final CustomPayload.Id<OpenCustomizableSignEditScreen> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "open_customizable_sign_edit_screen"));
    public static PacketCodec<ByteBuf, OpenCustomizableSignEditScreen> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, OpenCustomizableSignEditScreen::pos,
            OpenCustomizableSignEditScreen::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
