package at.tobiazsh.myworld.traffic_addition.rendering.renderers;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.exception.SpriteNotFoundException;
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
import com.mojang.math.Axis;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface.zOffset;

public class BackgroundRenderer {
    public static class MinecraftRenderer {
        public static void renderMinecraft(
                Background background,
                PoseStack matrices,
                @NonNull SubmitNodeCollector queue,
                int light,
                int backgroundOverlay,
                Direction facing,
                BorderProperty borders,
                float zOffsetRenderLayer
        ) {
            if (background.isColor())
                renderColor(background, matrices, queue, light, backgroundOverlay, facing, zOffsetRenderLayer);
            else
                renderTexture(background, matrices, queue, light, backgroundOverlay, facing, borders, zOffsetRenderLayer);
        }

        private static void renderColor(
                Background background,
                PoseStack poseStack,
                @NonNull SubmitNodeCollector queue,
                int light,
                int backgroundOverlay,
                Direction facing,
                float zOffsetRenderLayer
        ) {
            if (!background.isColor()) return; // Do NOT render if it's not color
            CustomRenderLayer.ColorLayering backgroundLayer = new CustomRenderLayer.ColorLayering(zOffsetRenderLayer);
            RenderType backgroundRenderLayer = backgroundLayer.buildRenderType();
            BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, zOffset);

            Color color = background.color;
            if (color == null) return;

            float r = color.r() / 255f;
            float g = color.g() / 255f;
            float b = color.b() / 255f;
            float a = color.a() / 255f;

            poseStack.pushPose();

            // Now render that
            // Position the vertices
            // Offest background so it's not directly in side the sign block
            poseStack.translate(forwardShift.x, forwardShift.y, forwardShift.z);

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
            poseStack.translate(-0.5, -0.5, -0.5);

            queue.submitCustomGeometry(poseStack, backgroundRenderLayer, (pose, vertexConsumer) -> {
                vertexConsumer.addVertex(pose, 0.0f, 0f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
                vertexConsumer.addVertex(pose, 1f, 0f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
                vertexConsumer.addVertex(pose, 1f, 1f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
                vertexConsumer.addVertex(pose, 0.0f, 1f, 0.0f).setColor(r, g, b, a).setUv(0.0f, 0.0f).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            });

            poseStack.popPose();
        }

        private static void renderTexture(
                Background background,
                PoseStack poseStack,
                @NonNull SubmitNodeCollector queue,
                int light,
                int backgroundOverlay,
                Direction facing,
                BorderProperty borders,
                float zOffsetRenderLayer
        ) {
            if (background.isColor()) return; // Do NOT render if it's color
            if (background.texture == null) return;

            Identifier atlasIdentifier = Identifier.parse(background.texture);
            SpriteAtlas spriteAtlas;
            Sprite bgSpr;

            try {
                spriteAtlas = SpriteAtlasManager.INSTANCE.getSpriteAtlas(atlasIdentifier);
                bgSpr = spriteAtlas.getSprite(BackgroundTextureUtil.getBackgroundTextureIdentifier(spriteAtlas.getAtlasId(), borders));

                if (bgSpr == null)
                    throw new SpriteNotFoundException(
                            "Sprite is with id "
                            + BackgroundTextureUtil.getBackgroundTextureIdentifier(spriteAtlas.getAtlasId(), borders)
                            + " isnull!"
                    );

            } catch (SpriteNotFoundException | NullPointerException e) { // Fallback if error during background getter
                renderColor(
                        Background.WHITE, // Least destructive background
                        poseStack,
                        queue,
                        light,
                        backgroundOverlay,
                        facing,
                        zOffsetRenderLayer
                );

                MyWorldTrafficAddition.LOGGER.debug("Failed rendering background in Minecraft! Fallback to color background!", e);

                return;
            }

            CustomRenderLayer.ImageLayering backgroundLayer = new CustomRenderLayer.ImageLayering(
                    zOffsetRenderLayer,
                    CustomRenderLayer.ImageLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID,
                    spriteAtlas.getTexture().getId()
            );

            RenderType backgroundRenderLayer = backgroundLayer.buildRenderType();
            BlockPosFloat forwardShift = new BlockPosFloat(0, 0, 0).offset(facing, zOffset);

            poseStack.pushPose();

            poseStack.translate(forwardShift.x, forwardShift.y, forwardShift.z);

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing.getOpposite())));
            poseStack.translate(-0.5, -0.5, -0.5);

            float uMargin = (bgSpr.u2 - bgSpr.u1) / 2048.0f;
            float vMargin = (bgSpr.v2 - bgSpr.v1) / 2048.0f;

            // Apply padding to get rid of tiny black lines
            float innerU1 = bgSpr.u1 + uMargin;
            float innerU2 = bgSpr.u2 - uMargin;
            float innerV1 = bgSpr.v1 + vMargin;
            float innerV2 = bgSpr.v2 - vMargin;

            queue.submitCustomGeometry(poseStack, backgroundRenderLayer, (pose, vertexConsumer) -> {
                vertexConsumer.addVertex(pose, 0.0f, 0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(innerU1, innerV2).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
                vertexConsumer.addVertex(pose, 1f, 0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(innerU2, innerV2).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
                vertexConsumer.addVertex(pose, 1f, 1f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(innerU2, innerV1).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
                vertexConsumer.addVertex(pose, 0.0f, 1f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(innerU1, innerV1).setLight(light).setOverlay(backgroundOverlay).setNormal(0, 0, 1);
            });

            poseStack.popPose();
        }
    }

    public static class ImGuiRenderer {
        /**
         * Renders the specified background in tiles using ImGui.image(...) and a {@link SpriteAtlas} in ImGui
         * @param background The background to render
         * @param borders A 2D Array of BorderProperty containing the borders of the sign in correct presentation...
         *                Syntax: BorderProperty[row][col]
         */
        public static void render(Background background, BorderProperty[][] borders, float pxOfBlock) {
            int tileSize = Math.round(pxOfBlock); // Round once so rendering and positioning always use the same integer value
            float currentY = ImGui.getCursorPosY() + (borders.length - 1) * tileSize; // Set to the position of bottom
            for (int row = borders.length - 1; row >= 0; row--) {
                ImGui.setCursorPosY(currentY);

                for (int col = 0; col < borders[row].length; col++) {
                    if (background.isColor())
                        renderTileColor(background, tileSize);
                    else
                        renderTileTexture(background, borders[row][col], tileSize);

                    if (col < borders[row].length - 1)
                        ImGui.sameLine();
                }

                currentY -= tileSize;
            }
        }

        private static void renderTileColor(Background background, int tileSize) {
            Color col = background.color;

            if (col == null) col = Background.WHITE.color; // Fallback to white
            if (col == null) return; // Make IDE happy (cannot happen - ever)

            // getCursorScreenPos() returns screen-space coords, which is what addRectFilled expects
            ImVec2 pMin = ImGui.getCursorScreenPos();
            ImGui.getWindowDrawList().addRectFilled(pMin, new ImVec2(pMin.x + tileSize, pMin.y + tileSize), Color.toImGuiColor(col));

            // Advance the cursor so the layout isn't broken
            ImGui.dummy(tileSize, tileSize);
        }

        private static void renderTileTexture(Background background, BorderProperty border, int tileSize) {
            if (background.texture == null) return;

            Identifier atlasIdentifier = Identifier.parse(background.texture);
            SpriteAtlas spriteAtlas;
            Sprite bgSpr;

            try {
                spriteAtlas = SpriteAtlasManager.INSTANCE.getSpriteAtlas(atlasIdentifier);
                bgSpr = spriteAtlas.getSprite(BackgroundTextureUtil.getBackgroundTextureIdentifier(spriteAtlas.getAtlasId(), border));

                if (bgSpr == null)
                    throw new SpriteNotFoundException(
                            "Sprite is with id "
                                    + BackgroundTextureUtil.getBackgroundTextureIdentifier(spriteAtlas.getAtlasId(), border)
                                    + " isnull!"
                    );

            } catch (SpriteNotFoundException | NullPointerException e) { // Fallback if error during background getter
                renderTileColor(Background.WHITE, tileSize); // Fallback to white background
                MyWorldTrafficAddition.LOGGER.debug("Failed rendering background in ImGui! Fallback to color background!", e);
                return;
            }

            ImGui.image(
                    spriteAtlas.getTexture().getTextureId(),
                    tileSize,
                    tileSize,
                    bgSpr.u1, bgSpr.v1,
                    bgSpr.u2, bgSpr.v2
            );
        }
    }
}
