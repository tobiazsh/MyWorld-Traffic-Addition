package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Networking.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.Networking.CustomServerNetworking;
import at.tobiazsh.myworld.traffic_addition.Utils.SmartPayload;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.SpecialBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.ServerActions.CustomizableSignBlockActions;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.ServerActions.SignBlockActions;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.ServerActions.SignPoleBlockActions;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification.*;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.ShowImGuiWindow;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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
 */

public class MyWorldTrafficAddition implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("myworld_traffic_addition");

	public static final String MOD_ID = "myworld_traffic_addition";
	public static final String MOD_ID_HUMAN = "MyWorld Traffic Addition";
	public static final String MODVER = "v1.2.1";

	private static final List<SmartPayload<? extends CustomPayload>> serverSmartPayloads = new ArrayList<>();
	private static final List<SmartPayload<? extends CustomPayload>> clientSmartPayloads = new ArrayList<>();
	private static final List<SmartPayload<? extends CustomPayload>> smartPayloads = new ArrayList<>();

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
		registerCustomProtocols();

		SmartPayload.bulkRegisterGlobalReceivers(serverSmartPayloads);
	}

	private static void addSmartPayloadsServer() {
		serverSmartPayloads.addAll(Arrays.asList(

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
				new SmartPayload<>(UpdateTextureVarsCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleUpdateTextureVariables, UpdateTextureVarsCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),

				// OTHER
                new SmartPayload<>(ChunkedDataPayload.Id, (payload, context) -> {
                    CustomServerNetworking.getInstance().processChunkedPayload(
                            payload,
                            (protocolId, data, handler) -> context.server().execute(() -> handler.accept(context.player(), data))
                    );
                }, ChunkedDataPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER)
		));
	}

	private static void addSmartPayloadsClient() {
		clientSmartPayloads.addAll(Arrays.asList(
				new SmartPayload<>(OpenSignPoleRotationScreenPayload.Id, null, OpenSignPoleRotationScreenPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(OpenSignSelectionPayload.Id, null, OpenSignSelectionPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(OpenCustomizableSignEditScreen.Id, null, OpenCustomizableSignEditScreen.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(ShowImGuiWindow.Id, null, ShowImGuiWindow.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(ChunkedDataPayload.Id, null, ChunkedDataPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT)
		));
	}

	private static void combineSmartPayloads() {
		smartPayloads.addAll(serverSmartPayloads);
		smartPayloads.addAll(clientSmartPayloads);
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

	private static void registerCustomProtocols() {
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_customizable_sign_texture"), (player, data) -> CustomizableSignBlockEntity.setTransmittedTexture(new String(data), player));
	}
}