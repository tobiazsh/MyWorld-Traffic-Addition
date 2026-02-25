package at.tobiazsh.myworld.traffic_addition.rendering;

import at.tobiazsh.myworld.traffic_addition.cache.LRUCache;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences.gameplayPreference;
import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

/**
 * Custom RenderLayer exclusively for this mod to prevent z-fighting when viewing signs from further away. Pairs with CustomTextRenderer.
 */
public class CustomRenderLayer {

    public static final int DEFAULT_IMAGE_CACHE_SIZE = 200;
    public static final int DEFAULT_TEXT_CACHE_SIZE = 100;

    public static final String TEXTURE_NAME = "Sampler0";

    // ------------------ GENERAL Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    abstract public static class Layering {

        protected Layering(float zOffset) {
            this.zOffset = zOffset;
        }

        protected final float zOffset;
        protected RenderType renderType;

        abstract public RenderType buildRenderType();
        abstract public RenderType getRenderType();

        public static LayeringTransform getZLayeringBackward(float zOffset) {
            return new LayeringTransform("view_offset_z_layering_backward", matrices -> RenderSystem.getProjectionType().applyLayeringTransform(matrices, zOffset));
        }
    }

    // ------------------ Image Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    public static class ImageLayering extends Layering {
        private final ImageLayering.LayeringType layeringType;
        private final Identifier texture;

        private static final LRUCache<ImageLayering> BUILT_IMAGE_LAYERING = new LRUCache<>(
                "BUILT_IMAGE_LAYERING",
                Objects.requireNonNullElse(
                        gameplayPreference.getInt("imageRenderLayerCacheSize"),
                        DEFAULT_IMAGE_CACHE_SIZE
                )
        ); // Stores all the built image render layers of all textures

        /**
         * Constructor for ImageLayering
         * @param zOffset The elevation on the z-axis. 1.0f = 128 Blocks | 0.128f = 1 Block
         * @param layeringType The type of layering (solid, cutout, etc.)
         * @param texture The texture id
         */
        public ImageLayering(float zOffset, LayeringType layeringType, Identifier texture) {
            super(zOffset);
            this.layeringType = layeringType;
            this.texture = texture;
        }

        private final Function<Identifier, RenderType> ENTITY_SOLID_Z_OFFSET_BACKWARD = Util.memoize(
                texture -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                            .withTexture(TEXTURE_NAME, texture)
                            .useLightmap()
                            .useOverlay()
                            .setLayeringTransform(Layering.getZLayeringBackward(zOffset))
                            .createRenderSetup();

                    return RenderType.create("entity_solid_z_offset_backward", renderSetup);
                }
        );

        private final Function<Identifier, RenderType> ENTITY_CUTOUT_Z_OFFSET_BACKWARD = Util.memoize(
                texture -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
                            .withTexture(TEXTURE_NAME, texture)
                            .useLightmap()
                            .useOverlay()
                            .setLayeringTransform(Layering.getZLayeringBackward(zOffset))
                            .createRenderSetup();

                    return RenderType.create("entity_solid_z_offset_backward", renderSetup);
                }
        );
        
        public RenderType buildRenderType() {

                // If cached, return the cached render layer
                if (layerExistsInCache(this.texture, this.zOffset, this.layeringType))
                    return Objects.requireNonNull(getLayerFromCache(this.texture, this.zOffset, this.layeringType)).getRenderType();

                this.renderType = switch (this.layeringType) {
                    case VIEW_OFFSET_Z_LAYERING_BACKWARD_SOLID -> ENTITY_SOLID_Z_OFFSET_BACKWARD.apply(this.texture);
                    case VIEW_OFFSET_Z_LAYERING_BACKWARD_CUTOUT -> ENTITY_CUTOUT_Z_OFFSET_BACKWARD.apply(this.texture);
                };

                cacheLayer(this);

                return this.renderType;
        }

        public RenderType getRenderType() {
            return renderType;
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

    public static class TextLayering extends Layering {

        private static final LRUCache<TextLayering> BUILT_TEXT_LAYERING = new LRUCache<>(
                "BUILT_TEXT_LAYERING",
                Objects.requireNonNullElse(
                        gameplayPreference.getInt("textRenderLayerCacheSize"),
                        DEFAULT_TEXT_CACHE_SIZE
                )
        ); // Stores all the built text render layers of all fonts


        private final LayeringType layeringType;
        private final Identifier texture;

        /**
         * Constructor for TextLayering
         * @param zOffset The elevation on the z-axis. 1.0f ≈ 128 Blocks
         * @param layeringType The type of layering
         * @param texture The texture id
         */
        public TextLayering(float zOffset, LayeringType layeringType, Identifier texture) {
            super(zOffset);
            this.layeringType = layeringType;
            this.texture = texture;
        }

        private final Function<Identifier, RenderType> TEXT_Z_OFFSET_BACKWARD_INTENSITY = Util.memoize(
                texture -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.TEXT_INTENSITY)
                            .withTexture(TEXTURE_NAME, texture)
                            .useLightmap()
                            .setLayeringTransform(Layering.getZLayeringBackward(zOffset))
                            .createRenderSetup();

                    return RenderType.create("text_z_offset_backward_intensity", renderSetup);
                }
        );

        @Override
        public RenderType buildRenderType() {

            // If cached, return the cached render layer
            if (layerExistsInCache(this.texture, this.zOffset, this.layeringType))
                return Objects.requireNonNull(getLayerFromCache(this.texture, this.zOffset, this.layeringType)).getRenderType();

            this.renderType = switch (this.layeringType) {
                case VIEW_OFFSET_Z_LAYERING_BACKWARD_INTENSITY -> TEXT_Z_OFFSET_BACKWARD_INTENSITY.apply(this.texture);
            };

            cacheLayer(this);

            return this.renderType;
        }

        @Override
        public RenderType getRenderType() {
            return renderType;
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

    public static class ModelLayering extends Layering {

        private final LayeringType layeringType;

        public ModelLayering(float zOffset, ModelLayering.LayeringType layeringType) {
            super(zOffset);
            this.layeringType = layeringType;
        }

        private final Function<Float, RenderType> CUTOUT_Z_OFFSET_BACKWARD = Util.memoize(
                zOff -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                            .useLightmap()
                            .withTexture(TEXTURE_NAME, LOCATION_BLOCKS) // Deprecated; might change in the future. Still using because Minecraft also uses it on its "model renderers"
                            .setLayeringTransform(Layering.getZLayeringBackward(zOff))
                            .createRenderSetup();

                    return RenderType.create("cutout_z_offset_backward", renderSetup);
                }
        );

        @Override
        public RenderType buildRenderType() {
            return switch (this.layeringType) {
                case CUTOUT_Z_OFFSET_BACKWARD -> CUTOUT_Z_OFFSET_BACKWARD.apply(this.zOffset);
            };
        }

        @Override
        public RenderType getRenderType() {
            return null;
        }

        public enum LayeringType {
            CUTOUT_Z_OFFSET_BACKWARD
        }
    }

    // ------------------ Color Layering -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static class ColorLayering extends Layering {

        /**
         * Constructor for ColorLayering
         * @param zOffset The elevation on the z-axis. 1.0f = 128 Blocks | 0.128f = 1 Block
         */
        public ColorLayering(float zOffset) {
            super(zOffset);
        }

        // ENTITY_TRANSLUCENT with blocks atlas bound:
        //  - Fixes ghost texture: ENTITY_SOLID/CUTOUT have no .withTexture() → GPU reuses whatever was last bound
        //  - Fixes alpha: ENTITY_SOLID is opaque; ENTITY_TRANSLUCENT has alpha blending enabled
        //  - UV is driven to (0,0) per-vertex so the white corner of the atlas is sampled; vertex color drives the actual tint
        private final Function<Float, RenderType> ENTITY_TRANSLUCENT_Z_OFFSET_BACKWARD = Util.memoize(
                zOff -> {
                    RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                            .withTexture(TEXTURE_NAME, LOCATION_BLOCKS)
                            .useLightmap()
                            .useOverlay()
                            .setLayeringTransform(Layering.getZLayeringBackward(zOff))
                            .createRenderSetup();

                    return RenderType.create("entity_translucent_z_offset_backward_color", renderSetup);
                }
        );

        @Override
        public RenderType buildRenderType() {
            this.renderType = ENTITY_TRANSLUCENT_Z_OFFSET_BACKWARD.apply(this.zOffset);
            return this.renderType;
        }

        @Override
        public RenderType getRenderType() {
            return renderType;
        }
    }

}