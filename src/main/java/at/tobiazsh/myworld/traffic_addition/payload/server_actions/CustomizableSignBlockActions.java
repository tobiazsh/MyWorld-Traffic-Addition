package at.tobiazsh.myworld.traffic_addition.payload.server_actions;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

public class CustomizableSignBlockActions {

    public static void handleInitializeSign(ServerPlayer from, byte[] data) {
        var initializationResult = CustomizableSignInitializer.CustomizableSignInitializationResult.decode(
                new FriendlyByteBuf(Unpooled.wrappedBuffer(data))
        );

        // Do various checks like sign width/height here in the next versions

        for (int i = 0; i < initializationResult.signAbsolute().size(); i++) {
            BlockPos signPos = initializationResult.signAbsolute().get(i);
            if (!(from.level().getBlockEntity(signPos) instanceof CustomizableSignBlockEntity sign)) {
                MyWorldTrafficAddition.LOGGER.error(
                        "Player {} with UUID {} has just tried to initialize a sign at position {}, which failed," +
                                "because specified sign is not a customizable sign block entity!",
                        from.getName(), from.getUUID(), signPos
                );

                from.sendSystemMessage(Component.literal(
                        "There has been an error initializing the sign at position " + signPos + "!"
                ));

                return;
            }

            sign.initialize(initializationResult, initializationResult.borders().get(i));
        }

        MyWorldTrafficAddition.LOGGER.info(
                "Player {} with UUID {} has just initialized a sign at position {}!",
                from.getName(), from.getUUID(), initializationResult.realMaster()
        );
    }

    public static void handleUpdateTextureVariables(UpdateTextureVarsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        BlockEntity blockEntity = defaults.world.getBlockEntity(pos);

        if (blockEntity instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(customizableSignBlockEntity::updateTextureVars);
    }

    public static void handleSetRotation(SetRotationCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
        GeneralActions.ActionDefaults defaults = GeneralActions.ActionDefaults.fromContext(ctx);
        BlockPos pos = payload.pos();
        int rotation = payload.rotation();

        if (defaults.world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity)
            defaults.world.getServer().execute(() -> customizableSignBlockEntity.setRotation(rotation));
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
