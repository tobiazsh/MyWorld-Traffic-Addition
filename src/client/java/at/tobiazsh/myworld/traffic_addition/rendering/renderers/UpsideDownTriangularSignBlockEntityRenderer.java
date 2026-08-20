package at.tobiazsh.myworld.traffic_addition.rendering.renderers;


/*
 * @created 29/08/2024 (DD/MM/YYYY) - 21:33
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.block_entities.UpsideDownTriangularSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class UpsideDownTriangularSignBlockEntityRenderer extends SignBlockEntityRenderer<UpsideDownTriangularSignBlockEntity> {

    public UpsideDownTriangularSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(Minecraft.getInstance().getModelManager());
    }

    @Override
    protected void renderTextureOnModel(
            String texturePath,
            PoseStack poseStack,
            @NonNull SubmitNodeCollector queue,
            Direction facing,
            int light,
            int overlay
    ) {
        Identifier texture = Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, texturePath);

        float zOffset = MyWorldTrafficAdditionClient.getClientPreferences().signs.viewDistance.getOrDefault();
        CustomRenderLayer.ImageLayering imageLayering = new CustomRenderLayer.ImageLayering(zOffset, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT, texture);
        RenderType renderLayer = imageLayering.buildRenderType();

        poseStack.pushPose();
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(0.57, 1, 0);
        poseStack.translate(0, 0.05, 0);

        rotateTexture(facing, poseStack);

        queue.submitCustomGeometry(poseStack, renderLayer, (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, -0.5f, -0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(0.0f, 1.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(pose, 0.5f, -0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(1.0f, 1.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(pose, 0.5f, 0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(1.0f, 0.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(pose, -0.5f, 0.5f, 0.0f).setColor(1f, 1f, 1f, 1f).setUv(0.0f, 0.0f).setLight(light).setOverlay(overlay).setNormal(0, 0, 1);
        });

        poseStack.popPose();
    }
}