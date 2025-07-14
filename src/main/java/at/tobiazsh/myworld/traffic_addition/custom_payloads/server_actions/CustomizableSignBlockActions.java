package at.tobiazsh.myworld.traffic_addition.custom_payloads.server_actions;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CustomizableSignBlockActions {
    public static void handleUpdateTextureVariables(UpdateTextureVarsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(customizableSignBlockEntity::updateTextureVars);
    }

    public static void handleSetSize(SetSizeCustomizableSignPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        int height = payload.height();
        int width = payload.width();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
            defaults.world.getServer().execute(() -> {
                if (height != -1) customizableSignBlockEntity.setHeight(height);
                if (width != -1) customizableSignBlockEntity.setWidth(width);

                customizableSignBlockEntity.setInitialized(true);
            });
        }
    }

    public static void handleSetRotation(SetRotationCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setRotation(rotation));
    }

    public static void handleSetRenderState(SetRenderStateCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        boolean renderState = payload.renderState();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setRendered(renderState));
    }

    public static void handleSetSignPositions(SetSignPositionsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        String blockPosString = payload.blockPosString();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setSignPositions(blockPosString));
    }

    public static void handleSetSignPolePositions(SetSignPolePositionsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        String blockPosString = payload.blockPosString();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setSignPolePositions(blockPosString));
    }

    public static void handleSetBorderType(SetBorderTypeCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        String modelPath = payload.modelPath();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity csbeBlockEntity)
            defaults.world.getServer().execute(() -> csbeBlockEntity.setBorderType(modelPath));
    }

    public static void handleSetMaster(SetMasterCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.ActionDefaultsBuilder(ctx);
        BlockPos pos = payload.pos();
        Boolean shouldMaster = payload.shouldMaster();
        BlockPos masterPos = payload.master();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity) {
            defaults.world.getServer().execute(() -> {
                CustomizableSignBlockEntity csbeBlockEntity = (CustomizableSignBlockEntity) blockEntity;

                csbeBlockEntity.setMaster(shouldMaster);
                csbeBlockEntity.setMasterPos(masterPos);
            });
        }
    }
}
