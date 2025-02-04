package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.BlockModification;


/*
 * @created 13/09/2024 (DD/MM/YYYY) - 23:06
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

public record SetMasterCustomizableSignBlockPayload(BlockPos pos, Boolean shouldMaster, BlockPos master) implements CustomPayload {

    public static final CustomPayload.Id<SetMasterCustomizableSignBlockPayload> Id = new CustomPayload.Id<>(Identifier.of(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block_master_change"));
    public static final PacketCodec<ByteBuf, SetMasterCustomizableSignBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetMasterCustomizableSignBlockPayload::pos,
            PacketCodecs.BOOLEAN, SetMasterCustomizableSignBlockPayload::shouldMaster,
            BlockPos.PACKET_CODEC, SetMasterCustomizableSignBlockPayload::master,
            SetMasterCustomizableSignBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Id;
    }
}
