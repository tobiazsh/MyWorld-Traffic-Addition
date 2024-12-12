package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 21:09
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowAboutWindow(boolean dummy) implements CustomPayload {
    public static CustomPayload.Id<ShowAboutWindow> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "show_about_window_payload"));
    public static PacketCodec<ByteBuf, ShowAboutWindow> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, ShowAboutWindow::dummy,
            ShowAboutWindow::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
