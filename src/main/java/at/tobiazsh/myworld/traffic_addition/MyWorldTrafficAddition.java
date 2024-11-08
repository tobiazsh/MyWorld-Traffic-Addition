package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.Utils.SmartPayload;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SpecialBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.*;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.Actions.CustomizableSignBlockActions;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.Actions.SignBlockActions;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.Actions.SignPoleBlockActions;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowAboutWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowDemoWindow;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.Utils.SmartPayload.bulkRegisterPayloads;

/*
	@author Tobias
	@mod MyWorld Traffic Addition

	You may modify this code and share with others but you may not make profit off of it and you must credit me if you share this code without any changes except for development purposes.
 */

public class MyWorldTrafficAddition implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("myworld_traffic_addition");

	public static final String MOD_ID = "myworld_traffic_addition";
	public static final String MOD_ID_HUMAN = "MyWorld Traffic Addition";

	private static List<SmartPayload<? extends CustomPayload>> smartPayloadsServer = new ArrayList<>();
	private static List<SmartPayload<? extends CustomPayload>> smartPayloadsClient = new ArrayList<>();
	private static List<SmartPayload<? extends CustomPayload>> smartPayloads = new ArrayList<>();

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModGroups.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModCommands.initialize();

		SpecialBlockEntity.initialize();

		addSmartPayloadsServer();
		addSmartPayloadsClient();
		combineSmartPayloads();

		// Register all payloads, no matter client or server
		bulkRegisterPayloads(smartPayloads);

		SmartPayload.bulkRegisterGlobalReceivers(smartPayloadsServer);
	}

	private static void addSmartPayloadsServer() {
		smartPayloadsServer.addAll(Arrays.asList(

				// Sign Poles
				new SmartPayload<>(SignPoleRotationPayload.Id, SignPoleBlockActions::handleRotation, SignPoleRotationPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetShouldRenderSignPolePayload.Id, SignPoleBlockActions::handleSetShouldRender, SetShouldRenderSignPolePayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),

				// Sign Blocks
				new SmartPayload<>(SignBlockTextureChangePayload.Id, SignBlockActions::handleTextureChange, SignBlockTextureChangePayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SignBlockRotationPayload.Id, SignBlockActions::handleRotationChange, SignBlockRotationPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SignBlockBackstepCoordsChange.Id, SignBlockActions::handleBackstepCoordsChange, SignBlockBackstepCoordsChange.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),

				// Customizable Sign Blocks
				new SmartPayload<>(SetMasterCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetMaster, SetMasterCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetBorderTypeCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetBorderType, SetBorderTypeCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetSignPolePositionsCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetSignPolePositions, SetSignPolePositionsCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetSignPositionsCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetSignPositions, SetSignPositionsCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetRenderStateCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetRenderState, SetRenderStateCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetRotationCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetRotation, SetRotationCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetSizeCustomizableSignPayload.Id, CustomizableSignBlockActions::handleSetSize, SetSizeCustomizableSignPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetCustomizableSignTexture.Id, CustomizableSignBlockActions::handleSetCustomTexture, SetCustomizableSignTexture.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(UpdateTextureVarsCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleUpdateTextureVariables, UpdateTextureVarsCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER)
		));
	}

	private static void addSmartPayloadsClient() {
		smartPayloadsClient.addAll(Arrays.asList(
				new SmartPayload<>(OpenSignPoleRotationScreenPayload.Id, null, OpenSignPoleRotationScreenPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(OpenSignSelectionPayload.Id, null, OpenSignSelectionPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(OpenCustomizableSignEditScreen.Id, null, OpenCustomizableSignEditScreen.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(ShowDemoWindow.Id, null, ShowDemoWindow.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(ShowAboutWindow.Id, null, ShowAboutWindow.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT)
		));
	}

	private static void combineSmartPayloads() {
		smartPayloads.addAll(smartPayloadsServer);
		smartPayloads.addAll(smartPayloadsClient);
	}

	public static void sendOpenSignPoleRotationScreenPacket(ServerPlayerEntity player, BlockPos pos) {
		ServerPlayNetworking.send(player, new OpenSignPoleRotationScreenPayload(pos));
	}

	public static void sendOpenSignSelectionScreenPacket (ServerPlayerEntity player, BlockPos pos, int type) {
		ServerPlayNetworking.send(player, new OpenSignSelectionPayload(pos, type));
	}

	public static void sendOpenCustomizableSignEditScreenPacket(ServerPlayerEntity player, BlockPos pos) {
		ServerPlayNetworking.send(player, new OpenCustomizableSignEditScreen(pos));
	}
}