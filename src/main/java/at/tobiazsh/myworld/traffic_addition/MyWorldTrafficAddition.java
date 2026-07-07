package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.data_fix.GlobalDataFixer;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.network.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.network.CustomServerNetworking;
import at.tobiazsh.myworld.traffic_addition.backend.OnlineImageBackend;
import at.tobiazsh.myworld.traffic_addition.payload.client_actions.ClearCSBETextureRenderState;
import at.tobiazsh.myworld.traffic_addition.preference.ServerBlacklist;
import at.tobiazsh.myworld.traffic_addition.preference.ServerPreferencesManager;
import at.tobiazsh.myworld.traffic_addition.network.SmartPayload;
import at.tobiazsh.myworld.traffic_addition.payload.server_actions.CustomizableSignBlockActions;
import at.tobiazsh.myworld.traffic_addition.payload.server_actions.SignBlockActions;
import at.tobiazsh.myworld.traffic_addition.payload.server_actions.SignPoleBlockActions;
import at.tobiazsh.myworld.traffic_addition.payload.ShowImGuiWindow;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.network.SmartPayload.bulkRegisterPayloads;

/*
	@author Tobias
	@mod MyWorld Traffic Addition
 */

public class MyWorldTrafficAddition implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("MyWorld Traffic Addition");

	public static final int DATA_FIXER_VERSION = 1;
	public static final String MOD_ID = "myworld_traffic_addition";
    public static final Path MOD_RESOURCES = Path.of("/assets/myworld_traffic_addition");
	public static final String MOD_ID_HUMAN = "MyWorld Traffic Addition";
	public static final String MOD_VERSION =
            "v" +
            FabricLoader.getInstance()
                    .getModContainer(MOD_ID)
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();

	private static boolean serverIsDedicated;

	private static final List<SmartPayload<? extends CustomPacketPayload>> serverSmartPayloads = new ArrayList<>();
	private static final List<SmartPayload<? extends CustomPacketPayload>> clientSmartPayloads = new ArrayList<>();
	private static final List<SmartPayload<? extends CustomPacketPayload>> smartPayloads = new ArrayList<>();

	@Override
	public void onInitialize() {
		MyWorldTrafficAddition.LOGGER.info("Initializing {} {}", MOD_ID_HUMAN, MOD_VERSION);
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

        MyWorldTrafficAddition.LOGGER.info("Registering events...");
        registerEvents();

		SmartPayload.bulkRegisterGlobalReceivers(serverSmartPayloads);

		MyWorldTrafficAddition.LOGGER.info("Loading preferences...");
		ServerPreferencesManager.loadPreferences();
        ServerBlacklist.loadBlacklist();

		GlobalDataFixer.register();

		MyWorldTrafficAddition.LOGGER.info("Counting uploaded images and reading metadata into memory...");
		OnlineImageBackend.countEntriesAndReadIntoMemory();
		MyWorldTrafficAddition.LOGGER.info("Found {} uploaded images", OnlineImageBackend.totalEntries);

		MyWorldTrafficAddition.LOGGER.info("{} {} initialized successfully!", MOD_ID_HUMAN, MOD_VERSION);
	}

	private static void addSmartPayloadsServer() {
		serverSmartPayloads.addAll(Arrays.asList(

				// Sign Poles
				new SmartPayload<>(SignPoleRotationPayload.Id, SignPoleBlockActions::handleRotation, SignPoleRotationPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SetShouldRenderSignPolePayload.Id, SignPoleBlockActions::handleSetShouldRender, SetShouldRenderSignPolePayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),

				// Sign Blocks
				new SmartPayload<>(SignBlockTextureChangePayload.Id, SignBlockActions::handleTextureChange, SignBlockTextureChangePayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(SignBlockRotationPayload.Id, SignBlockActions::handleRotationChange, SignBlockRotationPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),

				// Customizable Sign Blocks
				new SmartPayload<>(SetRotationCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleSetRotation, SetRotationCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
				new SmartPayload<>(UpdateTextureVarsCustomizableSignBlockPayload.Id, CustomizableSignBlockActions::handleUpdateTextureVariables, UpdateTextureVarsCustomizableSignBlockPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),
                new SmartPayload<>(CustomizableSignSettingScreenClosed.Id, CustomizableSignBlockActions::handleCustomizableSignEditScreenClosed, CustomizableSignSettingScreenClosed.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.SERVER),

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
				new SmartPayload<>(ChunkedDataPayload.Id, null, ChunkedDataPayload.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT),
				new SmartPayload<>(ClearCSBETextureRenderState.ID, null, ClearCSBETextureRenderState.CODEC, SmartPayload.RECEIVE_ENVIRONMENT.CLIENT)
		));
	}

	private static void combineSmartPayloads() {
		smartPayloads.addAll(serverSmartPayloads);
		smartPayloads.addAll(clientSmartPayloads);
	}

	public static void sendOpenSignPoleRotationScreenPacket(ServerPlayer player, BlockPos pos) {
		ServerPlayNetworking.send(player, new OpenSignPoleRotationScreenPayload(pos));
	}

	public static void sendOpenCustomizableSignEditScreenPacket(ServerPlayer player, BlockPos pos) {
		ServerPlayNetworking.send(player, new OpenCustomizableSignEditScreen(pos));
	}

	/**
	 * Broadcasts a packet to all players tracking the given block position, telling their client to
	 * invalidate (clear) the cached render state for that CSBE position. This is needed when the
	 * block is destroyed so the static renderer cache does not serve a stale texture when the block
	 * is placed again at the same position.
	 */
	public static void sendClearCSBETextureRenderStatePacket(ServerLevel level, BlockPos pos) {
		ClearCSBETextureRenderState packet = new ClearCSBETextureRenderState(pos);
		for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
			ServerPlayNetworking.send(player, packet);
		}
	}

	private static void registerCustomProtocols() {
		// Set customizable sign texture
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_customizable_sign_texture"), (player, data) -> CustomizableSignBlockEntity.setTransmittedTexture(new String(data), player));

		// Customizable Sign Initialization
		CustomServerNetworking.getInstance().registerProtocolHandler(
				Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "customizable_sign_initialization_transmission"),
				CustomizableSignBlockActions::handleInitializeSign
		);

		// Request the maximum image upload size
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_maximum_image_upload_size"), (player, data) -> {
            CustomServerNetworking.getInstance().sendStringToClient(player, Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_maximum_image_upload_size"), String.valueOf(ServerPreferencesManager.maximumImageUploadSize));
		});

		// Send custom image to server (client -> server as always)
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "send_custom_image_to_server"), (player, data) -> {
			byte[] imageData = Arrays.copyOfRange(data, 0, data.length);
			OnlineImageBackend.processUploadedImage(player, imageData);
		});

		// Request the total number of uploaded images
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_total_uploaded_images"), (player, data) -> {
            boolean isPlayerMod = player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
			CustomServerNetworking.getInstance().sendStringToClient(player, Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_total_uploaded_images"), isPlayerMod ? String.valueOf(OnlineImageBackend.totalEntries) : String.valueOf(OnlineImageBackend.publicEntries));
		});

		// Request the total number of uploaded images by user
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_private_uploaded_images"), (player, data) -> OnlineImageBackend.getEntryNumberByPlayer(player));

		// Request image entries metadata from server; Used in the online image gallery
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_image_entries_metadata"), OnlineImageBackend::sendEntryMetadataToClient);

		// Request thumbnail data (for custom images)
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_thumbnail_data"), OnlineImageBackend::sendThumbnailsOf);

		// Request for image deletion
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_image_deletion"), OnlineImageBackend::deleteImage);

		// Request image
		CustomServerNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_image_data"), OnlineImageBackend::sendImageDataOf);
	}

    private static void registerEvents() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			serverIsDedicated = server.isDedicatedServer();
		});

        ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> {
            ServerBlacklist.saveBlacklist();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ServerBlacklist.saveBlacklist();
            OnlineImageBackend.shutdown();
        });
    }

	private static boolean isServerDedicated() {
		return serverIsDedicated;
	}

	@Contract("_ -> new")
    public static @NotNull Identifier createId(String id) {
		return Identifier.fromNamespaceAndPath(MOD_ID, id);
	}

}