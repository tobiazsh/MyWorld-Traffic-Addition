package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SpecialBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.*;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowAboutWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowDemoWindow;
import at.tobiazsh.myworld.traffic_addition.Utils.Coordinates;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
	@author Tobias
	@owner Tobias
	@mod MyWorld Traffic Addition

	You may modify this code and share with others but you may not make profit off of it and you must credit me if you share this code without any changes except for development purposes.

 */

public class MyWorldTrafficAddition implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("myworld_traffic_addition");

	public static final String MOD_ID = "myworld_traffic_addition";
	public static final String MOD_ID_HUMAN = "MyWorld Traffic Addition";

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModGroups.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModCommands.initialize();

		SpecialBlockEntity.initialize();

		// Server to Client
		PayloadTypeRegistry.playS2C().register(OpenSignPoleRotationScreenPayload.Id, OpenSignPoleRotationScreenPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenSignSelectionPayload.Id, OpenSignSelectionPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenCustomizableSignEditScreen.Id, OpenCustomizableSignEditScreen.CODEC);
		PayloadTypeRegistry.playS2C().register(ShowDemoWindow.Id, ShowDemoWindow.CODEC);
		PayloadTypeRegistry.playS2C().register(ShowAboutWindow.Id, ShowAboutWindow.CODEC);

		// Client to Server
		PayloadTypeRegistry.playC2S().register(SignPoleRotationPayload.Id, SignPoleRotationPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SignBlockTextureChangePayload.Id, SignBlockTextureChangePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SignBlockRotationPayload.Id, SignBlockRotationPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SignBlockBackstepCoordsChange.Id, SignBlockBackstepCoordsChange.CODEC);
		PayloadTypeRegistry.playC2S().register(SetMasterCustomizableSignBlockPayload.Id, SetMasterCustomizableSignBlockPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetBorderTypeCustomizableSignBlockPayload.Id, SetBorderTypeCustomizableSignBlockPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetShouldRenderSignPolePayload.Id, SetShouldRenderSignPolePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetSignPolePositionsCustomizableSignBlockPayload.Id, SetSignPolePositionsCustomizableSignBlockPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetSignPositionsCustomizableSignBlockPayload.Id, SetSignPositionsCustomizableSignBlockPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetRenderStateCustomizableSignBlockPayload.Id, SetRenderStateCustomizableSignBlockPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetRotationCustomizableSignBlockPayload.Id, SetRotationCustomizableSignBlockPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetSizeCustomizableSignPayload.Id, SetSizeCustomizableSignPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetCustomizableSignTexture.Id, SetCustomizableSignTexture.CODEC);
		PayloadTypeRegistry.playC2S().register(UpdateTextureVarsCustomizableSignBlockPayload.Id, UpdateTextureVarsCustomizableSignBlockPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SignPoleRotationPayload.Id, MyWorldTrafficAddition::handleSignPoleRotationPayloadReceived);
		ServerPlayNetworking.registerGlobalReceiver(SignBlockTextureChangePayload.Id, MyWorldTrafficAddition::handleSignBlockTextureChange);
		ServerPlayNetworking.registerGlobalReceiver(SignBlockRotationPayload.Id, MyWorldTrafficAddition::handleSignBlockRotationChange);
		ServerPlayNetworking.registerGlobalReceiver(SignBlockBackstepCoordsChange.Id, MyWorldTrafficAddition::handleSignBlockBackstepCoordsChange);
		ServerPlayNetworking.registerGlobalReceiver(SetMasterCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleSetMasterCustomizableSignBlockReceived);
		ServerPlayNetworking.registerGlobalReceiver(SetBorderTypeCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleSetBorderTypeCustomizableSignBlockReceived);
		ServerPlayNetworking.registerGlobalReceiver(SetShouldRenderSignPolePayload.Id, MyWorldTrafficAddition::handleSetShouldRenderSignPolePayload);
		ServerPlayNetworking.registerGlobalReceiver(SetSignPolePositionsCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleSetSignPolePositionsCustomizableSignBlockReceived);
		ServerPlayNetworking.registerGlobalReceiver(SetSignPositionsCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleSetSignPositionsCustomizableSignBlockReceived);
		ServerPlayNetworking.registerGlobalReceiver(SetRenderStateCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleSetRenderStateCustomizableSignBlockPayload);
		ServerPlayNetworking.registerGlobalReceiver(SetRotationCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleSetRotationCustomizableSignBlockPayload);
		ServerPlayNetworking.registerGlobalReceiver(SetSizeCustomizableSignPayload.Id, MyWorldTrafficAddition::handleSetSizeCustomizableSignPayload);
		ServerPlayNetworking.registerGlobalReceiver(SetCustomizableSignTexture.Id, MyWorldTrafficAddition::handleSetCustomizableSignTexture);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTextureVarsCustomizableSignBlockPayload.Id, MyWorldTrafficAddition::handleUpdateTextureVarsCustomizableSignBlockPayload);
	}

	private static void handleUpdateTextureVarsCustomizableSignBlockPayload(UpdateTextureVarsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {
				customizableSignBlockEntity.updateTextureVars();
			});
		}
	}

	private static void handleSetCustomizableSignTexture(SetCustomizableSignTexture payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		String json = payload.json();

		if (world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {
				customizableSignBlockEntity.setSignTextureJson(json);
			});
		}
	}

	private static void handleSetSizeCustomizableSignPayload(SetSizeCustomizableSignPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		int height = payload.height();
		int width = payload.width();

		if (world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {

				if (height != -1) customizableSignBlockEntity.setHeight(height);
				if (width != -1) customizableSignBlockEntity.setWidth(width);

				customizableSignBlockEntity.setInitialized(true);
			});
		}
	}

	private static void handleSetRotationCustomizableSignBlockPayload(SetRotationCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		int rotation = payload.rotation();

		if (world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {
				customizableSignBlockEntity.setRotation(rotation);
			});
		}
	}

	private static void handleSetRenderStateCustomizableSignBlockPayload(SetRenderStateCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		boolean renderState = payload.renderState();

		if (world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {
				customizableSignBlockEntity.setRenderingState(renderState);
			});
		}
	}

	private static void handleSetSignPositionsCustomizableSignBlockReceived(SetSignPositionsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		String blockPosString = payload.blockPosString();

		if (world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {
				customizableSignBlockEntity.setSignPositions(blockPosString);
			});
		}
	}

	private static void handleSetSignPolePositionsCustomizableSignBlockReceived(SetSignPolePositionsCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		String blockPosString = payload.blockPosString();

		if (world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity customizableSignBlockEntity) {
			world.getServer().execute(() -> {
				customizableSignBlockEntity.setSignPolePositions(blockPosString);
			});
		}
	}

	private static void handleSetShouldRenderSignPolePayload(SetShouldRenderSignPolePayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		boolean value = payload.value();

		if (world.getBlockEntity(pos) instanceof SignPoleBlockEntity blockEntity) {
			world.getServer().execute(() -> {
				blockEntity.setShouldRender(value);
			});
		}
	}

	private static void handleSetBorderTypeCustomizableSignBlockReceived(SetBorderTypeCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		String modelPath = payload.modelPath();
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof CustomizableSignBlockEntity csbeBlockEntity) {
			world.getServer().execute(() -> {
				csbeBlockEntity.setBorderType(modelPath);
			});
		}
	}

	private static void handleSetMasterCustomizableSignBlockReceived(SetMasterCustomizableSignBlockPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		Boolean shouldMaster = payload.shouldMaster();
		BlockPos masterPos = payload.master();
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof CustomizableSignBlockEntity) {
			world.getServer().execute(() -> {
				CustomizableSignBlockEntity csbeBlockEntity = (CustomizableSignBlockEntity) blockEntity;

				csbeBlockEntity.setMaster(shouldMaster);
				csbeBlockEntity.setMasterPos(masterPos);
			});
		}
	}

	private static void handleSignPoleRotationPayloadReceived (SignPoleRotationPayload payload, ServerPlayNetworking.Context context) {
		ServerPlayerEntity serverPlayer = context.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		int rotation = payload.rotation();
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof SignPoleBlockEntity) {
			world.getServer().execute(() -> {
				((SignPoleBlockEntity) blockEntity).setRotationValue(rotation);
			});
		}
	}

	private static void handleSignBlockTextureChange(SignBlockTextureChangePayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		String textureId = payload.textureId();
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof SignBlockEntity signBlockEntity) {
			world.getServer().execute(() -> {
				signBlockEntity.setTextureId(textureId);
			});
		}
	}

	private static void handleSignBlockRotationChange(SignBlockRotationPayload payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		int rotation = payload.rotation();
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof SignBlockEntity signBlockEntity) {
			world.getServer().execute(() -> {
				signBlockEntity.setRotation(rotation);
			});
		}
	}

	private static void handleSignBlockBackstepCoordsChange(SignBlockBackstepCoordsChange payload, ServerPlayNetworking.Context ctx) {
		ServerPlayerEntity serverPlayer = ctx.player();
		ServerWorld world = serverPlayer.getServerWorld();
		BlockPos pos = payload.pos();
		float x = payload.x();
		float y = payload.y();
		float z = payload.z();
		Direction direction = payload.direction();
		BlockEntity blockEntity = world.getBlockEntity(pos);
		Coordinates coordinates = new Coordinates(x, y, z, direction);

		if(blockEntity instanceof SignBlockEntity signBlockEntity) {
			world.getServer().execute(() -> {
				signBlockEntity.setBackstepCoords(coordinates);
			});
		}
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