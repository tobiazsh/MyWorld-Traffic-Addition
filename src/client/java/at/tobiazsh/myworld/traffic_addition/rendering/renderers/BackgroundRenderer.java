package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.texture.Sprite;
import at.tobiazsh.myworld.traffic_addition.texture.SpriteAtlas;
import at.tobiazsh.myworld.traffic_addition.texture.SpriteAtlasManager;
import at.tobiazsh.myworld.traffic_addition.utils.BackgroundTextureUtil;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.Color;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import static at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface.zOffset;

public class BackgroundRenderer {
    public static class MinecraftRenderer {
        public static void renderMinecraft(Background background, PoseStack matrices, int light, int backgroundOverlay, Direction facing, BorderProperty borders, float zOffsetRenderLayer) {
            if (background.isColor())
                renderColor(background, matrices, light, backgroundOverlay, facing, zOffsetRenderLayer);
            else
                renderTexture(background, matrices, light, backgroundOverlay, facing, borders, zOffsetRenderLayer);
        }

        private static void renderColor(Background background, PoseStack matrices, int light, int backgroundOverlay, Direction facing, float zOffsetRenderLayer) {
            if (!background.isColor()) return; // Do NOT render if it's not color
            CustomRenderLayer.ColorLayering backgroundLayer = new CustomRenderLayer.ColorLayering(zOffsetRenderLayer, CustomRenderLayer.ColorLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID);
            RenderType backgroundRenderLayer = backgroundLayer.buildRenderType();
            BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, zOffset);

            MultiBufferSource.BufferSource vertexConsumerProvider = Minecraft.getInstance().gameRenderer.renderBuffers.bufferSource(); // ClassTweaker aka. AccessWidener!
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(backgroundRenderLayer);

            matrices.pushPose();

            // Now render that
            // Position the vertices
            // Offest background so it's not directly in side the sign block
            matrices.translate(forwardShift.x, forwardShift.y, forwardShift.z);

            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
            matrices.translate(-0.5, -0.5, -0.5);

            Color color = background.color;

            if (color == null) return;

            float r = color.r();
            float b = color.b();
            float g = color.g();
            float a = color.a();

            vertexConsumer.addVertex(matrices.last().pose(), 0.0f, 0f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 1.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(matrices.last().pose(), 1f, 0f, 0.0f).setColor(r, g, b, a).setUv(1.0f, 1.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(matrices.last().pose(), 1f, 1f, 0.0f).setColor(r, g, b, a).setUv(1.0f, 0.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(matrices.last().pose(), 0.0f, 1f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);

            matrices.popPose();
        }

        private static void renderTexture(Background background, PoseStack matrices, int light, int backgroundOverlay, Direction facing, BorderProperty borders, float zOffsetRenderLayer) {
            if (background.isColor()) return; // Do NOT render if it's color
            if (background.texture == null) return;

            Identifier textureIdentifier = Identifier.tryParse(background.texture);
            SpriteAtlas spriteAtlas = SpriteAtlasManager.INSTANCE.getSpriteAtlas(textureIdentifier);
            Sprite bgSpr = spriteAtlas.getSprite(BackgroundTextureUtil.getBackgroundTextureIdentifier(spriteAtlas.getAtlasId(), borders));

            CustomRenderLayer.ImageLayering backgroundLayer = new CustomRenderLayer.ImageLayering(zOffsetRenderLayer, CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID, textureIdentifier);
            RenderType backgroundRenderLayer = backgroundLayer.buildRenderType();
            BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, zOffset);

            MultiBufferSource.BufferSource vertexConsumerProvider = Minecraft.getInstance().gameRenderer.renderBuffers.bufferSource(); // ClassTweaker aka. AccessWidener!
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(backgroundRenderLayer);

            matrices.pushPose();

            matrices.translate(forwardShift.x, forwardShift.y, forwardShift.z);

            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
            matrices.translate(-0.5, -0.5, -0.5);

            vertexConsumer.addVertex(matrices.last().pose(), 0.0f, 0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(bgSpr.u1, bgSpr.v1).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(matrices.last().pose(), 1f, 0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(bgSpr.u2, bgSpr.v1).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(matrices.last().pose(), 1f, 1f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(bgSpr.u2, bgSpr.v2).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            vertexConsumer.addVertex(matrices.last().pose(), 0.0f, 1f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(bgSpr.u1, bgSpr.v2).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);

            matrices.popPose();
        }
    }

    //public static void renderImGui(...) {
    //
    //}
}
