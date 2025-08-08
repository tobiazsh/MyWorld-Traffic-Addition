package at.tobiazsh.myworld.traffic_addition.rendering;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Custom TextRenderer exclusively for this mod to prevent z-fighting when viewing signs from further away using custom render layers. Pairs with CustomRenderLayer.TextLayering.
 */
@Environment(EnvType.CLIENT)
public class CustomTextRenderer extends TextRenderer {

    public CustomTextRenderer(Function<Identifier, FontStorage> fontStorageAccessor, boolean validateAdvance) {
        super(fontStorageAccessor, validateAdvance);
    }

    public int draw(
            String text,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumers,
            CustomRenderLayer.TextLayering.LayeringType layerType,
            int backgroundColor,
            int light
    ) {
        if (this.isRightToLeft()) {
            text = this.mirror(text);
        }

        return this.drawInternal(text, x, y, zOffset, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, true);
    }

    public int drawInternal(
            String text,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumers,
            CustomRenderLayer.TextLayering.LayeringType layerType,
            int backgroundColor,
            int light,
            boolean mirror
    ) {
        color = tweakTransparency(color);
        x = this.drawLayerCustom(text, x, y, zOffset, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, mirror);
        return (int) x + (shadow ? 1 : 0);
    }

    public float drawLayerCustom(
            String text,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumerProvider,
            CustomRenderLayer.TextLayering.LayeringType layerType,
            int backgroundColor,
            int light,
            boolean swapZIndex
    ) {
        CustomTextRenderer.Drawer drawer = new CustomTextRenderer.Drawer(this, vertexConsumerProvider, x, y, zOffset, color, backgroundColor, shadow, matrix, layerType, light, swapZIndex);
        TextVisitFactory.visitFormatted(text, Style.EMPTY, drawer);
        return drawer.drawLayer(x);
    }

    private static int tweakTransparency(int argb) {
        return (argb & -67108864) == 0 ? ColorHelper.fullAlpha(argb) : argb;
    }



    public class Drawer extends TextRenderer.Drawer {

        protected CustomTextRenderer textRenderer;
        protected float zOffset;
        protected CustomRenderLayer.TextLayering.LayeringType layerType;
        protected VertexConsumerProvider vertexConsumers;
        protected Matrix4f matrices;
        protected int light;

        public Drawer(
                CustomTextRenderer textRenderer,
                Matrix4f matrices,
                VertexConsumerProvider vertexConsumers,
                float x, float y,
                float zOffset,
                int color, int backgroundColor,
                int light,
                boolean shadow,
                CustomRenderLayer.TextLayering.LayeringType layerType
        ) {
            super(x, y, color, backgroundColor, shadow);
            this.textRenderer = textRenderer;
            this.zOffset = zOffset;
            this.layerType = layerType;
            this.vertexConsumers = vertexConsumers;
            this.matrices = matrices;
        }

        public float drawLayer(float x) {
            BakedGlyph bakedGlyph = null;
            if (this.backgroundColor != 0) {
                BakedGlyph.Rectangle rectangle = new BakedGlyph.Rectangle(x - 1.0f, this.y + 9.0f, this.x, this.y - 1.0f, -1.0f, this.backgroundColor);
                bakedGlyph = this.textRenderer.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleBakedGlyph();
                bakedGlyph.drawRectangle(rectangle, this.matrices, vertexConsumer, this.light, true);
            }

            this.drawGlyphs();
            if (this.rectangles != null) {
                if (bakedGlyph == null) {
                    bakedGlyph = this.textRenderer.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleBakedGlyph();
                }

                VertexConsumer vertexConsumer2 = this.vertexConsumers.getBuffer(bakedGlyph.getLayer(TextLayerType.NORMAL));

                for(BakedGlyph.Rectangle rectangle2 : this.rectangles) {
                    bakedGlyph.drawRectangle(rectangle2, this.matrices, vertexConsumer2, this.light, true);
                }
            }

            return this.x;
        }

        public void drawGlyphs() {

            for(BakedGlyph.DrawnGlyph drawnGlyph : this.glyphs) {
                BakedGlyph bakedGlyph = drawnGlyph.glyph();

                RenderLayer templateLayer = bakedGlyph.getLayer(TextLayerType.NORMAL);
                RenderLayer.MultiPhase multiPhase = (RenderLayer.MultiPhase) templateLayer;
                RenderLayer.MultiPhaseParameters multiPhaseParameters = multiPhase.phases;
                RenderPhase.TextureBase textureBase = multiPhaseParameters.texture;
                Optional<Identifier> optId = textureBase.getId();
                Identifier id = optId.orElseGet(() -> Identifier.of("missing"));

                if (Objects.equals(id.getPath(), "missing"))
                    MyWorldTrafficAddition.LOGGER.error("Couldn't find Identifier of RenderLayer! DrawnGlyph: {}", drawnGlyph);

                CustomRenderLayer.TextLayering constructedLayering = new CustomRenderLayer.TextLayering(this.zOffset, this.layerType, id);

                VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(constructedLayering.buildRenderLayer());
                bakedGlyph.draw(drawnGlyph, this.matrix, vertexConsumer, this.light);
            }
        }

    }
}