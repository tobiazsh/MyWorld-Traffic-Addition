package at.tobiazsh.myworld.traffic_addition.Rendering.Renderers;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.SignPoleBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import static net.minecraft.client.render.RenderLayer.getSolid;

@Environment(EnvType.CLIENT)
public class SignPoleEntityRenderer implements BlockEntityRenderer<SignPoleBlockEntity> {

    private final BakedModel signPoleModel;

    public SignPoleEntityRenderer (BlockEntityRendererFactory.Context context) {
        signPoleModel = MinecraftClient.getInstance().getBakedModelManager().getModel(new ModelIdentifier(Identifier.of(MyWorldTrafficAddition.MOD_ID, "sign_pole_block"), ""));
    }

    @Override
    public void render(SignPoleBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if(!entity.isShouldRender()) return;

        int rotation_value = entity.getRotationValue();

        matrices.push();

        matrices.translate(.5, 0, .5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation_value));
        matrices.translate(-0.5f, 0, -0.5f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(getSolid());

        MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(matrices.peek(), vertexConsumer, entity.getCachedState(), signPoleModel, 1.0f, 1.0f, 1.0f, light, overlay);

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(SignPoleBlockEntity entity) {
        return true;
    }
}
