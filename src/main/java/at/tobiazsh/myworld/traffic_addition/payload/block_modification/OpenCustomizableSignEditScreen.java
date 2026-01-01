package at.tobiazsh.myworld.traffic_addition.payload.block_modification;


/*
 * @created 08/09/2024 (DD/MM/YYYY) - 00:29
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record OpenCustomizableSignEditScreen(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenCustomizableSignEditScreen> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "open_customizable_sign_edit_screen"));
    public static StreamCodec<ByteBuf, OpenCustomizableSignEditScreen> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenCustomizableSignEditScreen::pos,
            OpenCustomizableSignEditScreen::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
