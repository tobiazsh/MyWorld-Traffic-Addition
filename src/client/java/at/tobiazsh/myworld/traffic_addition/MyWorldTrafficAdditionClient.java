package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowAboutWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowDemoWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.OpenCustomizableSignEditScreen;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.OpenSignPoleRotationScreenPayload;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.components.Renderers.*;
import at.tobiazsh.myworld.traffic_addition.components.Screens.CustomizableSignEditScreen;
import at.tobiazsh.myworld.traffic_addition.components.Screens.SignPoleRotationScreen;
import at.tobiazsh.myworld.traffic_addition.components.Screens.SignSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.*;
import static at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SpecialBlockEntity.SPECIAL_BLOCK_ENTITY;

public class MyWorldTrafficAdditionClient implements ClientModInitializer {

	public static CustomizableSignEditScreen customizableSignEditScreen;

	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(SPECIAL_BLOCK_ENTITY, SpecialBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(SIGN_POLE_BLOCK_ENTITY, SignPoleEntityRenderer::new);
		BlockEntityRendererFactories.register(TRIANGULAR_SIGN_BLOCK_ENTITY, TriangularSignBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY, UpsideDownTriangularSignBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(OCTAGONAL_SIGN_BLOCK_ENTITY, OctagonalSignBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(ROUND_SIGN_BLOCK_ENTITY, RoundSignBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, CustomSignBlockEntityRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(OpenSignPoleRotationScreenPayload.Id, (payload, context) -> {
			context.client().execute(() -> {
				BlockPos pos = payload.pos();
				MinecraftClient.getInstance().setScreen(new SignPoleRotationScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player));
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(OpenSignSelectionPayload.Id, ((payload, context) -> {
			context.client().execute(() -> {
				BlockPos pos = payload.pos();
				MinecraftClient.getInstance().setScreen(new SignSelectionScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player, ModVars.getSignSelectionEnum(payload.selection_type())));
			});
		}));

		ClientPlayNetworking.registerGlobalReceiver(OpenCustomizableSignEditScreen.Id, (((payload, context) -> {
			context.client().execute(() -> {
				BlockPos pos = payload.pos();
				customizableSignEditScreen = new CustomizableSignEditScreen(MinecraftClient.getInstance().world, pos, MinecraftClient.getInstance().player);
				MinecraftClient.getInstance().setScreen(customizableSignEditScreen);
			});
		})));

		ClientPlayNetworking.registerGlobalReceiver(ShowDemoWindow.Id, ((payload, context) -> {
			context.client().execute(() -> {
				ImGuiRenderer.showDemoWindow = !ImGuiRenderer.showDemoWindow;
			});
		}));

		ClientPlayNetworking.registerGlobalReceiver(ShowAboutWindow.Id, (payload, context) -> {
			context.client().execute(() -> {
				ImGuiRenderer.showAboutWindow = true;
			});
		});

		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TRIANGULAR_SIGN_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.OCTAGONAL_SIGN_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SIGN_HOLDER_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK, RenderLayer.getCutout());

		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_all")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_top")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_bottom")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_left")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_right")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_top_bottom")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_left_right")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_top_left")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_top_right")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_bottom_left")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_bottom_right")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_not_right")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_not_left")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_not_top")));
		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(Identifier.of(MyWorldTrafficAddition.MOD_ID, "block/customizable_sign_block_border_not_bottom")));
	}
}