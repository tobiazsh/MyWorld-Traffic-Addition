package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;
import at.tobiazsh.myworld.traffic_addition.font.LogicalFont;
import dev.tobiazsh.imguib3d.client.font.FontIdentifier;
import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class ImGuiFontManager {

    private ImGuiFontManager() {}

    private static final ImGuiFontManager INSTANCE = new ImGuiFontManager();
    private static final long STALE_SECONDS = 15L;

    public static ImGuiFontManager getInstance() {
        return INSTANCE;
    }

    public record FontKey(
            LogicalFont logicalFont,
            int fontSize
    ) {}

    private final Map<FontKey, LivingImGuiFont> fontEntries = new ConcurrentHashMap<>();
    private final Map<FontKey, CompletableFuture<Optional<LivingImGuiFont>>> inFlight = new ConcurrentHashMap<>();

    public CompletableFuture<Optional<LivingImGuiFont>> loadOrGet(FontDefinition fontDefinition, int fontSize) {
        var fontKey = new FontKey(fontDefinition.getLogicalFont(), fontSize);

        LivingImGuiFont existingEntry = fontEntries.get(fontKey);
        if (existingEntry != null) {
            existingEntry.use();
            return CompletableFuture.completedFuture(Optional.of(existingEntry));
        }

        if (inFlight.containsKey(fontKey))
            return inFlight.get(fontKey);

        return inFlight.computeIfAbsent(fontKey, ignored -> {
            try {
                return ImGuiFont.loadFromStreamTTF(
                        fontDefinition.openStream(),
                        FontIdentifier.of(MyWorldTrafficAddition.MOD_ID, fontDefinition.getLogicalFont().fontHash()),
                        fontSize
                ).thenApplyAsync(font -> {
                    var livingFont = new LivingImGuiFont(
                            fontDefinition.getLogicalFont(),
                            fontDefinition.getLifetime(),
                            font,
                            fontSize,
                            new AtomicLong(System.currentTimeMillis() / 1000L)
                    );

                    fontEntries.put(fontKey, livingFont);
                    return Optional.of(livingFont);
                }, Minecraft.getInstance()).whenComplete((_, _) -> inFlight.remove(fontKey));
            } catch (IOException e) {
                // Return an empty future if the font cannot be loaded
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    public void sweep() {
        long currentTimeSeconds = System.currentTimeMillis() / 1000L;

        for (Map.Entry<FontKey, LivingImGuiFont> entry : fontEntries.entrySet()) {
            if (entry.getValue().isStale(currentTimeSeconds, STALE_SECONDS)) {
                entry.getValue().imguiFont().disposeLater();
                fontEntries.remove(entry.getKey());
            }
        }
    }
}
