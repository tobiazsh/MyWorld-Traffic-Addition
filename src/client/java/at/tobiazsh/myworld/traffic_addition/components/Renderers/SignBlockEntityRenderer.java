package at.tobiazsh.myworld.traffic_addition.components.Renderers;


/*
 * @created 03/09/2024 (DD/MM/YYYY) - 16:58
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.Blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SignBlockBackstepCoordsChange;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SignBlockRotationPayload;
import at.tobiazsh.myworld.traffic_addition.Utils.Coordinates;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

    /*
        IMPORTANT NOTICE!

        Before refactoring any of the renderers and replacing it with this one, PLEASE PLEASE PLEASE check if you have to re-do any method to render something. This may be fatal, I don't know the consequences yet.
        Nothing my happen, the texture may render wrong, the model may render wrong, your Computer may become an airplane but your computer may also begin to burn and explode!

        PROCEED WITH GREAT GREAT CAUTION!
     */

public class SignBlockEntityRenderer<T extends SignBlockEntity> implements BlockEntityRenderer<T> {

    private final BakedModelManager bakedModelMgr;
    private final String bakedModelIdentifier;
    private Coordinates backsetCoords;
    private BlockPos backCoords;
    public String textureIdentifier;
    private int rotationDegrees;
    public SignBlockEntityRenderer(BakedModelManager bakedModelMgr, String bakedModelIdentifier) {
        this.bakedModelMgr = bakedModelMgr;
        this.bakedModelIdentifier = bakedModelIdentifier;
    }
    private Direction direction;


    private boolean hasReloaded = false;

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        direction = entity.getCachedState().get(SignBlock.FACING);
        BakedModel signBlockModel = bakedModelMgr.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, bakedModelIdentifier), "facing=" + direction.getName()));

        SignBlock signBlock = (SignBlock) entity.getCachedState().getBlock();
        Coordinates backstepCoords = signBlock.setBackstepCoords(entity.getCachedState(), entity.getWorld(), entity.getPos());

        if(backstepCoords != entity.getBackstepCoords()) {
            ClientPlayNetworking.send(new SignBlockBackstepCoordsChange(entity.getPos(), backstepCoords.x, backstepCoords.y, backstepCoords.z, backstepCoords.direction));
        }

        reassignValues(backstepCoords, entity);

        BlockEntity blockEntityBehind = MinecraftClient.getInstance().world.getBlockEntity(backCoords);

        textureIdentifier = entity.getTextureId();

        matrices.push();

        if(blockEntityBehind instanceof SignPoleBlockEntity signPoleBlockEntity) {
            rotationDegrees = signPoleBlockEntity.getRotationValue() + 180;

            if (entity.getRotation() != rotationDegrees) {
                ClientPlayNetworking.send(new SignBlockRotationPayload(entity.getPos(), rotationDegrees));
            }

            matrices.translate(backsetCoords.x, backsetCoords.y, backsetCoords.z); // Place it in the correct position
            matrices.translate(0.5, 0, 0.5); // Set it back by half a block in each direction
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees)); // Rotate it to the desired degree
            matrices.translate(-0.5, 0, -0.5); // Return to original position

            // Now it's inside out

            matrices.translate(backsetCoords.x, backsetCoords.y, backsetCoords.z); // Set it back by another block
            matrices.translate(0.5, 0, 0.5); // Set it back by half a block in each direction
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180)); // Rotate it 180° to turn it the correct way
            matrices.translate(-0.5, 0, -0.5); // Set it back to original position
            // Do not set it back by -1 again. Since the model is right on the side of the next block, it does not need this behaviour.

            renderSignHolder(entity, matrices, vertexConsumers, light, overlay, direction);
        }

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(matrices.peek(), consumer, entity.getCachedState(), signBlockModel, 1.0f, 1.0f, 1.0f, light, overlay);

        renderTextureOnModel(entity, matrices, vertexConsumers, light, overlay);

        matrices.pop();
    }

    private void reassignValues(Coordinates source, SignBlockEntity entity) {
        switch (source.direction) {
            case EAST -> {
                backCoords = entity.getPos().west();
                backsetCoords = new Coordinates(-1, 0, 0, Direction.WEST);
            }

            case SOUTH -> {
                backCoords = entity.getPos().north();
                backsetCoords = new Coordinates(0, 0, -1, Direction.NORTH);
            }

            case WEST -> {
                backCoords = entity.getPos().east();
                backsetCoords = new Coordinates(1, 0, 0, Direction.EAST);
            }

            default -> {
                backCoords = entity.getPos().south();
                backsetCoords = new Coordinates(0, 0, 1, Direction.SOUTH);
            }
        }
    }

    protected void renderTextureOnModel(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        Identifier TEXTURE = Identifier.of(MyWorldTrafficAddition.MOD_ID, textureIdentifier);

        RenderLayer renderLayer = RenderLayer.getEntityCutout(TEXTURE);

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);

        matrices.push();
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0.57, 1, 0);

        rotateTexture(entity, matrices);

        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -0.5f, -0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.5f, -0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 1.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), 0.5f, 0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(1.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), -0.5f, 0.5f, 0.0f).color(1f, 1f, 1f, 1f).texture(0.0f, 0.0f).light(light).overlay(overlay).normal(0, 0, 1);

        matrices.pop();
    }

    public static void rotateTexture(SignBlockEntity entity, MatrixStack matrices) {
        switch (entity.getCachedState().get(SignBlock.FACING)) {

            case EAST -> {
                matrices.translate(0.5, 0.5, 0.5);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));

                matrices.translate(-0.5, -0.5, -0.5);
            }

            case WEST -> {
                matrices.translate(0.5, 0.5, 0.5);

                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90));

                matrices.translate(0.5, 0.5, 0.5);

                matrices.translate(0, -1, -1);
                matrices.translate(0, 0, 0.14);
            }

            case SOUTH -> {
                matrices.translate(0.43, 0, 0.57);
            }

            default -> {
                matrices.translate(0.5, 0.5, 0.5);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

                matrices.translate(-0.5, -0.5, -0.5);

                matrices.translate(0.57, 0, -0.43);
            }

        }
    }

    private static void rotateHolder(SignBlockEntity entity, MatrixStack matrices) {
        switch (entity.getCachedState().get(SignBlock.FACING)) {
            case SOUTH -> {
                matrices.translate(0, 0, 1);
            }

            default -> {
                matrices.translate(0, 0, -1);
            }

            case EAST -> {
                matrices.translate(1, 0, 0);
            }

            case WEST -> {
                matrices.translate(- 1, 0, 0);
            }
        }
    }

    private void renderSignHolder(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, Direction direction1) {
        BakedModel model = bakedModelMgr.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "sign_holder_block"), "facing=" + direction1.getName()));

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5, -0.5, -0.5);

        rotateHolder(entity, matrices);

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getSolid());
        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(matrices.peek(), vertexConsumer, entity.getCachedState(), model, 1.0f, 1.0f, 1.0f, light, overlay);

        matrices.pop();
    }

    public static int getFacingRotation(Direction FACING) {
        switch (FACING) {
            case SOUTH -> { return 180; }
            case WEST -> { return 90; }
            case EAST -> { return 270; }
            default -> { return 0; }
        }
    }
}
