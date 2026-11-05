package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import at.tobiazsh.myworld.traffic_addition.mixin.client.font.FontManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class DynamicFontManager {

    private DynamicFontManager() {}

    private static final DynamicFontManager INSTANCE = new DynamicFontManager();

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws IOException;
    }

    public static final Executor LOAD_EXECUTOR =
            Executors.newFixedThreadPool(2, r -> new Thread(r, "DynamicFontLoader-Thread"));

    private final Map<LogicalFont, DynamicMinecraftFont> fontEntries = new ConcurrentHashMap<>();
    private final Map<LogicalFont, CompletableFuture<DynamicMinecraftFont>> inFlight = new ConcurrentHashMap<>();

    public static DynamicFontManager getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<DynamicMinecraftFont> loadOrGet(
            FontDefinition fontDefinition,
            @Nullable FontLifetime lifetime
    ) {
        ThrowingSupplier<GlyphProvider> glyphProviderSupplier = () -> {
            try {
                return DynamicTrueTypeLoader.fromStream(fontDefinition.openStream(), fontDefinition.getOptions());
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        };

        return loadOrGet(
                fontDefinition.getLogicalFont(),
                glyphProviderSupplier,
                lifetime == null ? fontDefinition.getLifetime() : lifetime
        );
    }

    public CompletableFuture<DynamicMinecraftFont> loadOrGet(
            LogicalFont logicalFont,
            ThrowingSupplier<GlyphProvider> loader,
            FontLifetime lifetime
    ) {
        DynamicMinecraftFont existing = fontEntries.get(logicalFont);
        if (existing != null) {
            existing.use();
            return CompletableFuture.completedFuture(existing);
        }

        return inFlight.computeIfAbsent(logicalFont, key -> load(key, loader, lifetime)
                .whenComplete((_, _) -> inFlight.remove(key)));

    }

    public CompletableFuture<DynamicMinecraftFont> load(
            LogicalFont logicalFont,
            ThrowingSupplier<GlyphProvider> loader,
            FontLifetime lifetime
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loader.get();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, LOAD_EXECUTOR).thenApplyAsync(glyphProvider -> {
            DynamicMinecraftFont font = loadInternal(logicalFont, glyphProvider, lifetime);
            fontEntries.put(logicalFont, font);
            return font;
        }, Minecraft.getInstance());

    }

    public DynamicMinecraftFont loadInternal(
            LogicalFont logicalFont,
            GlyphProvider glyphProvider,
            FontLifetime lifetime
    ) {
        unloadInternal(logicalFont);

        var stitcher = new GlyphStitcher(
                Minecraft.getInstance().getTextureManager(),
                constructIdentifier(logicalFont)
        );

        FontSet fontSet = new FontSet(stitcher);

        fontSet.reload(
                List.of(new GlyphProvider.Conditional(glyphProvider, FontOption.Filter.ALWAYS_PASS)),
                Set.of()
        );

        long currentTick = currentTick();

        if (lifetime != FontLifetime.PERMANENT && currentTick < 0) {
            MyWorldTrafficAddition.LOGGER.warn(
                    "Font {} is loaded as temporary, but the game tick is not available. " +
                            "This may cause the font to be unloaded prematurely.", logicalFont
            );
        }

        Font.Provider fontProvider = newProvider(logicalFont);

        return new DynamicMinecraftFont(
                logicalFont,
                new CustomTextRenderer(fontProvider),
                glyphProvider,
                fontSet,
                lifetime,
                new AtomicLong(currentTick)
        );
    }

    private Font.Provider newProvider(LogicalFont logicalFont) {
        AtomicBoolean warnedMissing = new AtomicBoolean(false);

        return new Font.Provider() {
            @SuppressWarnings("resource")
            @Override
            public GlyphSource glyphs(FontDescription font) {
                DynamicMinecraftFont current = fontEntries.get(logicalFont);

                if (current != null) {
                    current.use();
                    return current.fontSet().source(true);
                }

                return missingFontSet().source(true);
            }

            @SuppressWarnings("resource")
            @Override
            public EffectGlyph effect() {
                DynamicMinecraftFont current = fontEntries.get(logicalFont);

                if (current != null) {
                    current.use();
                    return current.fontSet().whiteGlyph();
                }

                return missingFontSet().whiteGlyph();
            }

            private void warnOnce() {
                if (warnedMissing.compareAndSet(false, true)) {
                    MyWorldTrafficAddition.LOGGER.warn(
                            "Font {} not loaded (yet, or no longer); using missing-glyph fallback", logicalFont);
                }
            }
        };
    }

    @SuppressWarnings("resource")
    private static FontSet missingFontSet() {
        Minecraft client = Minecraft.getInstance();
        FontManagerAccessor fma = (FontManagerAccessor) ((MinecraftClientAccessor) client).myworldTrafficAddition$getFontManager();
        return fma.getMissingFontSet();
    }


    public void unload(LogicalFont id) {
        Minecraft.getInstance().execute(() -> unloadInternal(id));
    }

    private void unloadInternal(LogicalFont logicalFont) {
        DynamicMinecraftFont old = fontEntries.remove(logicalFont);
        if (old != null) {
            old.fontSet().close(); // Free atlas textures
            old.provider().close();  // Free face and native buffer
        }
    }

    public void sweep(long gracePeriodTicks) {
        long currentTick = currentTick();

        if (currentTick < 0)
            return;

        for (Map.Entry<LogicalFont, DynamicMinecraftFont> entry : fontEntries.entrySet()) {
            if (entry.getValue().lifetime() == FontLifetime.PERMANENT)
                continue;

            if (currentTick - entry.getValue().lastUsedTick().get() > gracePeriodTicks)
                unloadInternal(entry.getKey());
        }
    }

    /**
     * Returns the current game tick. If the level is not loaded, -1 will be returned.
     */
    private long currentTick() {
        var level = Minecraft.getInstance().level;

        if (level == null)
            return -1;

        return level.getGameTime();
    }

    private Identifier constructIdentifier(LogicalFont logicalFont) {
        String path = "dyn_font/" + logicalFont.fontHash();
        return Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, path);
    }

    public boolean isLoaded(LogicalFont logicalFont) {
        return fontEntries.containsKey(logicalFont);
    }

    public void unloadAll() {
        Minecraft.getInstance().execute(() -> fontEntries.keySet().forEach(this::unloadInternal));
    }
}
