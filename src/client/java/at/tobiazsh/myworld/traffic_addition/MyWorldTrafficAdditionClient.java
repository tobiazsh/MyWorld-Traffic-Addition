package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.PreferencesWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignSelector;
import at.tobiazsh.myworld.traffic_addition.networking.ChunkedDataPayload;
import at.tobiazsh.myworld.traffic_addition.networking.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.*;
import at.tobiazsh.myworld.traffic_addition.screens.EmptyScreen;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.ShowImGuiWindow;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.OpenCustomizableSignEditScreen;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.OpenSignPoleRotationScreenPayload;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.screens.CustomizableSignSettingScreen;
import at.tobiazsh.myworld.traffic_addition.screens.SignPoleRotationScreen;
import at.tobiazsh.myworld.traffic_addition.utils.custom_image.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.utils.custom_image.OnlineImageLogic;
import imgui.ImGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.*;

public class MyWorldTrafficAdditionClient implements ClientModInitializer {

	public static CustomizableSignSettingScreen customizableSignSettingScreen;
    public static final SignSelector signSelector = new SignSelector();

	private static final List<GlobalReceiverClient<? extends CustomPayload>> globalReceiverClients = new ArrayList<>();
	private static final List<RegistrableBlockEntityRender<? extends BlockEntity>> blockEntityRenderers = new ArrayList<>();

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

		registerCustomProtocols();

		putBlockRenderLayers();

		OnlineImageCache.createCacheDir();

		loadPreferences();
	}

	private static void loadPreferences() {
		ClientPreferences.loadGameplayPreferences();
	}

	public static void putBlockRenderLayer(Block block, BlockRenderLayer renderLayer) {
		BlockRenderLayerMap.putBlock(block, renderLayer);
	}

	private static void putBlockRenderLayers() {
		putBlockRenderLayer(ModBlocks.TRIANGULAR_SIGN_BLOCK.getBlock(), BlockRenderLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.getBlock(), BlockRenderLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.OCTAGONAL_SIGN_BLOCK.getBlock(), BlockRenderLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.SIGN_HOLDER_BLOCK.getBlock(), BlockRenderLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.CUSTOMIZABLE_SIGN_BORDER.getBlock(), BlockRenderLayer.CUTOUT);
		putBlockRenderLayer(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK.getBlock(), BlockRenderLayer.CUTOUT);
	}

	private static void addBlockEntityRenderers() {
		blockEntityRenderers.addAll(Arrays.asList(
				new RegistrableBlockEntityRender<>(SIGN_POLE_BLOCK_ENTITY, SignPoleEntityRenderer::new),
				new RegistrableBlockEntityRender<>(TRIANGULAR_SIGN_BLOCK_ENTITY, TriangularSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY, UpsideDownTriangularSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(OCTAGONAL_SIGN_BLOCK_ENTITY, OctagonalSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(ROUND_SIGN_BLOCK_ENTITY, RoundSignBlockEntityRenderer::new),
				new RegistrableBlockEntityRender<>(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, CustomizableSignBlockEntityRenderer::new)
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
					if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
						MyWorldTrafficAddition.LOGGER.warn("Cannot open SignSelectionScreen because world or player is null!");
						return;
					}

					MinecraftClient.getInstance().setScreen(new EmptyScreen(Text.literal("Sign Selection"), signSelector::close));
                    signSelector.open(SignBlock.getSignSelectionEnum(payload.selection_type()));
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