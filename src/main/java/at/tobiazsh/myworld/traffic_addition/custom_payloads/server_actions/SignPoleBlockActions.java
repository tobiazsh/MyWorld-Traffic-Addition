package at.tobiazsh.myworld.traffic_addition.custom_payloads.server_actions;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SetShouldRenderSignPolePayload;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SignPoleRotationPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class SignPoleBlockActions {
    public static void handleSetShouldRender(SetShouldRenderSignPolePayload payload, ServerPlayNetworking.Context ctx) {
        ServerPlayer serverPlayer = ctx.player();
        ServerLevel world = serverPlayer.level();
        BlockPos pos = payload.pos();
        boolean value = payload.value();

        if (world.getBlockEntity(pos) instanceof SignPoleBlockEntity blockEntity)
            world.getServer().execute(() -> blockEntity.setShouldRender(value));
    }

    public static void handleRotation(SignPoleRotationPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer serverPlayer = context.player();
        ServerLevel world = serverPlayer.level();
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if(blockEntity instanceof SignPoleBlockEntity)
            world.getServer().execute(() -> ((SignPoleBlockEntity) blockEntity).setRotationValue(rotation));
    }
}
