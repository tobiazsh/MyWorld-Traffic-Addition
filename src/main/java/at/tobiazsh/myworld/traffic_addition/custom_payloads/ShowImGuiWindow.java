package at.tobiazsh.myworld.traffic_addition.custom_payloads;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ShowImGuiWindow(int windowId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShowImGuiWindow> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "show_imgui_window"));
    public static final StreamCodec<ByteBuf, ShowImGuiWindow> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ShowImGuiWindow::windowId,
            ShowImGuiWindow::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
