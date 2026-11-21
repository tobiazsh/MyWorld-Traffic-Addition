package at.tobiazsh.myworld.traffic_addition.texture;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickableImGuiTextureCacheImpl implements TickableImGuiTextureCache {
    private final Map<Identifier, TickableImGuiTexture> textures = new ConcurrentHashMap<>();
    private static final int STALE_TICKS = 10 * 20; // 10 seconds aka. 20 ticks

    private static final TickableImGuiTextureCacheImpl INSTANCE = new TickableImGuiTextureCacheImpl();

    public static TickableImGuiTextureCacheImpl getInstance() {
        return INSTANCE;
    }

    public @Nullable TickableImGuiTexture byId(Identifier id) {
        return textures.get(id);
    }

    @Override
    public void add(TickableImGuiTexture texture) {
        textures.put(texture.getIdentifier(), texture);
    }

    public void sweep(long currentLevelTick) {
        List<HybridTexture> toDispose = new ArrayList<>();

        var iter = textures.values().iterator();

        while (iter.hasNext()) {
            var current = iter.next();

            if (current.isPinned())
                continue; // Do not dispose of pinned textures

            long lastUsedTick = current.getLastUsedTick();

            if (lastUsedTick + STALE_TICKS < currentLevelTick)
                iter.remove();
        }
    }
}
