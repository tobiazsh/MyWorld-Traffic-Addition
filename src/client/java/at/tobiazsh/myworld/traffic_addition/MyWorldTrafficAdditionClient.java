package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.PreferencesWindow;
import at.tobiazsh.myworld.traffic_addition.networking.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.networking.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.*;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.ShowImGuiWindow;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.block_modification.OpenCustomizableSignEditScreen;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.block_modification.OpenSignPoleRotationScreenPayload;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.block_modification.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.screens.CustomizableSignSettingScreen;
import at.tobiazsh.myworld.traffic_addition.screens.SignPoleRotationScreen;
import at.tobiazsh.myworld.traffic_addition.screens.SignSelectionScreen;
import imgui.ImGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.*;
import static at.tobiazsh.myworld.traffic_addition.components.block_entities.SpecialBlockEntity.SPECIAL_BLOCK_ENTITY;

public class MyWorldTrafficAdditionClient implements ClientModInitializer {

	public static CustomizableSignSettingScreen customizableSignSettingScreen;

	private static final List<GlobalReceiverClient<? extends CustomPayload>> globalReceiverClients = new ArrayList<>();
	private static final List<String> modelPaths = new ArrayList<>();
	private static final List<RegistrableBlockEntityRender<? extends BlockEntity>> blockEntityRenderers = new ArrayList<>();

	public static final ImGui imgui = new ImGui(); // I have to use this since a static reference crashes the program when I call calcTextSize / calcItemSize

	@Override
	public void onInitializeClient() {
		addGlobalReceivers();
		GlobalReceiverClient.bulkRegisterGlobalReceiversClient(globalReceiverClients);

		addBlockEntityRenderers();
		RegistrableBlockEntityRender.bulkRegisterBlockEntityRenderers(blockEntityRenderers);

		addModelPaths();
		registerNonBlockModels();
		registerCustomProtocols();

		putBlockRenderLayers();

		OnlineImageCache.createCacheDir();

		loadPreferences();
	}

	private static void loadPreferences() {
		ClientPreferences.loadGameplayPreferences();
	}

	public static void putBlockRenderLayer(Block block, RenderLayer renderLayer) {
		BlockRenderLayerMap.INSTANCE.putBlock(block, renderLayer);
	}

	private static Identifier genModId(String path) {
		return Identifier.of(MyWorldTrafficAddition.MOD_ID, path);
	}

	private static void putBlockRenderLayers() {
		putBlockRenderLayer(ModBlocks.TRIANGULAR_SIGN_BLOCK, RenderLayer.getCutout());
		putBlockRenderLayer(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK, RenderLayer.getCutout());
		putBlockRenderLayer(ModBlocks.OCTAGONAL_SIGN_BLOCK, RenderLayer.getCutout());
		putBlockRenderLayer(ModBlocks.SIGN_HOLDER_BLOCK, RenderLayer.getCutout());
		putBlockRenderLayer(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK, RenderLayer.getCutout());
	}

	private static void registerNonBlockModel(Identifier id) {
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(id));
	}

	private static void registerNonBlockModels() {
		modelPaths.forEach(path -> registerNonBlockModel(genModId(path)));
	}

	private static void addBlockEntityRenderers() {
		blockEntityRenderers.addAll(Arrays.asList(
				new RegistrableBlockEntityRender<>(SPECIAL_BLOCK_ENTITY, SpecialBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(SIGN_POLE_BLOCK_ENTITY, SignPoleEntityRenderer::new),
				new RegistrableBlockEntityRender<>(TRIANGULAR_SIGN_BLOCK_ENTITY, TriangularSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY, UpsideDownTriangularSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(OCTAGONAL_SIGN_BLOCK_ENTITY, OctagonalSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(ROUND_SIGN_BLOCK_ENTITY, RoundSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, CustomizableSignBlockEntityRenderer::new)
		));
	}

	private static void addModelPaths() {
		modelPaths.addAll(Arrays.asList(
				"block/customizable_sign_block_border_all",
				"block/customizable_sign_block_border_top",
				"block/customizable_sign_block_border_bottom",
				"block/customizable_sign_block_border_left",
				"block/customizable_sign_block_border_right",
				"block/customizable_sign_block_border_top_bottom",
				"block/customizable_sign_block_border_left_right",
				"block/customizable_sign_block_border_top_left",
				"block/customizable_sign_block_border_top_right",
				"block/customizable_sign_block_border_bottom_left",
				"block/customizable_sign_block_border_bottom_right",
				"block/customizable_sign_block_border_not_right",
				"block/customizable_sign_block_border_not_left",
				"block/customizable_sign_block_border_not_top",
				"block/customizable_sign_block_border_not_bottom"
		));
	}

	private static void addGlobalReceivers() {
		globalReceiverClients.addAll(Arrays.asList(
				new GlobalReceiverClient<>(OpenSignPoleRotationScreenPayload.Id, (payload) -> {
					BlockPos pos = payload.pos();

					if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open SignPoleRotationScreen because world or player is null!");
						return;
					}

                    MinecraftClient.getInstance().setScreen(new SignPoleRotationScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player));
				}),

				new GlobalReceiverClient<>(OpenSignSelectionPayload.Id, (payload) -> {
					BlockPos pos = payload.pos();

					if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open SignSelectionScreen because world or player is null!");
						return;
					}

					MinecraftClient.getInstance().setScreen(new SignSelectionScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player, ModVars.getSignSelectionEnum(payload.selection_type())));
				}),

				new GlobalReceiverClient<>(OpenCustomizableSignEditScreen.Id, (payload) -> {
					BlockPos pos = payload.pos();

					if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open CustomizableSignSettingScreen because world or player is null!");
						return;
					}

					customizableSignSettingScreen = new CustomizableSignSettingScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player);
					MinecraftClient.getInstance().setScreen(customizableSignSettingScreen);
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
                        (protocolId, data, handler) -> MinecraftClient.getInstance().execute(() -> handler.accept(data))
                ))
		));
	}

	private static void registerCustomProtocols() {
		// Get maximum image upload size
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_maximum_image_upload_size"), bytes -> {
			String maximumSize_str = new String(bytes);
            OnlineImageDialog.maximumUploadSize = Long.parseLong(maximumSize_str);
		});

		// Get total number of uploaded images
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_total_uploaded_images"), OnlineImageLogic::setImageCount);

		// Get number of private images uploaded by the player
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_private_uploaded_images"), OnlineImageLogic::setPrivateImageCount);

		// Get metadata of uploaded images
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_image_entries_metadata"), OnlineImageLogic::setMetadataList);

		// Get thumbnail of uploaded images
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_thumbnail_data"), OnlineImageLogic::setThumbnailData);

		// Get image data
		CustomClientNetworking.getInstance().registerProtocolHandler(Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_image_data"), OnlineImageLogic::setImageData);
	}

	public static void onStopGame() {
		MyWorldTrafficAddition.LOGGER.info("Shutting down MyWorld Traffic Addition!");

		MyWorldTrafficAddition.LOGGER.info("Clearing image cache...");
		OnlineImageCache.clearCache();

		MyWorldTrafficAddition.LOGGER.info("Thank you for playing MyWorld Traffic Addition! <3");
	}
}