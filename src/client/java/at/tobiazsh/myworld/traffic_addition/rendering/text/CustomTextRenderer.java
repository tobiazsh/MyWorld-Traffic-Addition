package at.tobiazsh.myworld.traffic_addition.rendering.text;

import at.tobiazsh.myworld.traffic_addition.access.client.GlyphIdentifierHolder;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.EmptyArea;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Optional;

/**
 * Custom TextRenderer exclusively for this mod to prevent z-fighting when viewing signs from further away using custom render layers. Pairs with CustomRenderLayer.TextLayering.
 */
@Environment(EnvType.CLIENT)
public class CustomTextRenderer extends Font {

    public CustomTextRenderer(Provider fonts) {
        super(fonts);
    }

    /**
     * @param color the text color in the 0xAARRGGBB format
     */
    public void draw(
            String string,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            Matrix4f matrix,
            MultiBufferSource vertexConsumers,
            CustomRenderLayer.TextLayering.LayeringType layeringType,
            int backgroundColor,
            int light
    ) {
        Font.PreparedText glyphDrawable = this.prepareText(string, x, y, color, shadow, backgroundColor);
        glyphDrawable.visit(CustomGlyphDrawer.drawing(vertexConsumers, matrix, layeringType, light, zOffset)); // <-- Custom Glyph Drawer
    }

    @Environment(EnvType.CLIENT)
    public interface CustomGlyphDrawer extends Font.GlyphVisitor {
        static CustomGlyphDrawer drawing(MultiBufferSource vertexConsumers, Matrix4f matrix, CustomRenderLayer.TextLayering.LayeringType layeringType, int light, float zOffset) {
            return new CustomGlyphDrawer() {
                @Override
                public void acceptGlyph(TextRenderable.@NotNull Styled glyph) {
                    this.draw(glyph);
                }

                @Override
                public void acceptEffect(@NotNull TextRenderable bakedGlyph) {
                    this.draw(bakedGlyph);
                }

                private void draw(TextRenderable glyph) {
                    // Get the id from the default render layer

                    Optional<Identifier> optionalTextureBinding = Optional.empty();

                    if (glyph instanceof BakedSheetGlyph.GlyphInstance glyphInstance) { // AccessWidener on GlyphInstance!
                        optionalTextureBinding = Optional.ofNullable(
                                ((GlyphIdentifierHolder) glyphInstance.glyph()).myworldTrafficAddition$getTexture()
                        );
                    }

                    // Construct our custom layering
                    CustomRenderLayer.TextLayering renderLayer = new CustomRenderLayer.TextLayering(
                            zOffset,
                            layeringType,
                            optionalTextureBinding.orElseGet(() -> Identifier.parse("missing"))
                    );

                    // User RenderLayer
                    VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer.buildRenderType());
                    glyph.render(matrix, vertexConsumer, light, false);
                }
            };
        }

        default void acceptGlyph(TextRenderable.@NotNull Styled glyph) {
        }

        default void acceptEffect(@NotNull TextRenderable rect) {
        }

        default void acceptEmptyArea(@NotNull EmptyArea rect) {
        }
    }

}

//      OLD IMPLEMENTATION USING DRAWER SUBCLASS - KEPT FOR REFERENCE
//
//    public CustomTextRenderer(Function<Identifier, FontStorage> fontStorageAccessor, boolean validateAdvance) {
//        super(fontStorageAccessor, validateAdvance);
//    }
//
//    public int draw(
//            String text,
//            float x,
//            float y,
//            float zOffset,
//            int color,
//            boolean shadow,
//            Matrix4f matrix,
//            VertexConsumerProvider vertexConsumers,
//            CustomRenderLayer.TextLayering.LayeringType layerType,
//            int backgroundColor,
//            int light
//    ) {
//        if (this.isRightToLeft()) {
//            text = this.mirror(text);
//        }
//
//        return this.drawInternal(text, x, y, zOffset, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
//    }
//
//    public int drawInternal(
//            String text,
//            float x,
//            float y,
//            float zOffset,
//            int color,
//            boolean shadow,
//            Matrix4f matrix,
//            VertexConsumerProvider vertexConsumers,
//            CustomRenderLayer.TextLayering.LayeringType layerType,
//            int backgroundColor,
//            int light
//    ) {
//        color = tweakTransparency(color);
//        x = this.drawLayerCustom(text, x, y, zOffset, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
//        return (int) x + (shadow ? 1 : 0);
//    }
//
//    public float drawLayerCustom(
//            String text,
//            float x,
//            float y,
//            float zOffset,
//            int color,
//            boolean shadow,
//            Matrix4f matrix,
//            VertexConsumerProvider vertexConsumerProvider,
//            CustomRenderLayer.TextLayering.LayeringType layerType,
//            int backgroundColor,
//            int light
//    ) {
//        CustomTextRenderer.Drawer drawer = new CustomTextRenderer.Drawer(
//                this,
//                matrix,
//                vertexConsumerProvider,
//                x, y,
//                zOffset,
//                color, backgroundColor,
//                light,
//                shadow,
//                layerType
//        );
//
//        TextVisitFactory.visitFormatted(text, Style.EMPTY, drawer);
//        return drawer.drawLayer(x);
//    }
//
//    private static int tweakTransparency(int argb) {
//        return (argb & -67108864) == 0 ? ColorHelper.fullAlpha(argb) : argb;
//    }
//
//
//
//    public class Drawer extends TextRenderer.Drawer {
//
//        protected CustomTextRenderer textRenderer;
//        protected float zOffset;
//        protected CustomRenderLayer.TextLayering.LayeringType layerType;
//        protected VertexConsumerProvider vertexConsumers;
//        protected Matrix4f matrices;
//        protected int light;
//
//        public Drawer(
//                CustomTextRenderer textRenderer,
//                Matrix4f matrices,
//                VertexConsumerProvider vertexConsumers,
//                float x, float y,
//                float zOffset,
//                int color, int backgroundColor,
//                int light,
//                boolean shadow,
//                CustomRenderLayer.TextLayering.LayeringType layerType
//        ) {
//            super(x, y, color, backgroundColor, shadow);
//            this.textRenderer = textRenderer;
//            this.zOffset = zOffset;
//            this.layerType = layerType;
//            this.vertexConsumers = vertexConsumers;
//            this.matrices = matrices;
//            this.light = light;
//        }
//
//        public float drawLayer(float x) {
//            BakedGlyph bakedGlyph = null;
//            VertexConsumer vertexConsumer;
//
//            if (this.backgroundColor != 0) {
//                BakedGlyph.Rectangle rectangle = new BakedGlyph.Rectangle(x - 1.0f, this.y + 9.0f, this.x, this.y - 1.0f, -1.0f, this.backgroundColor);
//                bakedGlyph = this.textRenderer.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleBakedGlyph();
//                vertexConsumer = this.vertexConsumers.getBuffer(bakedGlyph.getLayer(TextLayerType.NORMAL));
//                bakedGlyph.drawRectangle(rectangle, this.matrices, vertexConsumer, this.light, true);
//            }
//
//            this.drawGlyphs();
//            if (this.rectangles != null) {
//                if (bakedGlyph == null) {
//                    bakedGlyph = this.textRenderer.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleBakedGlyph();
//                }
//
//                vertexConsumer = this.vertexConsumers.getBuffer(bakedGlyph.getLayer(TextLayerType.NORMAL));
//
//                for(BakedGlyph.Rectangle rectangle2 : this.rectangles) {
//                    bakedGlyph.drawRectangle(rectangle2, this.matrices, vertexConsumer, this.light, true);
//                }
//            }
//
//            return this.x;
//        }
//
//        public void drawGlyphs() {
//            for(BakedGlyph.DrawnGlyph drawnGlyph : this.drawnGlyphs) {
//                BakedGlyph bakedGlyph = drawnGlyph.glyph();
//
//                RenderLayer templateLayer = bakedGlyph.getLayer(TextLayerType.NORMAL);
//                RenderLayer.MultiPhase multiPhase = (RenderLayer.MultiPhase) templateLayer;
//                RenderLayer.MultiPhaseParameters multiPhaseParameters = multiPhase.phases;
//                RenderPhase.TextureBase textureBase = multiPhaseParameters.texture;
//                Optional<Identifier> optId = textureBase.getId();
//                Identifier id = optId.orElseGet(() -> Identifier.of("missing"));
//
//                if (Objects.equals(id.getPath(), "missing"))
//                    MyWorldTrafficAddition.LOGGER.error("Couldn't find Identifier of RenderLayer! DrawnGlyph: {}", drawnGlyph);
//
//                CustomRenderLayer.TextLayering constructedLayering = new CustomRenderLayer.TextLayering(this.zOffset, this.layerType, id);
//
//                VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(constructedLayering.buildRenderLayer());
//                bakedGlyph.draw(drawnGlyph, this.matrices, vertexConsumer, this.light, true);
//            }
//        }
//
//    }