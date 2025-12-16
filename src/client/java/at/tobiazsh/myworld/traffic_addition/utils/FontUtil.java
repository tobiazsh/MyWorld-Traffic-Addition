package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import at.tobiazsh.myworld.traffic_addition.mixin.client.font.FontManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class FontUtil {
    private FontUtil() {}

    public static Font createTextRenderer(Identifier font) {
        FontManagerAccessor fma = ((FontManagerAccessor) ((MinecraftClientAccessor) Minecraft.getInstance()).myworldTrafficAddition$getFontManager());

        Font.Provider provider = new Font.Provider() {
            private FontSet pickStorage() {
                return fma.getFontSets().getOrDefault(font, fma.getMissingFontSet());
            }

            @Override
            public GlyphSource glyphs(FontDescription source) {
                return pickStorage().source(true);
            }

            @Override
            public EffectGlyph effect() {
                return pickStorage().whiteGlyph();
            }
        };

        return new CustomTextRenderer(provider);
    }
}
