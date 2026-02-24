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
import at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences;
import at.tobiazsh.myworld.traffic_addition.rendering.renderstates.CustomizableSignBlockRenderState;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface.zOffset;

public class CustomizableSignBlockEntityRenderer implements BlockEntityRenderer<@NotNull CustomizableSignBlockEntity, @NotNull CustomizableSignBlockRenderState> {

    public static final int DEFAULT_CALCULATION_CACHE_SIZE = 256; // Default size for the calculation cache, can be adjusted if needed

    private static final LRUCache<AbstractMap.SimpleEntry<String, List<BlockPosExtended>>> CALCULATION_CACHE = new LRUCache<>(
            "CALCULATION_CACHE",
            Objects.requireNonNullElse(
                    ClientPreferences.gameplayPreference.getInt("calculationCacheSize"), // Get the size from the config
                    DEFAULT_CALCULATION_CACHE_SIZE
            )
    );

    private final ModelManager bakedModelManager;

    public static float zOffsetRenderLayer = 3f;
    public static final float zOffsetRenderLayerDefault = 3f;

    public static float elementDistancingRenderLayer = 0.75f;
    public static final float elementDistancingRenderLayerDefault = 0.75f;

    public static final Map<BlockPos, List<ClientElementInterface>> elements =
            Collections.synchronizedMap(new WeakHashMap<>());

    // Constructor
    public CustomizableSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        bakedModelManager = Minecraft.getInstance().getModelManager();

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

    /**
     * Invalidates the csbe's texture at the specified position
     */
    public static void invalidateTexture(BlockPos pos) {
        elements.remove(pos); // Remove elements associated with the block
    }

    @Override
    public CustomizableSignBlockRenderState createRenderState() {
        return new CustomizableSignBlockRenderState();
    }

    @Override
    public void extractRenderState(CustomizableSignBlockEntity blockEntity, CustomizableSignBlockRenderState state, float tickProgress, @NotNull Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        state.isRendering = blockEntity.isRendering();
        state.isMaster = blockEntity.isMaster();

        if (!blockEntity.isRendering()) return; // If the block shouldn't render, exit function

        state.rotation = blockEntity.getRotation();
        state.height = blockEntity.getHeight();
        state.width = blockEntity.getWidth();

        state.isInitialized = blockEntity.isInitialized();

        state.masterBlockPos = blockEntity.getMasterPos();
        state.borders = blockEntity.getBorderType();

        state.textureData = blockEntity.getTextureData();

        if (elements.containsKey(blockEntity.getBlockPos()) && !blockEntity.hasTextureUpdateOccurred.get()) {
            state.clientElements = elements.get(blockEntity.getBlockPos());
        } else {
            List<ClientElementInterface> clientElements = blockEntity.getTextureData().getElementContainer().getElements()
                    .reversed()
                    .stream()
                    .map(ClientElementFactory::toClientElement)
                    .filter(Objects::nonNull)
                    .toList();

            state.clientElements = clientElements;
            elements.put(blockEntity.getBlockPos(), clientElements); // Update the cached elements
            blockEntity.hasTextureUpdateOccurred.set(false); // Reset the flag
        }

        state.signPoleDistancesString = blockEntity.getSignPoleDistancesString();
        state.signDistancesString = blockEntity.getSignDistancesString();
    }

    // Render the sign block
    @Override
    public void submit(CustomizableSignBlockRenderState state, @NotNull PoseStack matrices, @NotNull SubmitNodeCollector queue, @NotNull CameraRenderState cameraState) {

        // If the block shouldn't render, exit function, for example when block isn't a master block
        if (!state.isRendering) return;

        // Get the facing of the sign block
        Direction facing = state.blockState.getValue(CustomizableSignBlock.FACING);

        // Get the BlockEntity of the master block
        assert Minecraft.getInstance().level != null;

        int rotation = state.rotation;

        matrices.pushPose();

        // Rotate the sign
        rotateSign(rotation, matrices);

        // Render master block sign block
        BlockStateModel csbeStateModel = bakedModelManager.getBlockModelShaper().getBlockModel(state.blockState);

        queue.submitBlockModel(
                matrices,
                RenderTypes.cutoutMovingBlock(),
                csbeStateModel,
                1.0f, 1.0f, 1.0f,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        // Render the border for the master sign block
        BorderRenderer.render(queue, matrices, state.borders, state.lightCoords, facing);
        renderTexture(queue, state, matrices, state.lightCoords, facing);

        boolean masterPresent = false;
        if (Minecraft.getInstance().level != null) {
            masterPresent = Minecraft.getInstance().level.getBlockEntity(state.masterBlockPos) instanceof CustomizableSignBlockEntity;
        }

        // If the entity is master, render the other signs attached to it
        if (state.isMaster || !masterPresent) {
            renderSignPoles(queue, state, matrices, state.lightCoords);
            renderSigns(queue, state, csbeStateModel, matrices, state.lightCoords, facing);
        }

        matrices.popPose();
    }




    private void renderSigns(SubmitNodeCollector queue, CustomizableSignBlockRenderState state, BlockStateModel blockStateModel, PoseStack matrices, int light, Direction facing) {
        // Get the sign positions as a list of BlockPos
        List<BlockPosExtended> signDistances = getSignDistances(state.signDistancesString);
        List<BlockPosExtended> signPositions = calculatePosition(signDistances, new BlockPosExtended(state.blockPos));

        // Render each sign
        for (int i = 0; i < signPositions.size(); i++) {
            BlockPos signPos = signPositions.get(i).toBlockPos();
            CustomizableSignBlockEntity csbe = null;
            if (Minecraft.getInstance().level != null) {
                BlockEntity be = Minecraft.getInstance().level.getBlockEntity(signPos);
                if (be instanceof CustomizableSignBlockEntity) csbe = (CustomizableSignBlockEntity) be;
            }

            BorderProperty borderType = (csbe != null) ? csbe.getBorderType() : state.borders;

            renderSign(
                    queue,
                    state,
                    blockStateModel,
                    matrices,
                    light,
                    facing,
                    signDistances.get(i).invert(),
                    borderType,
                    OverlayTexture.NO_OVERLAY,
                    signPos
            );
        }
    }




    // Render one sign
    private void renderSign(SubmitNodeCollector queue, CustomizableSignBlockRenderState masterState, BlockStateModel blockStateModel, PoseStack matrices, int light, Direction facing, BlockPosExtended offset, BorderProperty borderType, int backgroundOverlay, BlockPos position) {
        matrices.pushPose();

        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Set the sign to the correct position

        // Render sign block
        queue.submitBlockModel(
                matrices,
                RenderTypes.cutoutMovingBlock(),
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        BackgroundRenderer.MinecraftRenderer.renderMinecraft(
                masterState.textureData.getBackground(),
                matrices,
                light,
                backgroundOverlay,
                facing,
                borderType,
                zOffsetRenderLayer
        );

        // Render the border on top of the sign
        BorderRenderer.render(queue, matrices, borderType, light, facing);

        BlockPos blockPosBehind = new BlockPos(position).relative(masterState.blockState.getValue(CustomizableSignBlock.FACING).getOpposite(), 1);

        if (Minecraft.getInstance().level != null &&
                Minecraft.getInstance().level.getBlockEntity(blockPosBehind) instanceof SignPoleBlockEntity) {

            renderSignHolder(queue, masterState, matrices, light, facing);
        }

        matrices.popPose();
    }





    // Render the sign poles that hold the sign
    private void renderSignPoles(SubmitNodeCollector queue, CustomizableSignBlockRenderState state, PoseStack matrices, int light) {
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

            cachedPositions.forEach(pos -> renderSignPole(queue, state, bakedModelManager.getBlockModelShaper().getBlockModel(ModBlocks.SIGN_POLE_BLOCK.getBlock().defaultBlockState()), matrices, light, pos));
            return;
        }

        List<BlockPosExtended> polePositions;

        try {
            // Convert the string to a list of BlockPosExtended which represent the distance to the master position
            List<BlockPosExtended> distances = ListUtils.fromByteArray(Base64.getDecoder().decode(state.signPoleDistancesString)).stream().map(distance -> BlockPosExtended.INSTANCE.fromString((String) distance)).toList();

            // Add distance to master position to get the actual position of the sign pole
            polePositions = distances.stream()
                    .map(distance -> (new BlockPosExtended(state.blockPos)).addOffset(distance.invert()))
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
        BlockStateModel signPoleStateModel = bakedModelManager.getBlockModelShaper().getBlockModel(ModBlocks.SIGN_POLE_BLOCK.getBlock().defaultBlockState());

        // Render each sign pole
        polePositions.forEach(pos -> renderSignPole(queue, state, signPoleStateModel, matrices, light, pos));
    }




    // Render one sign pole
    private void renderSignPole(SubmitNodeCollector queue, CustomizableSignBlockRenderState state, BlockStateModel blockStateModel, PoseStack matrices, int light, BlockPos position) {
        // The position if the master block
        BlockPos masterPos = state.masterBlockPos;
        BlockPos offset = BlockPosExtended.getOffset(masterPos, position); // Offset of the sign. If the sign pole is one behind, the offset is (0, 0, -1) for example

        // Correct the offset to match the sign pole position
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1));

        matrices.pushPose();

        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Translate the sign pole to the correct position

        // Render sign pole
        queue.submitBlockModel(
                matrices,
                RenderTypes.cutoutMovingBlock(),
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }




    // Render the texture of the whole sign
    private void renderTexture(SubmitNodeCollector queue, CustomizableSignBlockRenderState state, PoseStack matrices, int light, Direction facing) {
        // If the block isn't a master block, exit function because there's nothing to render anyway since non-masters don't hold texture information
        if (!state.isMaster || !state.isInitialized) return;

        renderElements(queue, state, state.height, matrices, light, facing);
    }



    // Render the elements that were placed when the sign was edited
    private void renderElements(SubmitNodeCollector queue, CustomizableSignBlockRenderState state, int height, PoseStack matrices, int light, Direction facing) {
        if (state.clientElements.isEmpty()) return; // If there are no elements, exit

        List<ClientElementInterface> renderedElements = state.clientElements;

        for (int i = 0; i < renderedElements.size(); i++) {
            ClientElementInterface element = renderedElements.get(i);
            renderElement(queue, element, i, height, matrices, light, facing);
        }
    }

    public static void renderElement(SubmitNodeCollector queue, ClientElementInterface element, int index, int height, PoseStack matrices, int light, Direction facing) {
        element.renderMinecraft(queue, index, height, matrices, light, facing);
    }




    private void renderSignHolder(SubmitNodeCollector queue, CustomizableSignBlockRenderState state, PoseStack matrices, int light, Direction facing) {
        BlockStateModel blockStateModel = bakedModelManager.getBlockModelShaper().getBlockModel(ModBlocks.SIGN_HOLDER_BLOCK.getBlock().defaultBlockState());

        matrices.pushPose();

        BlockPos holderPos = state.blockPos.relative(facing, 1); // Position of the sign holder is one block in front of the sign
        matrices.translate(Vec3.atLowerCornerOf(BlockPosExtended.getOffset(state.blockPos, holderPos))); // Translate the sign holder to the correct position

        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
        matrices.translate(-0.5, -0.5, -0.5);

        queue.submitBlockModel(
                matrices,
                RenderTypes.cutoutMovingBlock(),
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }




    private void rotateSign(int rotationDegrees, PoseStack matrices) {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        matrices.translate(-0.5, -0.5, -0.5);
    }

}
