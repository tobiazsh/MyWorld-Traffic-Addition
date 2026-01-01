package at.tobiazsh.myworld.traffic_addition.payload.block_modification;


/*
 * @created 13/09/2024 (DD/MM/YYYY) - 23:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SetMasterCustomizableSignBlockPayload(BlockPos pos, Boolean shouldMaster, BlockPos master) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetMasterCustomizableSignBlockPayload> Id = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block_master_change"));
    public static final StreamCodec<ByteBuf, SetMasterCustomizableSignBlockPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetMasterCustomizableSignBlockPayload::pos,
            ByteBufCodecs.BOOL, SetMasterCustomizableSignBlockPayload::shouldMaster,
            BlockPos.STREAM_CODEC, SetMasterCustomizableSignBlockPayload::master,
            SetMasterCustomizableSignBlockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Id;
    }
}
