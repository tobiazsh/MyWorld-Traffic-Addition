package at.tobiazsh.myworld.traffic_addition.rendering;

import at.tobiazsh.myworld.traffic_addition.cache.LRUCache;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.client.render.RenderLayer;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences.gameplayPreference;
import static net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;

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
        public static LayeringTransform getZLayeringBackward(float zOffset) {
            return new LayeringTransform("view_offset_z_layering_backward", matrices -> RenderSystem.getProjectionType().apply(matrices, zOffset));
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
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                            .texture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .layeringTransform(Layering.getZLayeringBackward(zOffset))
                            .build();

                    return RenderLayer.of("entity_solid_z_offset_backward", renderSetup);
                }
        );

        private final Function<Identifier, RenderLayer> ENTITY_CUTOUT_Z_OFFSET_BACKWARD = Util.memoize(
                texture -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
                            .texture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .layeringTransform(Layering.getZLayeringBackward(zOffset))
                            .build();

                    return RenderLayer.of("entity_solid_z_offset_backward", renderSetup);
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
                texture -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.RENDERTYPE_TEXT_INTENSITY)
                            .texture("Sampler0", texture)
                            .useLightmap()
                            .layeringTransform(Layering.getZLayeringBackward(zOffset))
                            .build();

                    return RenderLayer.of("text_z_offset_backward_intensity", renderSetup);
                }
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
                zOff -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
                            .useLightmap()
                            .texture("Sampler0", BLOCK_ATLAS_TEXTURE) // Deprecated; might change in the future. Still using because Minecraft also uses it on it's "model renderers"
                            .layeringTransform(Layering.getZLayeringBackward(zOff))
                            .build();

                    return RenderLayer.of("cutout_z_offset_backward", renderSetup);
                }
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