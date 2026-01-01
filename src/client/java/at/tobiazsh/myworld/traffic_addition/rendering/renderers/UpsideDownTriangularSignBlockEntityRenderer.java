package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 29/08/2024 (DD/MM/YYYY) - 21:33
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.UpsideDownTriangularSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class UpsideDownTriangularSignBlockEntityRenderer extends SignBlockEntityRenderer<UpsideDownTriangularSignBlockEntity> {

    public UpsideDownTriangularSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(Minecraft.getInstance().getModelManager());
    }

    @Override
    protected void renderTextureOnModel(String texturePath, PoseStack matrices, MultiBufferSource vertexConsumers, Direction facing, int light, int overlay) {
        Identifier TEXTURE = Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, texturePath);

        RenderType renderLayer = RenderTypes.entityCutout(TEXTURE);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.pushPose();
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0.57, 1, 0);
        matrices.translate(0, 0.05, 0);

        rotateTexture(facing, matrices);

        vertexConsumer.addVertex(matrices.last().pose(), -0.5f, -0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(0.0f, 1.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrices.last().pose(), 0.5f, -0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(1.0f, 1.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrices.last().pose(), 0.5f, 0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(1.0f, 0.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrices.last().pose(), -0.5f, 0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(0.0f, 0.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);

        matrices.popPose();
    }
}