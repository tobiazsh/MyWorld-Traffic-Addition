package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.fix.CustomizableSignReinitializer;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignSelector;
import at.tobiazsh.myworld.traffic_addition.imgui.overlay.OverlayStorage;
import at.tobiazsh.myworld.traffic_addition.network.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.network.GlobalReceiverClient;
import at.tobiazsh.myworld.traffic_addition.payload.client_actions.ClearCSBETextureRenderState;
import at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences;
import at.tobiazsh.myworld.traffic_addition.preference.LegacyClientPreferenceConverter;
import at.tobiazsh.myworld.traffic_addition.preference.PreferenceLoader;
import at.tobiazsh.myworld.traffic_addition.rendering.RegistrableBlockEntityRender;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.*;
import at.tobiazsh.myworld.traffic_addition.screens.EmptyScreen;
import at.tobiazsh.myworld.traffic_addition.payload.ShowImGuiWindow;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.OpenCustomizableSignEditScreen;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.OpenSignPoleRotationScreenPayload;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.screens.CustomizableSignSettingScreen;
import at.tobiazsh.myworld.traffic_addition.screens.SignPoleRotationScreen;
import at.tobiazsh.myworld.traffic_addition.error.Error;
import at.tobiazsh.myworld.traffic_addition.cache.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.network.OnlineImageNetworking;
import at.tobiazsh.myworld.traffic_addition.texture.HybridTextureSweeper;
import at.tobiazsh.myworld.traffic_addition.texture.sign.BackgroundLoader;
import dev.tobiazsh.imguib3d.client.overlay.ImGuiOverlayManager;
import imgui.ImGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.*;

public class MyWorldTrafficAdditionClient implements ClientModInitializer {

	public static CustomizableSignSettingScreen customizableSignSettingScreen;
    public static final SignSelector SIGN_SELECTOR = new SignSelector("NormalSignSelector");

	private static final List<GlobalReceiverClient<? extends CustomPacketPayload>> globalReceiverClients = new ArrayList<>();
	private static final List<RegistrableBlockEntityRender<? extends @NotNull BlockEntity, ? extends @NotNull BlockEntityRenderState>> blockEntityRenderers = new ArrayList<>();

	public static final Path clientPreferencesLocation =
			FabricLoader.getInstance().getConfigDir()
					.resolve(MyWorldTrafficAddition.MOD_ID)
					.resolve("client_preferences.toml");

	public static final Path legacyClientPreferencesLocation =
			FabricLoader.getInstance().getConfigDir()
					.resolve(MyWorldTrafficAddition.MOD_ID)
					.resolve("gameplay_config.json");

	/**
	 * Can be null if preferences haven't yet been loaded.
	 */
	private static final ClientPreferences clientPreferences = loadOrConvertOrCreate();

	public static final ImGui imgui = new ImGui(); // I have to use this since a static reference crashes the program when I call calcTextSize / calcItemSize

    static {
        SignSelector.signSelectors.add(SIGN_SELECTOR);
    }

	@Override
	public void onInitializeClient() {
		addGlobalReceivers();
		GlobalReceiverClient.bulkRegisterGlobalReceiversClient(globalReceiverClients);

		ImGuiOverlayManager.getInstance().add(OverlayStorage.EDITOR_OVERLAY);

		addBlockEntityRenderers();
		RegistrableBlockEntityRender.bulkRegisterBlockEntityRenderers(blockEntityRenderers);

		ClientCommandRegistrationCallback.EVENT.register(ModCommandsClient::initialize);

		registerCustomProtocols();

		OnlineImageCache.createCacheDir();

		ClientLifecycleEvents.CLIENT_STARTED.register((_) -> {
			MyWorldTrafficAddition.LOGGER.info("Loading Background Atlases from the Autoload folder...");
			autoloadBackgroundAtlases();
			MyWorldTrafficAddition.LOGGER.info("Loaded {} background atlases", BackgroundLoader.BACKGROUND_SPRITES.size());

			MyWorldTrafficAddition.LOGGER.info("Registering essential fonts...");
			DefaultFonts.registerAll();
		});

		// Sweep all HybridTextures every tick to free up memory
		ClientTickEvents.END_CLIENT_TICK.register((minecraft) -> {
			Level level = minecraft.level;
			if (level == null) return;
			HybridTextureSweeper.getInstance().sweep(level.getGameTime());
		});

		CustomizableSignReinitializer.register();
	}

	private static void addBlockEntityRenderers() {
		blockEntityRenderers.addAll(Arrays.asList(
                new RegistrableBlockEntityRender<>(SIGN_POLE_BLOCK_ENTITY, SignPoleBlockEntityRenderer::new),
                new RegistrableBlockEntityRender<>(TRIANGULAR_SIGN_BLOCK_ENTITY, TriangularSignBlockEntityRenderer::new),
                new RegistrableBlockEntityRender<>(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY, UpsideDownTriangularSignBlockEntityRenderer::new),
                new RegistrableBlockEntityRender<>(OCTAGONAL_SIGN_BLOCK_ENTITY, OctagonalSignBlockEntityRenderer::new),
                new RegistrableBlockEntityRender<>(ROUND_SIGN_BLOCK_ENTITY, RoundSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, CustomizableSignBlockEntityRenderer::new)
		));
	}

	private static void addGlobalReceivers() {
		globalReceiverClients.addAll(Arrays.asList(
				new GlobalReceiverClient<>(ClearCSBETextureRenderState.ID, (payload) -> CustomizableSignBlockEntityRenderer.invalidateTexture(payload.pos())),

				new GlobalReceiverClient<>(OpenSignPoleRotationScreenPayload.Id, (payload) -> {
					BlockPos pos = payload.pos();

					if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open SignPoleRotationScreen because world or player is null!");
						return;
					}

                    Minecraft.getInstance().gui.setScreen(new SignPoleRotationScreen(Minecraft.getInstance().level, pos, Minecraft.getInstance().player));
				}),

				new GlobalReceiverClient<>(OpenSignSelectionPayload.Id, (payload) -> {
					if (Minecraft.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open SignSelectionScreen because world or player is null!");
						return;
					}

					Minecraft.getInstance().gui.setScreen(new EmptyScreen(Component.literal("Sign Selection"), SIGN_SELECTOR::close));
                    SIGN_SELECTOR.open(SignBlock.getSignSelectionEnum(payload.selection_type()), payload.pos(), payload.dimensionRegistryKey());
				}),

				new GlobalReceiverClient<>(OpenCustomizableSignEditScreen.Id, (payload) -> {
					BlockPos pos = payload.pos();

					if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open CustomizableSignSettingScreen because world or player is null!");
						return;
					}

					customizableSignSettingScreen = new CustomizableSignSettingScreen(Minecraft.getInstance().level, pos, Minecraft.getInstance().player);
					Minecraft.getInstance().gui.setScreen(customizableSignSettingScreen);
				}),

				new GlobalReceiverClient<>(ShowImGuiWindow.Id, (payload -> {
					switch (ModVars.ImGuiWindowIds.values()[payload.windowId()]) {
						case ABOUT -> {
							OverlayStorage.CONFIGURATION_OVERLAY.open();
							OverlayStorage.CONFIGURATION_OVERLAY.aboutWindow.render();
						}
						case PREF -> {
							OverlayStorage.CONFIGURATION_OVERLAY.open();
							OverlayStorage.CONFIGURATION_OVERLAY.preferencesWindow.render();
						}
					}
				})),

				new GlobalReceiverClient<>(ChunkedDataPayload.Id, (payload) -> CustomClientNetworking.getInstance().processChunkedPayload(
                        payload,
                        (protocolId, data, handler) -> Minecraft.getInstance().execute(() -> handler.accept(data))
                ))
		));
	}

	private static void registerCustomProtocols() {
		// Get maximum image upload size
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_maximum_image_upload_size"), bytes -> {
			String maximumSize_str = new String(bytes);
            OnlineImageDialog.setMaximumUploadSize(Long.parseLong(maximumSize_str));
		});

        CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_server_error"), bytes -> {
            Error error = Error.fromBytes(bytes);

			SystemToast toast = new SystemToast(
					SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
					Component.literal(error.getTitle()),
					Component.literal(error.getMessage())
			);

			Minecraft.getInstance().gui.toastManager().addToast(toast);
        });

		// Get total number of uploaded images
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_total_uploaded_images"), OnlineImageNetworking::setImageCount);

		// Get number of private images uploaded by the player
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_private_uploaded_images"), OnlineImageNetworking::setPrivateImageCount);

		// Get metadata of uploaded images
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_image_entries_metadata"), OnlineImageNetworking::setMetadataList);

		// Get thumbnail of uploaded images
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_thumbnail_data"), OnlineImageNetworking::setThumbnailData);

		// Get image data
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "get_image_data"), OnlineImageNetworking::setImageData);
	}

	/**
	 * Loads all background atlases from the autoload folder inside the resources
	 */
	public static void autoloadBackgroundAtlases() {
		try {
			BackgroundLoader.autoload();
		} catch (IOException | NotImplementedException | NullPointerException | IllegalArgumentException e) {
			MyWorldTrafficAddition.LOGGER.error("Failed autoloading one or multiple background atlases. Check resource structure!", e);
		}
	}

	public static void onStopGame() {
		MyWorldTrafficAddition.LOGGER.info("Shutting down MyWorld Traffic Addition!");

		MyWorldTrafficAddition.LOGGER.info("Clearing image cache...");
		OnlineImageCache.clearCache();

		MyWorldTrafficAddition.LOGGER.info("Saving preferences...");
		savePreferences();

		MyWorldTrafficAddition.LOGGER.info("Thank you for playing MyWorld Traffic Addition! <3");
	}

	// ---------------- PREFERENCES ----------------

	private static void savePreferences() {
		PreferenceLoader.savePreferenceToFileOrCallback(
				clientPreferencesLocation.toFile(),
				clientPreferences,
				MyWorldTrafficAddition.LOGGER::error
		);
	}

	public static ClientPreferences getClientPreferences() {
		return clientPreferences;
	}

	/**
	 * Either load normally, or convert from old format if the file is in the old format.
	 * If neither file could be found, it will be created with the newer format.
	 */
	private static ClientPreferences loadOrConvertOrCreate() {
		if (Files.exists(clientPreferencesLocation)) {
			return PreferenceLoader.loadPreferenceFromFileOrDefault(
					clientPreferencesLocation.toFile(),
					new ClientPreferences(),
					ClientPreferences::new,
                    MyWorldTrafficAddition.LOGGER::warn
			);
		} else if (Files.exists(legacyClientPreferencesLocation)) {
			MyWorldTrafficAddition.LOGGER.info("Found legacy client preferences file. Converting to new format...");

			ClientPreferences converted =
					LegacyClientPreferenceConverter.produceNewClientPreferences(legacyClientPreferencesLocation.toFile());

			MyWorldTrafficAddition.LOGGER.info("""
				Converted legacy client preferences file to new format.
				Old file is kept as a backup.
			""");

			return converted;
		} else {
			MyWorldTrafficAddition.LOGGER.info("No client preferences file found. Using empty preferences.");
			return new ClientPreferences();
		}
	}

}