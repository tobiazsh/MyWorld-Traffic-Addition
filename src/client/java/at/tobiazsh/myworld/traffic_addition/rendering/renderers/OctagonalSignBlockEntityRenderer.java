package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 30/08/2024 (DD/MM/YYYY) - 16:08
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.block_entities.OctagonalSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class OctagonalSignBlockEntityRenderer extends SignBlockEntityRenderer<OctagonalSignBlockEntity> {

    public OctagonalSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(MinecraftClient.getInstance().getBakedModelManager(), "octagonal_sign_block");
    }
}

// Just a backup before refactoring :)

/*private final BakedModelManager octagonalSignBlockModelManager;

private Direction direction = Direction.NORTH;
private BlockPos backCoords;
private Coordinates backstepCoords;
private Coordinates backsetCoords = new Coordinates(0, 0, -1, Direction.NORTH);

public OctagonalSignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    octagonalSignBlockModelManager = MinecraftClient.getInstance().getBakedModelManager();

}

@Override
public void render(OctagonalSignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
    direction = entity.getCachedState().get(OctagonalSignBlock.FACING);
    BakedModel octagonalSignBlockModel = octagonalSignBlockModelManager.getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "octagonal_sign_block"), ("facing=" + direction.getName())));

    backstepCoords = entity.getBackstepCoords();

    reassignValues(backstepCoords, entity);

    BlockEntity blockEntityBehind = MinecraftClient.getInstance().world.getBlockEntity(backCoords);

    SignBlock block = (SignBlock)entity.getCachedState().getBlock();
    block.setBackstepCoords(entity.getCachedState(), MinecraftClient.getInstance().world, entity.getPos());

    matrices.push();

    if(blockEntityBehind instanceof SignPoleBlockEntity signPoleBlockEntity) {
        int rotationDegrees = signPoleBlockEntity.getRotationValue() + 180;

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
    }

    VertexConsumer vertexConsumer = vertexConsumers.getBuffer(getSolid());
    MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(matrices.peek(), vertexConsumer, entity.getCachedState(), octagonalSignBlockModel, 1.0f, 1.0f, 1.0f, light, overlay);

    renderTextureOnModel(entity, matrices, vertexConsumers, light, overlay);

    matrices.pop();
}

private void reassignValues (Coordinates source, BlockEntity entity) {
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

private static void renderTextureOnModel (OctagonalSignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
    Identifier TEXTURE = Identifier.of(MyWorldTrafficAddition.MOD_ID, "textures/sign_pngs/aut/octagonal/stop.png");

    RenderLayer renderLayer = RenderLayer.getEntityCutout(TEXTURE);

    VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

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

public static void rotateTexture (OctagonalSignBlockEntity entity, MatrixStack matrices) {
    switch (entity.getCachedState().get(OctagonalSignBlock.FACING)) {

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
}*/
