package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.ServerActions;

import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.BlockModification.SetShouldRenderSignPolePayload;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.BlockModification.SignPoleRotationPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class SignPoleBlockActions {
    public static void handleSetShouldRender(SetShouldRenderSignPolePayload payload, ServerPlayNetworking.Context ctx) {
        ServerPlayerEntity serverPlayer = ctx.player();
        ServerWorld world = serverPlayer.getServerWorld();
        BlockPos pos = payload.pos();
        boolean value = payload.value();

        if (world.getBlockEntity(pos) instanceof SignPoleBlockEntity blockEntity)
            world.getServer().execute(() -> blockEntity.setShouldRender(value));
    }

    public static void handleRotation(SignPoleRotationPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity serverPlayer = context.player();
        ServerWorld world = serverPlayer.getServerWorld();
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if(blockEntity instanceof SignPoleBlockEntity)
            world.getServer().execute(() -> ((SignPoleBlockEntity) blockEntity).setRotationValue(rotation));
    }
}
