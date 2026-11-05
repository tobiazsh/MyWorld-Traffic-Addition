package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import at.tobiazsh.myworld.traffic_addition.mixin.client.font.FontManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public record DynamicMinecraftFont(
        LogicalFont logicalFont,
        CustomTextRenderer renderer,
        GlyphProvider provider,
        FontSet fontSet,
        FontLifetime lifetime,
        AtomicLong lastUsedTick
) {

    public enum FontLifetime {
        PERMANENT,
        TEMPORARY
    }

    private static final Map<LogicalFont, CustomTextRenderer> FONT_RENDERERS = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static CompletableFuture<DynamicMinecraftFont> load(
            LogicalFont logicalFont,
            ThrowingSupplier<GlyphProvider> loader,
            FontLifetime lifetime
    ) {
        CustomTextRenderer renderer = FONT_RENDERERS.computeIfAbsent(
                logicalFont,
                key -> new CustomTextRenderer(providerFor(key))
        );

        var handle = new DynamicMinecraftFont(logicalFont, renderer);

        Supplier<GlyphProvider> unchecked = () -> {
            try {
                return loader.get();
            } catch (Exception e) {
                throw new CompletionException("Failed to load font with ID " + logicalFont, e);
            }
        };

        return DynamicFontManager.getInstance().load(logicalFont, unchecked, lifetime)
                .handle((_, error) -> {
                    if (error != null) {
                        MyWorldTrafficAddition.LOGGER.error(
                                "Failed to load font with ID {}: {}", logicalFont, error.getMessage(), error
                        );
                    }

                    return handle;
                });
    }

    private static Font.Provider providerFor(LogicalFont id) {
        var warnedMissing = new AtomicBoolean(false);
        var manager = DynamicFontManager.getInstance();

        return new Font.Provider() {

            @SuppressWarnings("resource")
            @Override
            public @NonNull GlyphSource glyphs(@NonNull FontDescription descriptor) {
                GlyphSource source = manager.getGlyphSource(id, true);

                if (source != null)
                    return source;

                warnOnce();
                return missingFontSet().source(true);
            }

            @SuppressWarnings("resource")
            @Override
            public EffectGlyph effect() {
                EffectGlyph effectGlyph = manager.getEffectGlyph(id);

                if (effectGlyph != null)
                    return effectGlyph;

                warnOnce();
                return missingFontSet().whiteGlyph();
            }

            private void warnOnce() {
                if (!warnedMissing.getAndSet(true)) {
                    MyWorldTrafficAddition.LOGGER.warn(
                            "Font with ID {} is not loaded yet. Using default font instead.", id
                    );
                }
            }
        };
    }

    private static FontSet missingFontSet() {
        var client = Minecraft.getInstance();

        @SuppressWarnings("resource")
        FontManagerAccessor fma = (FontManagerAccessor) ((MinecraftClientAccessor) client)
                .myworldTrafficAddition$getFontManager();

        return fma.getMissingFontSet();
    }

}
