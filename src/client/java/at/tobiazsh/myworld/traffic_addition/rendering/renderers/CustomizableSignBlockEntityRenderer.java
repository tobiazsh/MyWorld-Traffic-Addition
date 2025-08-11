package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 09/09/2024 (DD/MM/YYYY) - 20:34
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ModBlocks;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.*;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface.zOffset;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class CustomizableSignBlockEntityRenderer implements BlockEntityRenderer<CustomizableSignBlockEntity> {

    public static final int DEFAULT_CALCULATION_CACHE_SIZE = 256; // Default size for the calculation cache, can be adjusted if needed

    private static final LRUCache<Pair<String, List<BlockPosExtended>>> CALCULATION_CACHE = new LRUCache<>(
            "CALCULATION_CACHE",
            Objects.requireNonNullElse(
                    ClientPreferences.gameplayPreference.getInt("calculationCacheSize"), // Get the size from the config
                    DEFAULT_CALCULATION_CACHE_SIZE
            )
    );

    private final BakedModelManager bakedModelManager;
    private int rotation = 0;

    public static float zOffsetRenderLayer = 3f;
    public static final float zOffsetRenderLayerDefault = 3f;

    public static float elementDistancingRenderLayer = 0.75f;
    public static final float elementDistancingRenderLayerDefault = 0.75f;

    private static final Map<CustomizableSignBlockEntity, List<ClientElementInterface>> elements = new HashMap<>();

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

    private List<BlockPosExtended> getSignDistances(CustomizableSignBlockEntity entity) {
        String signDistancesStringEncoded = entity.getSignDistancesString();
        List<BlockPosExtended> signDistances;

        if(signDistancesStringEncoded.isEmpty())
            return new ArrayList<>(); // If there are no signs, return an empty list

        // If already calculated, return the cached value
        if (CALCULATION_CACHE.anyMatch(match ->
                match.getLeft().equals(signDistancesStringEncoded) &&
                !match.getRight().isEmpty()
        )) {
            // If the sign distances are already calculated, return them from the cache
            return CALCULATION_CACHE.filter(match ->
                    match.getLeft().equals(signDistancesStringEncoded) &&
                    !match.getRight().isEmpty())
                    .getFirst().get().getRight();
        }

        if(signDistancesStringEncoded.isEmpty())
            return new ArrayList<>(); // If there are no signs, return an empty list

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
        CALCULATION_CACHE.access(new Pair<>(signDistancesStringEncoded, signDistances));

        return signDistances;
    }




    // Render the sign block
    @Override
    public void render(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {

        // If the block shouldn't render, exit function, for example when block isn't a master block
        if (!entity.isRendering()) return;

        // Get the BlockEntity of the master block
        assert MinecraftClient.getInstance().world != null;
        BlockEntity masterEntity = MinecraftClient.getInstance().world.getBlockEntity(entity.getMasterPos());

        // Just a check to avoid errors
        if (masterEntity instanceof CustomizableSignBlockEntity) {
            // Define the rotation depending on the facing state of the sign
            rotation = ((CustomizableSignBlockEntity) masterEntity).getRotation();
        }

        matrices.push();

        // Rotate the sign
        rotateSign(rotation, matrices);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());

        // Render master block sign block
        BlockStateModel csbeStateModel = bakedModelManager.getBlockModels().getModel(entity.getCachedState());

        BlockModelRenderer.render(
                matrices.peek(),
                consumer,
                csbeStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        Direction facingDirection = entity.getCachedState().get(CustomizableSignBlock.FACING);

        // Render the border for the master sign block
        BorderRenderer.render(matrices, vertexConsumers, entity.getBorderType(), light, overlay, facingDirection);
        renderTexture(entity, matrices, vertexConsumers, light, overlay, facingDirection);

        // If the entity is master, render the other signs attached to it
        if (entity.isMaster()) {
            renderSignPoles(entity, matrices, vertexConsumers, light, overlay);
            renderSigns(entity, csbeStateModel, matrices, vertexConsumers, light, overlay, facingDirection);
        }

        matrices.pop();
    }




    private void renderSigns(CustomizableSignBlockEntity entity, BlockStateModel blockStateModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        // Get the sign positions as a list of BlockPos
        List<BlockPosExtended> signDistances = getSignDistances(entity);
        List<BlockPosExtended> signPositions = calculatePosition(signDistances, new BlockPosExtended(entity.getPos()));

        // Render each sign
        for (int i = 0; i < signPositions.size(); i++) {
            if (Objects.requireNonNull(entity.getWorld()).getBlockEntity(signPositions.get(i)) instanceof CustomizableSignBlockEntity signBlockEntity)
                renderSign(
                        signBlockEntity,
                        blockStateModel,
                        matrices,
                        vertexConsumers,
                        light, overlay,
                        facing, signDistances.get(i).invert()
                );
        }
    }




    // Render one sign
    private void renderSign(CustomizableSignBlockEntity entity, BlockStateModel blockStateModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing, BlockPosExtended offset) {
        matrices.push();

        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Set the sign to the correct position

        // Render sign block
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        BlockModelRenderer.render(
                matrices.peek(),
                vertexConsumer,
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        // Render the border on top of the sign
        BorderRenderer.render(matrices, vertexConsumers, entity.getBorderType(), light, overlay, facing);

        BlockPosFloat blockPosBehind = new BlockPosFloat(entity.getPos())
                .offset(
                        entity.getCachedState().get(CustomizableSignBlock.FACING).getOpposite(),
                        1f
                );

        BlockEntity blockBehind = Objects.requireNonNull(entity.getWorld())
                .getBlockEntity(
                        new BlockPos(
                                (int) blockPosBehind.x,
                                (int) blockPosBehind.y,
                                (int) blockPosBehind.z
                        )
                );

        if (blockBehind instanceof SignPoleBlockEntity) {
            renderSignHolder(entity, matrices, vertexConsumers, light, overlay, facing);
        }

        matrices.pop();
    }




    // Render the sign poles that hold the sign
    private void renderSignPoles(CustomizableSignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // Get the position of each sign pole compacted in one string
        String signPolePositionsString = entity.getSignPoleDistancesString();
        if (signPolePositionsString.isEmpty()) return; // If there are no sign poles, exit function

        List<BlockPosExtended> polePositions;

        try {
            // Convert the string to a list of BlockPosExtended which represent the distance to the master position
            List<BlockPosExtended> distances = ListUtils.fromByteArray(Base64.getDecoder().decode(signPolePositionsString)).stream().map(distance -> BlockPosExtended.INSTANCE.fromString((String) distance)).toList();

            // Add distance to master position to get the actual position of the sign pole
            polePositions = distances.stream()
                    .map(distance -> (new BlockPosExtended(entity.getPos())).addOffset(distance.invert()))
                    .toList();

        } catch (IOException | ClassNotFoundException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to decode sign pole positions string: {}", signPolePositionsString, e);
            throw new RuntimeException("Failed to decode sign pole positions string", e);
        }

        // If there are no sign poles, don't do anything
        if(polePositions.isEmpty()) return;

        // Define the BakedModel for the sign poles
        BlockStateModel signPoleStateModel = bakedModelManager.getBlockModels().getModel(ModBlocks.SIGN_POLE_BLOCK.getBlock().getDefaultState());

        // Render each sign pole
        polePositions.forEach(pos -> renderSignPole(entity, signPoleStateModel, matrices, vertexConsumers, light, overlay, pos));
    }




    // Render one sign pole
    private void renderSignPole(CustomizableSignBlockEntity entity, BlockStateModel blockStateModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockPos position) {
        matrices.push();

        // The position if the master block
        BlockPos masterPos = entity.getMasterPos();
        BlockPos offset = BlockPosExtended.getOffset(masterPos, position); // Offset of the sign. If the sign pole is one behind, the offset is (0, 0, -1) for example

        // Correct the offset to match the sign pole position
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1));
        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Translate the sign pole to the correct position

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());

        // Render sign pole
        BlockModelRenderer.render(
                matrices.peek(),
                consumer, blockStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        matrices.pop();
    }




    // Render the texture of the sign
    private void renderTexture(CustomizableSignBlockEntity csbe, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        // If the block isn't a master block, exit function because there's nothing to render anyway since non-masters don't hold texture information
        if (!csbe.isMaster() || !csbe.isInitialized()) return;

        renderBackground(csbe, csbe.backgroundStylePieces, csbe.getHeight(), csbe.getWidth(), matrices, vertexConsumers, light, overlay, facing);
        renderElements(csbe, csbe.getHeight(), matrices, vertexConsumers, light, overlay, facing);
    }




    // Render the background texture of the sign
    private void renderBackground(CustomizableSignBlockEntity csbe, List<String> backgroundStylePieces, int height, int width, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        // If there's nothing to render, exit
        if (csbe.shouldUpdateBackgroundTexture()) {
            csbe.backgroundStylePieces = CustomizableSignData.getBackgroundTexturePathList(new CustomizableSignData().setJson(csbe.getSignTextureJson()), csbe).reversed();
            csbe.backgroundStylePieces.replaceAll(s -> s.replaceFirst("/assets/".concat(MyWorldTrafficAddition.MOD_ID).concat("/"), ""));
            csbe.setUpdateBackgroundTexture(false);
        }

        if (backgroundStylePieces.isEmpty()) return;

        // Coordinates of the master block
        BlockPos masterPos = csbe.getMasterPos();
        BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, zOffset);

        matrices.push();

        // Render from top to bottom and from left to right
        int currentListPos = 0;
        for (int i = height; i > 0; i--) {
            for (int j = width; j > 0; j--) {
                if (currentListPos >= backgroundStylePieces.size()) break; // Prevent out of bounds crashes

                matrices.push();

                BlockPos renderPos = masterPos.up(i - 1);
                renderPos = blockPosInDirection(getRightSideDirection(facing.getOpposite()), renderPos, j - 1);

                BlockPos offset = BlockPosExtended.getOffset(masterPos, renderPos);
                offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1)); // The position of the texture

                Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, backgroundStylePieces.get(currentListPos));

                CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffsetRenderLayer, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID, texture);
                RenderLayer renderLayer = imageLayering.buildRenderLayer();

                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

                matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Position the texture
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

                currentListPos++; // Move to the next texture

                matrices.pop();
            }
        }

        matrices.pop();
    }




    // Render the elements that were placed when the sign was edited
    private void renderElements(CustomizableSignBlockEntity csbe, int height, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        if (csbe.elements.isEmpty()) return; // If there are no elements, exit
        if (csbe.hasUpdateOccured()) {
            elements.put(csbe, csbe.elements.reversed().stream().map(ClientElementFactory::toClientElement).toList()); // Reverse so top most element gets rendered last
            csbe.setUpdateOccurred(false); // Reset the update flag
        }

        List<ClientElementInterface> renderedElements = elements.get(csbe);
        renderedElements.forEach(element -> renderElement(element, renderedElements.indexOf(element), height, matrices, vertexConsumers, light, overlay, facing));
    }

    public static void renderElement(ClientElementInterface element, int index, int height, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        element.renderMinecraft(index, height, matrices, vertexConsumers, light, overlay, facing);
    }




    private void renderSignHolder(CustomizableSignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        BlockStateModel blockStateModel = bakedModelManager.getBlockModels().getModel(ModBlocks.SIGN_HOLDER_BLOCK.getBlock().getDefaultState());

        matrices.push();

        BlockPos holderPos = entity.getPos().offset(facing, 1); // Position of the sign holder is one block in front of the sign
        matrices.translate(Vec3d.of(BlockPosExtended.getOffset(entity.getPos(), holderPos))); // Translate the sign holder to the correct position);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
        matrices.translate(-0.5, -0.5, -0.5);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        BlockModelRenderer.render(
                matrices.peek(),
                consumer,
                blockStateModel,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );

        matrices.pop();
    }




    private void rotateSign(int rotationDegrees, MatrixStack matrices) {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        matrices.translate(-0.5, -0.5, -0.5);
    }

}
