package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.networking.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.networking.CustomServerNetworking;
import at.tobiazsh.myworld.traffic_addition.utils.custom_image.OnlineImageServerLogic;
import at.tobiazsh.myworld.traffic_addition.utils.preferences.ServerPreferences;
import at.tobiazsh.myworld.traffic_addition.utils.SmartPayload;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.server_actions.CustomizableSignBlockActions;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.server_actions.SignBlockActions;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.server_actions.SignPoleBlockActions;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.ShowImGuiWindow;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

import static at.tobiazsh.myworld.traffic_addition.utils.SmartPayload.bulkRegisterPayloads;

/*
	@author Tobias
	@mod MyWorld Traffic Addition
 */

public class MyWorldTrafficAddition implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("MyWorld Traffic Addition");

	public static final String MOD_ID = "myworld_traffic_addition";
	public static final String MOD_ID_HUMAN = "MyWorld Traffic Addition";
	public static final String MODVER = "v1.3.0";

	private static final List<SmartPayload<? extends CustomPayload>> serverSmartPayloads = new ArrayList<>();
	private static final List<SmartPayload<? extends CustomPayload>> clientSmartPayloads = new ArrayList<>();
	private static final List<SmartPayload<? extends CustomPayload>> smartPayloads = new ArrayList<>();

	@Override
	public void onInitialize() {
		MyWorldTrafficAddition.LOGGER.info("Initializing {} {}", MOD_ID_HUMAN, MODVER);
		ModItems.initialize();
		ModGroups.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();

		CommandRegistrationCallback.EVENT.register(ModCommands::initialize);

		MyWorldTrafficAddition.LOGGER.info("Adding payloads...");
		addSmartPayloadsServer();
		addSmartPayloadsClient();
		combineSmartPayloads();

		MyWorldTrafficAddition.LOGGER.info("Registering payloads...");
		// Register all payloads, no matter client or server
		bulkRegisterPayloads(smartPayloads);
		registerCustomProtocols();

		SmartPayload.bulkRegisterGlobalReceivers(serverSmartPayloads);

		MyWorldTrafficAddition.LOGGER.info("Loading preferences...");
		ServerPreferences.loadPreferences();

		MyWorldTrafficAddition.LOGGER.info("Counting uploaded images and reading metadata into memory...");
		OnlineImageServerLogic.countEntriesAndReadIntoMemory();
		MyWorldTrafficAddition.LOGGER.info("Found {} uploaded images", OnlineImageServerLogic.entries);

		MyWorldTrafficAddition.LOGGER.info("{} {} initialized successfully!", MOD_ID_HUMAN, MODVER);
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
		// Set customizable sign texture
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_customizable_sign_texture"), (player, data) -> CustomizableSignBlockEntity.setTransmittedTexture(new String(data), player));

		// Request the maximum image upload size
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_maximum_image_upload_size"), (player, data) -> {
            CustomServerNetworking.getInstance().sendStringToClient(player, Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_maximum_image_upload_size"), String.valueOf(ServerPreferences.maximumImageUploadSize));
		});

		// Send custom image to server (client -> server as always)
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "send_custom_image_to_server"), (player, data) -> {
			byte[] imageData = Arrays.copyOfRange(data, 0, data.length);
			OnlineImageServerLogic.processUploadedImage(imageData);
		});

		// Request the total number of uploaded images
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_total_uploaded_images"), (player, data) -> {
			CustomServerNetworking.getInstance().sendStringToClient(player, Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_total_uploaded_images"), String.valueOf(OnlineImageServerLogic.publicEntries));
		});

		// Request the total number of uploaded images by user
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_private_uploaded_images"), (player, data) -> OnlineImageServerLogic.getEntryNumberByPlayer(player));

		// Request image entries metadata from server; Used in the online image gallery
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_image_entries_metadata"), OnlineImageServerLogic::sendEntryMetadataToClient);

		// Request thumbnail data (for custom images)
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_thumbnail_data"), OnlineImageServerLogic::sendThumbnailsOf);

		// Request for image deletion
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_image_deletion"), OnlineImageServerLogic::deleteImage);

		// Request image
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_image_data"), OnlineImageServerLogic::sendImageDataOf);
	}



	public static Identifier createId(String id) {
		return Identifier.of(MOD_ID, id);
	}
}