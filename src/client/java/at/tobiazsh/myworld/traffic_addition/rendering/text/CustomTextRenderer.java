package at.tobiazsh.myworld.traffic_addition.rendering.text;

import at.tobiazsh.myworld.traffic_addition.access.client.GlyphIdentifierHolder;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;


/**
 * Custom TextRenderer exclusively for this mod to prevent z-fighting when viewing signs from further away using custom render layers. Pairs with CustomRenderLayer.TextLayering.
 */
@Environment(EnvType.CLIENT)
public class CustomTextRenderer extends Font {

    private static final Identifier MISSING_TEXTURE = Identifier.parse("missing");

    public CustomTextRenderer(Provider fonts) {
        super(fonts);
    }

    public float draw(
            String string,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            @NonNull SubmitNodeCollector queue,
            PoseStack poseStack,
            CustomRenderLayer.TextLayering.LayeringType layeringType,
            int backgroundColor,
            int light
    ) {
        Font.PreparedText prepared = this.prepareText(string, x, y, color, shadow, backgroundColor);
        return drawPrepared(prepared, x, this.width(string), shadow, queue, poseStack, layeringType, light, zOffset);
    }


    public float draw(
            FormattedCharSequence text,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            @NonNull SubmitNodeCollector queue,
            PoseStack poseStack,
            CustomRenderLayer.TextLayering.LayeringType layeringType,
            int backgroundColor,
            int light
    ) {
        Font.PreparedText prepared = this.prepareText(text, x, y, color, shadow, false, backgroundColor);
        return drawPrepared(prepared, x, this.width(text), shadow, queue, poseStack, layeringType, light, zOffset);
    }


    public float draw(
            FormattedText text,
            float x,
            float y,
            float zOffset,
            int color,
            boolean shadow,
            @NonNull SubmitNodeCollector queue,
            PoseStack poseStack,
            CustomRenderLayer.TextLayering.LayeringType layeringType,
            int backgroundColor,
            int light
    ) {
        FormattedCharSequence ordered = Language.getInstance().getVisualOrder(text);
        return draw(ordered, x, y, zOffset, color, shadow, queue, poseStack, layeringType, backgroundColor, light);
    }


    private float drawPrepared(
            Font.PreparedText prepared,
            float x,
            float width,
            boolean shadow,
            @NonNull SubmitNodeCollector queue,
            PoseStack poseStack,
            CustomRenderLayer.TextLayering.LayeringType layeringType,
            int light,
            float zOffset
    ) {
        prepared.visit(CustomGlyphDrawer.drawing(queue, poseStack, layeringType, light, zOffset));
        return x + width + (shadow ? 1.0F : 0.0F);
    }

    @Environment(EnvType.CLIENT)
    public interface CustomGlyphDrawer extends Font.GlyphVisitor {
        static CustomGlyphDrawer drawing(
                @NonNull SubmitNodeCollector queue,
                PoseStack poseStack,
                CustomRenderLayer.TextLayering.LayeringType layeringType,
                int light,
                float zOffset
        ) {
            return new CustomGlyphDrawer() {
                @Override
                public void acceptRenderable(TextRenderable renderable) {
                    this.draw(renderable);
                }

                @Override
                public void acceptGlyph(TextRenderable.@NonNull Styled glyph) { this.draw(glyph); }

                @Override
                public void acceptEffect(@NonNull TextRenderable bakedGlyph) { this.draw(bakedGlyph); }

                private void draw(TextRenderable glyph) {
                    Identifier textureId = MISSING_TEXTURE;
                    if (glyph instanceof BakedSheetGlyph.GlyphInstance gi) {
                        Identifier resolved = ((GlyphIdentifierHolder) gi.glyph()).myworldTrafficAddition$getTexture();
                        if (resolved != null) textureId = resolved;
                    }
                    CustomRenderLayer.TextLayering layer =
                            new CustomRenderLayer.TextLayering(zOffset, layeringType, textureId);
                    queue.submitCustomGeometry(
                            poseStack,
                            layer.buildRenderType(),
                            (pose, vc) -> glyph.render(pose.pose(), vc, light, false)
                    );
                }
            };
        }

        @Override
        void acceptRenderable(TextRenderable renderable);
    }

}