package at.tobiazsh.myworld.traffic_addition.custom_payloads.server_actions;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SignBlockRotationPayload;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SignBlockTextureChangePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

public class SignBlockActions {
    public static void handleTextureChange(SignBlockTextureChangePayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        String textureId = payload.texturePath();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof SignBlockEntity signBlockEntity)
            defaults.world.getServer().execute(() -> signBlockEntity.setTexturePath(textureId));
    }

    public static void handleRotationChange(SignBlockRotationPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if(blockEntity instanceof SignBlockEntity signBlockEntity)
            defaults.world.getServer().execute(() -> signBlockEntity.setRotation(rotation));
    }
}
