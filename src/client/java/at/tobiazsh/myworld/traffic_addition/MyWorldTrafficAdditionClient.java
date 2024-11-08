package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowAboutWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowDemoWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.OpenCustomizableSignEditScreen;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.OpenSignPoleRotationScreenPayload;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.components.Renderers.*;
import at.tobiazsh.myworld.traffic_addition.components.Screens.CustomizableSignSettingScreen;
import at.tobiazsh.myworld.traffic_addition.components.Screens.SignPoleRotationScreen;
import at.tobiazsh.myworld.traffic_addition.components.Screens.SignSelectionScreen;
import at.tobiazsh.myworld.traffic_addition.components.Utils.GlobalReceiverClient;
import at.tobiazsh.myworld.traffic_addition.components.Utils.RegistrableBlockEntityRender;
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
import static at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SpecialBlockEntity.SPECIAL_BLOCK_ENTITY;

public class MyWorldTrafficAdditionClient implements ClientModInitializer {

	public static CustomizableSignSettingScreen customizableSignSettingScreen;

	private static List<GlobalReceiverClient<? extends CustomPayload>> globalReceiverClients = new ArrayList<>();
	private static List<String> modelPaths = new ArrayList<>();
	private static List<RegistrableBlockEntityRender<? extends BlockEntity>> blockEntityRenderers = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		addGlobalReceivers();
		GlobalReceiverClient.bulkRegisterGlobalReceiversClient(globalReceiverClients);

		addBlockEntityRenderers();
		RegistrableBlockEntityRender.bulkRegisterBlockEntityRenderers(blockEntityRenderers);

		addModelPaths();
		registerNonBlockModels();

		putBlockRenderLayers();
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
				new RegistrableBlockEntityRender<>(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, CustomSignBlockEntityRenderer::new)
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
					MinecraftClient.getInstance().setScreen(new SignPoleRotationScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player));
				}),

				new GlobalReceiverClient<>(OpenSignSelectionPayload.Id, (payload) -> {
					BlockPos pos = payload.pos();
					MinecraftClient.getInstance().setScreen(new SignSelectionScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player, ModVars.getSignSelectionEnum(payload.selection_type())));
				}),

				new GlobalReceiverClient<>(OpenCustomizableSignEditScreen.Id, (payload) -> {
					BlockPos pos = payload.pos();
					customizableSignSettingScreen = new CustomizableSignSettingScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player);
					MinecraftClient.getInstance().setScreen(customizableSignSettingScreen);
				}),

				new GlobalReceiverClient<>(ShowDemoWindow.Id,
						(payload) -> ImGuiRenderer.showDemoWindow = !ImGuiRenderer.showDemoWindow),

				new GlobalReceiverClient<>(ShowAboutWindow.Id,
						(payload) -> ImGuiRenderer.showAboutWindow = true)
		));
	}
}