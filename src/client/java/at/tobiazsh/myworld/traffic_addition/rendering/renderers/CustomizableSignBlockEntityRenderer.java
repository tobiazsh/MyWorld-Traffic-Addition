package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 09/09/2024 (DD/MM/YYYY) - 20:34
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.cache.LRUCache;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.*;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.CustomizableSignBlockRenderState;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.SignBlockRenderState;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface.zOffset;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class CustomizableSignBlockEntityRenderer implements BlockEntityRenderer<CustomizableSignBlockEntity, CustomizableSignBlockRenderState> {

    public static final int DEFAULT_CALCULATION_CACHE_SIZE = 256; // Default size for the calculation cache, can be adjusted if needed

    private static final LRUCache<AbstractMap.SimpleEntry<String, List<BlockPosExtended>>> CALCULATION_CACHE = new LRUCache<>(
            "CALCULATION_CACHE",
            Objects.requireNonNullElse(
                    ClientPreferences.gameplayPreference.getInt("calculationCacheSize"), // Get the size from the config
                    DEFAULT_CALCULATION_CACHE_SIZE
            )
    );

    private final BakedModelManager bakedModelManager;

    public static float zOffsetRenderLayer = 3f;
    public static final float zOffsetRenderLayerDefault = 3f;

    public static float elementDistancingRenderLayer = 0.75f;
    public static final float elementDistancingRenderLayerDefault = 0.75f;

    public static final Map<BlockPos, List<ClientElementInterface>> elements =
            Collections.synchronizedMap(new WeakHashMap<>());

    // Constructor
    public CustomizableSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        bakedModelManager = MinecraftClient.getInstance().getBakedModelManager();

        BorderRenderer.init(
                bakedModelManager,
                new CustomRenderLayer.ModelLayering(
                        zOffsetRenderLayer,
                        CustomRenderLayer.ModelLayering.LayeringType.CUTOUT_Z_OFFSET_BACKWARD
                )
        ); // Initialize the border renderer with the baked model manager
    }



    /**
     * Calculates the position of a BlockPosExtended. Basically just adds the distance to the master position.
     * @return a list of BlockPosExtended which represent the position of the signs.
     */
    private List<BlockPosExtended> calculatePosition(List<BlockPosExtended> distances, BlockPosExtended masterPos) {
        return distances.stream()
                .map(distance -> masterPos.addOffset(distance.invert())) // Add the distance to the master position
                .toList();
    }

    private List<BlockPosExtended> getSignDistances(String signDistancesStringEncoded) {
        List<BlockPosExtended> signDistances;

        if(signDistancesStringEncoded.isEmpty())
            return new ArrayList<>(); // If there are no signs, return an empty list

        // If already calculated, return the cached value
        if (CALCULATION_CACHE.anyMatch(match ->
                match.getKey().equals(signDistancesStringEncoded) &&
                !match.getValue().isEmpty()
        )) {
            // If the sign distances are already calculated, return them from the cache
            return CALCULATION_CACHE.filter(match ->
                    match.getKey().equals(signDistancesStringEncoded) &&
                    !match.getValue().isEmpty())
                    .getFirst().get().getValue();
        }

        try {
            // Decode the string to a list of BlockPosExtended which represent the distance to the master position
            List<String> signDistancingList = ListUtils.fromByteArray(Base64.getDecoder().decode(signDistancesStringEncoded));

            signDistances = signDistancingList.stream().map(BlockPosExtended.INSTANCE::fromString).toList();

            // If there are no signs, return an empty list
            if (signDistances.isEmpty()) return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to decode sign distances string: {}", signDistancesStringEncoded, e);
            return new ArrayList<>();
        }

        // Cache the calculated sign distances for later use
        CALCULATION_CACHE.access(new AbstractMap.SimpleEntry<>(signDistancesStringEncoded, signDistances));

        return signDistances;
    }


    @Override
    public CustomizableSignBlockRenderState createRenderState() {
        return new CustomizableSignBlockRenderState();
    }

    @Override
    public void updateRenderState(CustomizableSignBlockEntity blockEntity, CustomizableSignBlockRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        state.rotation = blockEntity.getRotation();
        state.height = blockEntity.getHeight();
        state.width = blockEntity.getWidth();

        state.isRendering = blockEntity.isRendering();
        state.isMaster = blockEntity.isMaster();
        state.isInitialized = blockEntity.isInitialized();

        state.masterBlockPos = blockEntity.getMasterPos();
        state.borders = blockEntity.getBorderType();

        state.signTextureJson = blockEntity.getSignTextureJson();

        if (blockEntity.elements != null && !blockEntity.elements.isEmpty()) {
            List<ClientElementInterface> clientList = blockEntity.elements.reversed().stream()
                    .map(ClientElementFactory::toClientElement)
                    .toList();
            // store either per-position
            elements.put(blockEntity.getPos(), clientList);
            // and/or directly in the state so render reads it
            state.clientElements = clientList;
        } else {
            state.clientElements = Collections.emptyList();
            elements.remove(blockEntity.getPos());
        }

        state.signPoleDistancesString = blockEntity.getSignPoleDistancesString();
        state.signDistancesString = blockEntity.getSignDistancesString();
    }

    // Render the sign block
    @Override
    public void render(CustomizableSignBlockRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {

        Direction facing = state.blockState.get(CustomizableSignBlock.FACING);

        // If the block shouldn't render, exit function, for example when block isn't a master block
        if (!state.isRendering) return;

        // Get the BlockEntity of the master block
        assert MinecraftClient.getInstance().world != null;
        BlockEntity masterEntity = MinecraftClient.getInstance().world.getBlockEntity(state.masterBlockPos);

        int rotation = 0;

        // Just a check to avoid errors
        if (masterEntity instanceof CustomizableSignBlockEntity) {
            // Define the rotation depending on the facing state of the sign
            rotation = ((CustomizableSignBlockEntity) masterEntity).getRotation();
        }

        matrices.push();

        // Rotate the sign
        rotateSign(rotation, matrices);

        // Render master block sign block
        BlockStateModel csbeStateModel = bakedModelManager.getBlockModels().getModel(state.blockState);

        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getCutout(),
                csbeStateModel,
                1.0f, 1.0f, 1.0f,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0
        );

        // Render the border for the master sign block
        BorderRenderer.render(queue, matrices, state.borders, state.lightmapCoordinates, facing);
        renderTexture(queue, state, matrices, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, facing);

        // If the entity is master, render the other signs attached to it
        if (state.isMaster) {
            renderSignPoles(queue, state, matrices, state.lightmapCoordinates);
            renderSigns(queue, state, csbeStateModel, matrices, state.lightmapCoordinates, facing);
        }

        matrices.pop();
    }




    private void renderSigns(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, BlockStateModel blockStateModel, MatrixStack matrices, int light, Direction facing) {
        // Get the sign positions as a list of BlockPos
        List<BlockPosExtended> signDistances = getSignDistances(state.signDistancesString);
        List<BlockPosExtended> signPositions = calculatePosition(signDistances, new BlockPosExtended(state.pos));

        // Render each sign
        for (int i = 0; i < signPositions.size(); i++) {
            if (Objects.requireNonNull(MinecraftClient.getInstance().world).getBlockEntity(signPositions.get(i)) instanceof CustomizableSignBlockEntity)
                renderSign(
                        queue,
                        state,
                        blockStateModel,
                        matrices,
                        light,
                        facing,
                        signDistances.get(i).invert()
                );
        }
    }




    // Render one sign
    private void renderSign(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, BlockStateModel blockStateModel, MatrixStack matrices, int light, Direction facing, BlockPosExtended offset) {
        matrices.push();

        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Set the sign to the correct position

        // Render sign block
        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getCutout(),
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        // Render the border on top of the sign
        BorderRenderer.render(queue, matrices, state.borders, light, facing);

        BlockPosFloat blockPosBehind = new BlockPosFloat(state.pos)
                .offset(
                        state.blockState.get(CustomizableSignBlock.FACING).getOpposite(),
                        1f
                );

        BlockEntity blockBehind = Objects.requireNonNull(MinecraftClient.getInstance().world)
                .getBlockEntity(
                        new BlockPos(
                                (int) blockPosBehind.x,
                                (int) blockPosBehind.y,
                                (int) blockPosBehind.z
                        )
                );

        if (blockBehind instanceof SignPoleBlockEntity) {
            renderSignHolder(queue, state, matrices, light, facing);
        }

        matrices.pop();
    }




    // Render the sign poles that hold the sign
    private void renderSignPoles(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, MatrixStack matrices, int light) {
        // Get the position of each sign pole compacted in one string
        if (state.signPoleDistancesString.isEmpty()) return; // If there are no sign poles, exit function

        // If already cached, return the cached value
        if (CALCULATION_CACHE.anyMatch(match ->
                match.getKey().equals(state.signPoleDistancesString) &&
                !match.getValue().isEmpty()
        )) {
            // If the sign pole positions are already calculated, return them from the cache
            List<BlockPosExtended> cachedPositions = CALCULATION_CACHE.filter(match ->
                    match.getKey().equals(state.signPoleDistancesString) &&
                    !match.getValue().isEmpty())
                    .getFirst().get().getValue();

            cachedPositions.forEach(pos -> renderSignPole(queue, state, bakedModelManager.getBlockModels().getModel(ModBlocks.SIGN_POLE_BLOCK.getBlock().getDefaultState()), matrices, light, pos));
            return;
        }

        List<BlockPosExtended> polePositions;

        try {
            // Convert the string to a list of BlockPosExtended which represent the distance to the master position
            List<BlockPosExtended> distances = ListUtils.fromByteArray(Base64.getDecoder().decode(state.signPoleDistancesString)).stream().map(distance -> BlockPosExtended.INSTANCE.fromString((String) distance)).toList();

            // Add distance to master position to get the actual position of the sign pole
            polePositions = distances.stream()
                    .map(distance -> (new BlockPosExtended(state.pos)).addOffset(distance.invert()))
                    .toList();

        } catch (IOException | ClassNotFoundException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to decode sign pole positions string: {}", state.signPoleDistancesString, e);
            throw new RuntimeException("Failed to decode sign pole positions string", e);
        }

        // If there are no sign poles, don't do anything
        if(polePositions.isEmpty()) return;

        // Cache the calculated sign pole positions for later use
        CALCULATION_CACHE.access(new AbstractMap.SimpleEntry<>(state.signPoleDistancesString, polePositions));

        // Define the BakedModel for the sign poles
        BlockStateModel signPoleStateModel = bakedModelManager.getBlockModels().getModel(ModBlocks.SIGN_POLE_BLOCK.getBlock().getDefaultState());

        // Render each sign pole
        polePositions.forEach(pos -> renderSignPole(queue, state, signPoleStateModel, matrices, light, pos));
    }




    // Render one sign pole
    private void renderSignPole(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, BlockStateModel blockStateModel, MatrixStack matrices, int light, BlockPos position) {
        // The position if the master block
        BlockPos masterPos = state.masterBlockPos;
        BlockPos offset = BlockPosExtended.getOffset(masterPos, position); // Offset of the sign. If the sign pole is one behind, the offset is (0, 0, -1) for example

        // Correct the offset to match the sign pole position
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1));

        matrices.push();

        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Translate the sign pole to the correct position

        // Render sign pole
        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getCutout(),
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }




    // Render the texture of the sign
    private void renderTexture(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, MatrixStack matrices, int light, int overlay, Direction facing) {
        // If the block isn't a master block, exit function because there's nothing to render anyway since non-masters don't hold texture information
        if (!state.isMaster || !state.isInitialized) return;

        renderBackground(queue, state, state.height, state.width, matrices, light, overlay, facing);
        renderElements(queue, state, state.height, matrices, light, facing);
    }




    // Render the background texture of the sign
    private void renderBackground(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, int height, int width, MatrixStack matrices, int light, int overlay, Direction facing) {
        // If there's nothing to render, exit
        if (state.backgroundPieces.isEmpty()) return;

//        if (!Objects.equals(state.cachedSignTextureJson, state.signTextureJson)) {
//            state.backgroundPieces = CustomizableSignData.getBackgroundTexturePathList(new CustomizableSignData().setJson(state.signTextureJson)).reversed();
//            state.backgroundPieces.replaceAll(s -> s.replaceFirst("/assets/".concat(MyWorldTrafficAddition.MOD_ID).concat("/"), ""));
//            state.cachedSignTextureJson = state.signTextureJson;
//        }


        // Coordinates of the master block
        BlockPos masterPos = state.masterBlockPos;
        BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, zOffset);

        matrices.push();

        // Render from top to bottom and from left to right
        int currentListPos = 0;
        for (int i = height; i > 0; i--) {
            for (int j = width; j > 0; j--) {
                if (currentListPos >= state.backgroundPieces.size()) break; // Prevent out of bounds crashes

                BlockPos renderPos = masterPos.up(i - 1);
                renderPos = blockPosInDirection(getRightSideDirection(facing.getOpposite()), renderPos, j - 1);

                BlockPos offset = BlockPosExtended.getOffset(masterPos, renderPos);
                offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1)); // The position of the texture

                Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, state.backgroundPieces.get(currentListPos));

                CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffsetRenderLayer, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID, texture);
                RenderLayer renderLayer = imageLayering.buildRenderLayer();

                BlockPos finalOffset = offset;
                queue.submitCustom(matrices, renderLayer, (ms, vertexConsumer) -> {
                    matrices.push();

                    matrices.translate(finalOffset.getX(), finalOffset.getY(), finalOffset.getZ()); // Position the texture
                    matrices.translate(forwardShift.x, forwardShift.y, forwardShift.z); // Forward shift so it's visible and not rendered inside the other textures

                    // Turn to match the facing direction
                    matrices.translate(0.5, 0.5, 0.5);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
                    matrices.translate(-0.5, -0.5, -0.5);

                    // Position the vertices
                    vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.0f, 0f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
                    vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 1f, 0f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
                    vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 1f, 1f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);
                    vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.0f, 1f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);

                    matrices.pop();
                });

                currentListPos++; // Move to the next texture

            }
        }

        matrices.pop();
    }




    // Render the elements that were placed when the sign was edited
    private void renderElements(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, int height, MatrixStack matrices, int light, Direction facing) {
        if (state.clientElements.isEmpty()) return; // If there are no elements, exit
//        if (!Objects.equals(state.cachedSignTextureJson, state.signTextureJson)) {
//            elements.put(state.pos, .elements.reversed().stream().map(ClientElementFactory::toClientElement).toList()); // Reverse so top most element gets rendered last
//            csbe.setUpdateOccurred(false); // Reset the update flag
//        }

        List<ClientElementInterface> renderedElements = state.clientElements;

        for (int i = 0; i < renderedElements.size(); i++) {
            ClientElementInterface element = renderedElements.get(i);
            renderElement(queue, element, i, height, matrices, light, facing);
        }
    }

    public static void renderElement(OrderedRenderCommandQueue queue, ClientElementInterface element, int index, int height, MatrixStack matrices, int light, Direction facing) {
        element.renderMinecraft(queue, index, height, matrices, light, facing);
    }




    private void renderSignHolder(OrderedRenderCommandQueue queue, CustomizableSignBlockRenderState state, MatrixStack matrices, int light, Direction facing) {
        BlockStateModel blockStateModel = bakedModelManager.getBlockModels().getModel(ModBlocks.SIGN_HOLDER_BLOCK.getBlock().getDefaultState());

        matrices.push();

        BlockPos holderPos = state.pos.offset(facing, 1); // Position of the sign holder is one block in front of the sign
        matrices.translate(Vec3d.of(BlockPosExtended.getOffset(state.pos, holderPos))); // Translate the sign holder to the correct position);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
        matrices.translate(-0.5, -0.5, -0.5);

        queue.submitBlockStateModel(
                matrices,
                RenderLayer.getCutout(),
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }




    private void rotateSign(int rotationDegrees, MatrixStack matrices) {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        matrices.translate(-0.5, -0.5, -0.5);
    }

}
