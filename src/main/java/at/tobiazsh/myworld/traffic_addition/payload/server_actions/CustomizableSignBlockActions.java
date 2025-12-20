package at.tobiazsh.myworld.traffic_addition.payload.server_actions;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

public class CustomizableSignBlockActions {
    public static void handleUpdateTextureVariables(UpdateTextureVarsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(customizableSignBlockEntity::updateTextureVars);
    }

    public static void handleSetSize(SetSizeCustomizableSignPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
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
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setRotation(rotation));
    }

    public static void handleSetRenderState(SetRenderStateCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        boolean renderState = payload.renderState();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setRendered(renderState));
    }

    public static void handleSetSignPositions(SetSignPositionsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        byte[] bytes = payload.signDistances();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setSignDistances(bytes));
    }

    public static void handleSetSignPolePositions(SetSignPolePositionsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        byte[] bytes = payload.bytes();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setSignPoleDistances(bytes));
    }

    public static void handleSetBorderType(SetBorderTypeCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        String borders = payload.borders();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity csbeBlockEntity)
            defaults.world.getServer().execute(() -> csbeBlockEntity.setBorderType(BorderProperty.INSTANCE.fromString(borders)));
    }

    public static void handleSetMaster(SetMasterCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        Boolean shouldMaster = payload.shouldMaster();
        BlockPos masterPos = payload.master();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity csbeBlockEntity) {
            defaults.world.getServer().execute(() -> {
                csbeBlockEntity.setMaster(shouldMaster);
                csbeBlockEntity.setMasterPos(masterPos);
            });
        }
    }

    public static void handleCustomizableSignEditScreenClosed(CustomizableSignSettingScreenClosed payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos masterSignPos = payload.masterSignPos();

        if (defaults.world == null) {
            MyWorldTrafficAddition.LOGGER.warn("World is null when trying to mark customizable sign as being edited!");
            return;
        }

        if (defaults.world.getBlockEntity(masterSignPos) instanceof CustomizableSignBlockEntity csbe)
            csbe.resetEditedBy();
    }
}
