package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;
import at.tobiazsh.myworld.traffic_addition.font.FontLifetime;
import at.tobiazsh.myworld.traffic_addition.font.LogicalFont;
import dev.tobiazsh.imguib3d.client.font.FontIdentifier;
import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class FontManager {

    private FontManager() {}

    private static final FontManager INSTANCE = new FontManager();
    private final long gracePeriodTicks = 15; // 15 seconds

    public static FontManager getInstance() {
        return INSTANCE;
    }

    public record FontEntry(
            LogicalFont logicalFont,
            FontLifetime lifetime,
            ImGuiFont imguiFont,
            float fontSize,
            AtomicLong lastUsedTimeSeconds
    ) {}

    public record FontKey(
            LogicalFont logicalFont,
            int fontSize
    ) {}

    private final Map<FontKey, FontEntry> fontEntries = new ConcurrentHashMap<>();

    private CompletableFuture<ImGuiFont> loadFont(FontDefinition fontDefinition, int fontSize) {
        var fontKey = new FontKey(fontDefinition.getLogicalFont(), fontSize);
        var lifetime = fontDefinition.getLifetime();

        FontEntry existingEntry = fontEntries.get(fontKey);

        if (existingEntry != null) {
            existingEntry.lastUsedTimeSeconds.set(currentTimeSeconds());
            return CompletableFuture.completedFuture(existingEntry.imguiFont);
        }

        try {
            return ImGuiFont.loadFromStreamTTF(
                    fontDefinition.openStream(),
                    FontIdentifier.of(MyWorldTrafficAddition.MOD_ID, fontDefinition.getLogicalFont().fontHash()),
                    fontSize
            );
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new IOException("Default font is not loaded!", e));
        }
    }

    public CompletableFuture<ImGuiFont> loadAndRegisterFont(FontDefinition fontDefinition, int fontSize) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadFont(fontDefinition, fontSize).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenApplyAsync(imguiFont -> {
            var entry = new FontEntry(
                    fontDefinition.getLogicalFont(),
                    fontDefinition.getLifetime(),
                    imguiFont,
                    fontSize,
                    new AtomicLong(currentTimeSeconds())
            );

            var fontKey = new FontKey(fontDefinition.getLogicalFont(), fontSize);

            fontEntries.put(fontKey, entry);
            return imguiFont;
        }, Minecraft.getInstance());
    }

    public void tick() {
        long currentTime = currentTimeSeconds();

        for (Map.Entry<FontKey, FontEntry> entry : fontEntries.entrySet()) {
            if (currentTime - entry.getValue().lastUsedTimeSeconds.get() > gracePeriodTicks) {
                fontEntries.remove(entry.getKey());
                entry.getValue().imguiFont.disposeLater();
                continue;
            }

            entry.getValue().lastUsedTimeSeconds.set(currentTime);
        }
    }

    private long currentTimeSeconds() {
        return Math.round(System.currentTimeMillis() * 0.001);
    }

    public void sweep() {

    }
}
