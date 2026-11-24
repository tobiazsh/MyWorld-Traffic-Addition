package at.tobiazsh.myworld.traffic_addition.texture;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickableImGuiTextureCacheImpl implements TickableImGuiTextureCache {
    private final Map<Identifier, TickableImGuiTexture> textures = new ConcurrentHashMap<>();
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
        textures.values().removeIf(current -> current.isStale(currentLevelTick));
    }
}
