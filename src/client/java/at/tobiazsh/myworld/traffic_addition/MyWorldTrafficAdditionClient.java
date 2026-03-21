package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import at.tobiazsh.myworld.traffic_addition.block_entities.*;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TexturableElementInterface;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.PreferencesWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignSelector;
import at.tobiazsh.myworld.traffic_addition.network.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.network.GlobalReceiverClient;
import at.tobiazsh.myworld.traffic_addition.payload.client_actions.ClearCSBETextureRenderState;
import at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences;
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
import at.tobiazsh.myworld.traffic_addition.texture.DynamicTexture;
import at.tobiazsh.myworld.traffic_addition.texture.sign.BackgroundLoader;
import imgui.ImGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.*;

public class MyWorldTrafficAdditionClient implements ClientModInitializer {

	public static CustomizableSignSettingScreen customizableSignSettingScreen;
    public static final SignSelector signSelector = new SignSelector("NormalSignSelector");

	private static final List<GlobalReceiverClient<? extends CustomPacketPayload>> globalReceiverClients = new ArrayList<>();
	private static final List<RegistrableBlockEntityRender<? extends @NotNull BlockEntity, ? extends @NotNull BlockEntityRenderState>> blockEntityRenderers = new ArrayList<>();

	public static final ImGui imgui = new ImGui(); // I have to use this since a static reference crashes the program when I call calcTextSize / calcItemSize

    static {
        SignSelector.signSelectors.add(signSelector);
    }

	@Override
	public void onInitializeClient() {
		addGlobalReceivers();
		GlobalReceiverClient.bulkRegisterGlobalReceiversClient(globalReceiverClients);

		addBlockEntityRenderers();
		RegistrableBlockEntityRender.bulkRegisterBlockEntityRenderers(blockEntityRenderers);

		ClientCommandRegistrationCallback.EVENT.register(ModCommandsClient::initialize);

        registerOnChunkUnload();

		registerCustomProtocols();

		putBlockRenderLayers();

		OnlineImageCache.createCacheDir();

		ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
			MyWorldTrafficAddition.LOGGER.info("Loading Background Atlases from the Autoload folder...");
			autoloadBackgroundAtlases();
			MyWorldTrafficAddition.LOGGER.info("Loaded {} background atlases", BackgroundLoader.BACKGROUND_SPRITES.size());
		});

		loadPreferences();
	}

	private static void loadPreferences() {
		ClientPreferences.loadGameplayPreferences();
	}

	public static void putBlockRenderLayer(Block block, ChunkSectionLayer renderLayer) {
		BlockRenderLayerMap.putBlock(block, renderLayer);
	}

	private static void putBlockRenderLayers() {
		putBlockRenderLayer(ModBlocks.TRIANGULAR_SIGN_BLOCK.getBlock(), ChunkSectionLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.getBlock(), ChunkSectionLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.OCTAGONAL_SIGN_BLOCK.getBlock(), ChunkSectionLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.SIGN_HOLDER_BLOCK.getBlock(), ChunkSectionLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.CUSTOMIZABLE_SIGN_BORDER.getBlock(), ChunkSectionLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK.getBlock(), ChunkSectionLayer.CUTOUT);
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

                    Minecraft.getInstance().setScreen(new SignPoleRotationScreen(Minecraft.getInstance().level, pos, Minecraft.getInstance().player));
				}),

				new GlobalReceiverClient<>(OpenSignSelectionPayload.Id, (payload) -> {
					if (Minecraft.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open SignSelectionScreen because world or player is null!");
						return;
					}

					Minecraft.getInstance().setScreen(new EmptyScreen(Component.literal("Sign Selection"), signSelector::close));
                    signSelector.open(SignBlock.getSignSelectionEnum(payload.selection_type()), payload.pos(), payload.dimensionRegistryKey());
				}),

				new GlobalReceiverClient<>(OpenCustomizableSignEditScreen.Id, (payload) -> {
					BlockPos pos = payload.pos();

					if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open CustomizableSignSettingScreen because world or player is null!");
						return;
					}

					customizableSignSettingScreen = new CustomizableSignSettingScreen(Minecraft.getInstance().level, pos, Minecraft.getInstance().player);
					Minecraft.getInstance().setScreen(customizableSignSettingScreen);
				}),

				new GlobalReceiverClient<>(ShowImGuiWindow.Id, (payload -> {
					switch (ModVars.ImGuiWindowIds.values()[payload.windowId()]) {
						case ABOUT -> ImGuiRenderer.showAboutWindow = true;
						case DEMO -> ImGuiRenderer.showDemoWindow = !ImGuiRenderer.showDemoWindow;
						case PREF -> PreferencesWindow.open();
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
            ErrorPopup.open(error, null);
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

		MyWorldTrafficAddition.LOGGER.info("Thank you for playing MyWorld Traffic Addition! <3");
	}

    private static void registerOnChunkUnload() {
        ClientChunkEvents.CHUNK_UNLOAD.register((ClientLevel world, LevelChunk chunk) -> {
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof CustomizableSignBlockEntity csbEntity) {
                    customizableSignDeleteUnusedTextures(csbEntity);
                }
            }
        });
    }

    /**
     * Deletes all unused textures of the given CustomizableSignBlockEntity (if no elements are using them anymore). This is to prevent memory leaks.
     * @param blockEntity The CustomizableSignBlockEntity to delete unused textures from.
     */
    private static void customizableSignDeleteUnusedTextures(CustomizableSignBlockEntity blockEntity) {
        Map<BlockPos, List<ClientElementInterface>> elementMap = CustomizableSignBlockEntityRenderer.elements;
        List<ClientElementInterface> elements = elementMap.get(blockEntity.getBlockPos());

        if (elements == null)
            return;

        for (ClientElementInterface element : elements) {
            if (!(element instanceof TexturableElementInterface texturableElement)) // If the element is not texturable, skip it
                continue;

            if (!texturableElement.isTextureLoaded()) // If the texture is not loaded, skip it
                continue;

            DynamicTexture texture = texturableElement.getDynamicTexture();
            if (texture == null)
                continue;

            try {
                texture.destroy(); // Won't destroy just yet if there are subscribers
                texturableElement.markTextureStale(); // Mark texture as stale. NOW it get's destroyed if no subscribers are left
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.warn("Could not mark texture stale for {}", texturableElement, e);
            }
        }

        // Notify renderer that the chunk got unloaded
        CustomizableSignBlockEntityRenderer.invalidateTexture(blockEntity.getBlockPos());
    }

}