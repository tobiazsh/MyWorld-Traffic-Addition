package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import at.tobiazsh.myworld.traffic_addition.mixin.client.font.FontManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.EffectGlyph;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;

public class FontUtil {
    private FontUtil() {}

    public static TextRenderer createTextRenderer(Identifier font) {
        FontManagerAccessor fma = ((FontManagerAccessor) ((MinecraftClientAccessor) MinecraftClient.getInstance()).myworldTrafficAddition$getFontManager());

        TextRenderer.GlyphsProvider provider = new TextRenderer.GlyphsProvider() {
            private FontStorage pickStorage() {
                return fma.getFontStorages().getOrDefault(font, fma.getMissingStorage());
            }

            @Override
            public GlyphProvider getGlyphs(StyleSpriteSource source) {
                return pickStorage().getGlyphs(true);
            }

            @Override
            public EffectGlyph getRectangleGlyph() {
                return pickStorage().getRectangleBakedGlyph();
            }
        };

        return new CustomTextRenderer(provider);
    }
}
