package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowImGuiWindow(int windowId) implements CustomPayload {

    public static final CustomPayload.Id<ShowImGuiWindow> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "show_imgui_window"));
    public static final PacketCodec<ByteBuf, ShowImGuiWindow> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ShowImGuiWindow::windowId,
            ShowImGuiWindow::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
