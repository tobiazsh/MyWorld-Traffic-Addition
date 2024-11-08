package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.Actions;

import at.tobiazsh.myworld.traffic_addition.Utils.Coordinates;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SignBlockBackstepCoordsChange;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SignBlockRotationPayload;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SignBlockTextureChangePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SignBlockActions {
    public static void handleTextureChange(SignBlockTextureChangePayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        String textureId = payload.textureId();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof SignBlockEntity signBlockEntity)
            defaults.world.getServer().execute(() -> signBlockEntity.setTextureId(textureId));
    }

    public static void handleRotationChange(SignBlockRotationPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if(blockEntity instanceof SignBlockEntity signBlockEntity)
            defaults.world.getServer().execute(() -> signBlockEntity.setRotation(rotation));
    }

    public static void handleBackstepCoordsChange(SignBlockBackstepCoordsChange payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        float x = payload.x();
        float y = payload.y();
        float z = payload.z();
        Direction direction = payload.direction();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);
        Coordinates coordinates = new Coordinates(x, y, z, direction);

        if(blockEntity instanceof SignBlockEntity signBlockEntity)
            defaults.world.getServer().execute(() -> signBlockEntity.setBackstepCoords(coordinates));
    }
}
