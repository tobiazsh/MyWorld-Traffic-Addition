package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange;


/*
 * @created 25/09/2024 (DD/MM/YYYY) - 18:44
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowDemoWindow(int i) implements CustomPayload {
    public static final CustomPayload.Id<ShowDemoWindow> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "show_demo_window_payload"));
    public static final PacketCodec<PacketByteBuf, ShowDemoWindow> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ShowDemoWindow::i
            ,ShowDemoWindow::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
