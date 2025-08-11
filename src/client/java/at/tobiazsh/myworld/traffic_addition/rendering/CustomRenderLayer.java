package at.tobiazsh.myworld.traffic_addition.rendering;

import at.tobiazsh.myworld.traffic_addition.utils.LRUCache;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.client.render.RenderLayer;
import org.joml.Matrix4fStack;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.utils.ClientPreferences.gameplayPreference;
import static net.minecraft.client.render.RenderPhase.*;

/**
 * Custom RenderLayer exclusively for this mod to prevent z-fighting when viewing signs from further away. Pairs with CustomTextRenderer.
 */
public class CustomRenderLayer {

    public static final int DEFAULT_IMAGE_CACHE_SIZE = 200;
    public static final int DEFAULT_TEXT_CACHE_SIZE = 100;

    public static final LRUCache<TextLayering> BUILT_TEXT_LAYERING = new LRUCache<>(
        "BUILT_TEXT_LAYERING",
        Objects.requireNonNullElse(
            gameplayPreference.getInt("textRenderLayerCacheSize"),
                DEFAULT_TEXT_CACHE_SIZE
        )
    ); // Stores all the built text render layers of all fonts

    public static final LRUCache<ImageLayering> BUILT_IMAGE_LAYERING = new LRUCache<>(
        "BUILT_IMAGE_LAYERING",
        Objects.requireNonNullElse(
                gameplayPreference.getInt("imageRenderLayerCacheSize"),
                DEFAULT_IMAGE_CACHE_SIZE
        )
    ); // Stores all the built image render layers of all textures

    // ------------------ GENERAL Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static class Layering {
        public static RenderPhase.Layering getRenderPhaseZLayeringBackward(float zOffset) {
            return new RenderPhase.Layering("view_offset_z_layering_backward", () -> {
                Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
                matrix4fStack.pushMatrix();
                RenderSystem.getProjectionType().apply(matrix4fStack, zOffset);
            },
                    () -> {
                        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
                        matrix4fStack.popMatrix();
                    }
            );
        }
    }

    // ------------------ Image Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    public static class ImageLayering {
        
        private float zOffset;
        private RenderLayer renderLayer;
        private final ImageLayering.LayeringType layeringType;
        private final Identifier texture;

        /**
         * Constructor for ImageLayering
         * @param zOffset The elevation on the z-axis. 1.0f = 128 Blocks | 0.128f = 1 Block
         * @param layeringType The type of layering (solid, cutout, etc.)
         * @param texture The texture id
         */
        public ImageLayering(float zOffset, LayeringType layeringType, Identifier texture) {
            this.zOffset = zOffset;
            this.layeringType = layeringType;
            this.texture = texture;
        }

        private final Function<Identifier, RenderLayer> ENTITY_SOLID_Z_OFFSET_BACKWARD = Util.memoize(
                texture -> {
                    RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                            .texture(new RenderPhase.Texture(texture, false))
                            .lightmap(ENABLE_LIGHTMAP)
                            .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                            .layering(CustomRenderLayer.Layering.getRenderPhaseZLayeringBackward(zOffset))
                            .build(true);

                    return RenderLayer.of(
                            "entity_solid_z_offset_backward",
                            1536,
                            true,
                            false,
                            RenderPipelines.ENTITY_SOLID,
                            multiPhaseParameters
                    );
                }
        );

        private final Function<Identifier, RenderLayer> ENTITY_CUTOUT_Z_OFFSET_BACKWARD = Util.memoize(
                texture -> {
                    RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                            .texture(new RenderPhase.Texture(texture, false))
                            .lightmap(ENABLE_LIGHTMAP)
                            .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                            .layering(CustomRenderLayer.Layering.getRenderPhaseZLayeringBackward(zOffset))
                            .build(true);

                    return RenderLayer.of(
                            "entity_cutout_z_offset_backward",
                            1536,
                            true,
                            false,
                            RenderPipelines.ENTITY_CUTOUT,
                            multiPhaseParameters
                    );
                }
        );
        
        public RenderLayer buildRenderLayer() {

                // If cached, return the cached render layer
                if (layerExistsInCache(this.texture, this.zOffset, this.layeringType))
                    return Objects.requireNonNull(getLayerFromCache(this.texture, this.zOffset, this.layeringType)).getRenderLayer();

                this.renderLayer = switch (this.layeringType) {
                    case VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID -> ENTITY_SOLID_Z_OFFSET_BACKWARD.apply(this.texture);
                    case VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT -> ENTITY_CUTOUT_Z_OFFSET_BACKWARD.apply(this.texture);
                };

                cacheLayer(this);

                return this.renderLayer;
        }

        public RenderLayer getRenderLayer() {
            return renderLayer;
        }
        
        public enum LayeringType {
            VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID,
            VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT
        }

        // CACHE STUFF --------------------

        private static ImageLayering getLayerFromCache(Identifier id, float zOffset, ImageLayering.LayeringType layeringType) {
            List<LRUCache.CacheItem<ImageLayering>> matchingItems = BUILT_IMAGE_LAYERING.filter(item -> item.texture.equals(id) && item.zOffset == zOffset && item.layeringType == layeringType);
            LRUCache.CacheItem<ImageLayering> firstItem = matchingItems.stream().findFirst().orElse(null);

            if (firstItem == null) return null;

            ImageLayering imageLayering = firstItem.get();
            BUILT_IMAGE_LAYERING.access(imageLayering);
            return imageLayering;
        }

        private static boolean layerExistsInCache(Identifier id, float zOffset, ImageLayering.LayeringType layeringType) {
            return BUILT_IMAGE_LAYERING.anyMatch(imageLayering -> imageLayering.texture.equals(id) && imageLayering.zOffset == zOffset && imageLayering.layeringType == layeringType);
        }

        private static void cacheLayer(ImageLayering ImageLayering) {
            BUILT_IMAGE_LAYERING.access(ImageLayering);
        }
    }

    // ------------------ Text Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static class TextLayering {

        private float zOffset;
        private RenderLayer renderLayer;
        private final LayeringType layeringType;
        private final Identifier texture;

        /**
         * Constructor for TextLayering
         * @param zOffset The elevation on the z-axis. 1.0f ≈ 128 Blocks
         * @param layeringType The type of layering
         * @param texture The texture id
         */
        public TextLayering(float zOffset, LayeringType layeringType, Identifier texture) {
            this.zOffset = zOffset;
            this.layeringType = layeringType;
            this.texture = texture;
        }

        private final Function<Identifier, RenderLayer> TEXT_Z_OFFSET_BACKWARD_INTENSITY = Util.memoize(
                texture -> RenderLayer.of(
                        "text_z_offset_backward_intensity",
                        786432,
                        false,
                        false,
                        CustomRenderPipelines.RENDERTYPE_CUSTOM_TEXT_INTENSITY,
                        RenderLayer.MultiPhaseParameters.builder()
                                .texture(new RenderPhase.Texture(texture, false))
                                .lightmap(ENABLE_LIGHTMAP)
                                .layering(CustomRenderLayer.Layering.getRenderPhaseZLayeringBackward(zOffset))
                                .build(false)
                )
        );

        public RenderLayer buildRenderLayer() {

            // If cached, return the cached render layer
            if (layerExistsInCache(this.texture, this.zOffset, this.layeringType))
                return Objects.requireNonNull(getLayerFromCache(this.texture, this.zOffset, this.layeringType)).getRenderLayer();

            this.renderLayer = switch (this.layeringType) {
                case VIEW_OFFSET_Z_LAYERING_BACKWARD_INTENSITY -> TEXT_Z_OFFSET_BACKWARD_INTENSITY.apply(this.texture);
            };

            cacheLayer(this);

            return this.renderLayer;
        }

        public RenderLayer getRenderLayer() {
            return renderLayer;
        }

        public enum LayeringType {
            VIEW_OFFSET_Z_LAYERING_BACKWARD_INTENSITY
        }

        // CACHE STUFF --------------------

        private static TextLayering getLayerFromCache(Identifier id, float zOffset, TextLayering.LayeringType layeringType) {
            List<LRUCache.CacheItem<TextLayering>> matchingItems = BUILT_TEXT_LAYERING.filter(item -> item.texture.equals(id) && item.zOffset == zOffset && item.layeringType == layeringType);
            LRUCache.CacheItem<TextLayering> firstItem = matchingItems.stream().findFirst().orElse(null);

            if (firstItem == null) return null;

            TextLayering textLayering = firstItem.get();
            BUILT_TEXT_LAYERING.access(textLayering);
            return textLayering;
        }

        private static boolean layerExistsInCache(Identifier id, float zOffset, TextLayering.LayeringType layeringType) {
            return BUILT_TEXT_LAYERING.anyMatch(textLayering -> textLayering.texture.equals(id) && textLayering.zOffset == zOffset && textLayering.layeringType == layeringType);
        }

        private static void cacheLayer(TextLayering textLayering) {
            BUILT_TEXT_LAYERING.access(textLayering);
        }

    }

    // ------------------ Model Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static class ModelLayering {

        private final float zOffset;
        private final LayeringType layeringType;

        public ModelLayering(float zOffset, ModelLayering.LayeringType layeringType) {
            this.zOffset = zOffset;
            this.layeringType = layeringType;
        }

        private final Function<Float, RenderLayer> CUTOUT_Z_OFFSET_BACKWARD = Util.memoize(
                zOff -> RenderLayer.of(
                        "cutout_z_offset_backward",
                        786432,
                        true,
                        false,
                        RenderPipelines.CUTOUT,
                        RenderLayer.MultiPhaseParameters.builder()
                                .lightmap(ENABLE_LIGHTMAP)
                                .layering(Layering.getRenderPhaseZLayeringBackward(zOff))
                                .texture(BLOCK_ATLAS_TEXTURE)
                                .build(true)
                )
        );

        public RenderLayer buildRenderLayer() {
            return switch (this.layeringType) {
                case CUTOUT_Z_OFFSET_BACKWARD -> CUTOUT_Z_OFFSET_BACKWARD.apply(this.zOffset);
            };
        }

        public enum LayeringType {
            CUTOUT_Z_OFFSET_BACKWARD
        }
    }
}