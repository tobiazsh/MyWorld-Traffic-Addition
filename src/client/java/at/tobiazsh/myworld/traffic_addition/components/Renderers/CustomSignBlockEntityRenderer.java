package at.tobiazsh.myworld.traffic_addition.components.Renderers;


/*
 * @created 09/09/2024 (DD/MM/YYYY) - 20:34
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.ImageElementClient;
import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.Blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.Utils.BlockPosExtended;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.components.Renderers.SignBlockEntityRenderer.getFacingRotation;

public class CustomSignBlockEntityRenderer implements BlockEntityRenderer<CustomizableSignBlockEntity> {

    private BakedModelManager bakedModelManager;
    private BlockRenderManager blockRenderManager;
    private Direction direction;
    private String borderModelPath;
    private int rotation = 0;
    private final MinecraftClient client = MinecraftClient.getInstance();

    public CustomSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        bakedModelManager = MinecraftClient.getInstance().getBakedModelManager();
        blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        borderModelPath = "block/customizable_sign_block_border_all"; // Standard border model
    }

    // Render the sign block
    @Override
    public void render(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        // If the block shouldn't render, exit function, for example when block isn't a master block
        if (!entity.isRendering()) return;

        // Get the direction the sign is facing
        direction = entity.getCachedState().get(CustomizableSignBlock.FACING);
        BakedModel baseModel = bakedModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block"), "facing=" + direction.getName())); // Define the BakedModel for the sign

        // Get the BlockEntity of the master block
        BlockEntity masterEntity = MinecraftClient.getInstance().world.getBlockEntity(entity.getMasterPos());

        // Just a check to avoid errors
        if(masterEntity instanceof CustomizableSignBlockEntity) {
            // Define the rotation depending on the facing state of the sign
            rotation = ((CustomizableSignBlockEntity) masterEntity).getRotation();
        }

        matrices.push();

        // Rotate the sign
        rotateSign(rotation, matrices);

        // Render the master block sign block
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), baseModel, 1.0f, 1.0f, 1.0f, light, overlay);

        // Render the border for the master sign block
        renderBorder(entity, tickDelta, matrices, vertexConsumers, light, overlay, entity.getCachedState().get(CustomizableSignBlock.FACING));
        renderTexture(entity, entity.getCachedState().get(CustomizableSignBlock.FACING), matrices, vertexConsumers, light, overlay);
        renderText(matrices, vertexConsumers, light, overlay);

        // If the entity is master, render the other signs attached to it
        if (entity.isMaster()) {
            renderSignPoles(entity, tickDelta, matrices, vertexConsumers, light, overlay);
            renderSigns(entity, tickDelta, matrices, vertexConsumers, light, overlay, direction);
        }

        matrices.pop();
    }

    private void renderSigns(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction FACING) {
        // Get the positions of each sign compacted in one giant string
        String constructedSignPositions = entity.getSignPositions();

        // If there are no sign blocks to render, do nothing
        if(constructedSignPositions.isEmpty()) return;

        // Get the sign positions as a list of BlockPos
        List<BlockPos> signPositions = CustomizableSignBlockEntity.deconstructBlockPosListString(constructedSignPositions);
        BakedModel signModel = bakedModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block"), "facing=" + FACING.getName())); // Define the BakedModel for the sign block

        // Render each sign
        for (BlockPos sign : signPositions) {
            if (entity.getWorld().getBlockEntity(sign) instanceof CustomizableSignBlockEntity signBlockEntity) renderSign(signBlockEntity, tickDelta, matrices, vertexConsumers, light, overlay, FACING, signModel);
        }
    }

    // Render one sign
    private void renderSign(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction FACING, BakedModel model) {
        matrices.push();

        // Position of the master block
        BlockPos masterPos = entity.getMasterPos();
        BlockPos offset = BlockPosExtended.getOffset(masterPos, entity.getPos()); // Offset of the sign.
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1)); // Offset correction relative to the sign
        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Set the sign to the correct position

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), model, 1.0f, 1.0f, 1.0f, light, overlay); // Render sign block

        // Render the border on top of the sign
        renderBorder(entity, tickDelta, matrices, vertexConsumers, light, overlay, FACING);

        matrices.pop();
    }

    // Render the border of the sign
    private void renderBorder(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction FACING) {
        matrices.push();

        // Define base border model path
        borderModelPath = "block/" + entity.getBorderType();

        // If the border model path has no texture and shouldn't render, exit function
        if (Objects.equals(borderModelPath, "block/customizable_sign_block_border_none")) {
            matrices.pop();
            return;
        }

        // Define the BakedModel for the border
        BakedModel model = bakedModelManager.getModel(Identifier.of(MyWorldTrafficAddition.MOD_ID, borderModelPath));

        // Rotate the border appropriately to match the direction the sign is facing with the pivot point in the center
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getFacingRotation(FACING)));
        matrices.translate(-0.5, -0.5, -0.5);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), model, 1.0f, 1.0f, 1.0f, light, overlay); // Rendering occurs here

        matrices.pop();
    }

    // Render the sign poles that hold the sign
    private void renderSignPoles(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // Get the position of each sign pole compacted in one string
        String signPolePositionsString = entity.getSignPolePositions();

        // If there are no sign poles, don't do anything
        if(signPolePositionsString.isEmpty()) return;

        // Define the BakedModel for the sign poles
        BakedModel signPoleModel = bakedModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "sign_pole_block"), ""));
        List<BlockPos> positions = CustomizableSignBlockEntity.deconstructBlockPosListString(signPolePositionsString); // Deconstruct the string into a list of BlockPos

        // Render each sign pole
        positions.forEach(pos -> renderSignPole(entity, tickDelta, matrices, vertexConsumers, light, overlay, pos, signPoleModel));
    }

    // Render one sign pole
    private void renderSignPole(CustomizableSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockPos position, BakedModel signPoleModel) {
        matrices.push();

        // The position if the master block
        BlockPos masterPos = entity.getMasterPos();
        BlockPos offset = BlockPosExtended.getOffset(masterPos, position); // Offset of the sign. If the sign pole is one behind, the offset is (0, 0, -1) for example

        // Correct the offset to match the sign pole position
        offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1));
        matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Translate the sign pole to the correct position

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
        blockRenderManager.getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), signPoleModel, 1.0f, 1.0f, 1.0f, light, overlay); // Render sign pole

        matrices.pop();
    }

    // Render the custom texture that is being created with the MyWorld Traffic Addition Sign Editor
    private void renderTexture(CustomizableSignBlockEntity csbe, Direction facing, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // If the block isn't a master block, exit function because there's nothing to render anyway since non-masters don't hold texture information
        if (!csbe.isMaster() || !csbe.isInitialized()) return;

        renderTextureBackground(csbe, csbe.backgroundStylePieces, csbe.getHeight(), csbe.getWidth(), matrices, vertexConsumers, light, overlay, facing);
        renderElements(csbe, csbe.getHeight(), matrices, vertexConsumers, light, overlay, facing);
    }

    // Render the background texture of the sign
    private void renderTextureBackground(CustomizableSignBlockEntity csbe, List<String> backgroundStylePieces, int height, int width, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        // If there's nothing to render, exit
        if (backgroundStylePieces.isEmpty()) return;

        // Coordinates of the master block
        BlockPos masterPos = csbe.getMasterPos();

        BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, 0.075f);

        matrices.push();

        // Render from top to bottom and from left to right
        int currentListPos = 0;
        for (int i = height; i > 0; i--) {
            for (int j = width; j > 0; j--) {
                if (currentListPos >= backgroundStylePieces.size()) break; // Prevent out of bounds crashes

                matrices.push();

                BlockPos renderPos = masterPos.up(i - 1);
                renderPos = CustomizableSignBlockEntity.getBlockPosAtDirection(CustomizableSignBlockEntity.getRightSideDirection(facing.getOpposite()), renderPos, j - 1);

                BlockPos offset = BlockPosExtended.getOffset(masterPos, renderPos);
                offset = new BlockPos(offset.getX() * (-1), offset.getY() * (-1), offset.getZ() * (-1)); // The position of the texture

                Identifier texture = Identifier.of(MyWorldTrafficAddition.MOD_ID, backgroundStylePieces.get(currentListPos));
                RenderLayer renderLayer = RenderLayer.getEntityCutout(texture);
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

                matrices.translate(offset.getX(), offset.getY(), offset.getZ()); // Position the texture
                matrices.translate(forwardShift.x, forwardShift.y, forwardShift.z); // Forward shift so it's visible and not rendered inside the other textures

                // Turn to match the facing direction
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getFacingRotation(facing.getOpposite())));
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
        List<BaseElement> elements = csbe.elements.reversed(); // Reverse so top most element gets rendered last

        elements.forEach(element -> {
            if (element instanceof ImageElement) {
                ImageElementClient.fromImageElement((ImageElement) element).renderMinecraft(element, elements.indexOf(element), height, matrices, vertexConsumers, light, overlay, facing);
            } else if (element instanceof TextElement) {
                TextElementClient.fromTextElement((TextElement) element).renderMinecraft(element, elements.indexOf(element), height, matrices, vertexConsumers, light, overlay, facing);
            }
        });
    }

    // Render the sign holders that hold the sign up
    private void renderSignHolders() {
        // Code here ...
    }

    private String textToRender = "Hello, Fabric!";

    // Test Rendering Text
    private void renderText(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        //CustomFont.renderer.draw(textToRender, 0, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

        matrices.pop();
    }


    private void rotateSign(int rotationDegrees, MatrixStack matrices) {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        matrices.translate(-0.5, -0.5, -0.5);
    }
}
