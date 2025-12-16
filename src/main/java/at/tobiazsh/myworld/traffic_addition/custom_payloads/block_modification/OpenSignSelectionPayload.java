package at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification;

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
import org.jspecify.annotations.NullMarked;

@NullMarked
public record OpenSignSelectionPayload(BlockPos pos, int selection_type, ResourceKey<Level> dimensionRegistryKey) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenSignSelectionPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "open_sign_selection_screen"));
    public static final StreamCodec<ByteBuf, OpenSignSelectionPayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OpenSignSelectionPayload::pos,
        ByteBufCodecs.INT, OpenSignSelectionPayload::selection_type,
        ResourceKey.streamCodec(Registries.DIMENSION), OpenSignSelectionPayload::dimensionRegistryKey,
        OpenSignSelectionPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return Id; }
}
