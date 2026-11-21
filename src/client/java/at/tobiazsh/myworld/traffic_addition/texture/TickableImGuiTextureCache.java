package at.tobiazsh.myworld.traffic_addition.texture;

import net.minecraft.resources.Identifier;

public interface TickableImGuiTextureCache {
    TickableImGuiTexture byId(Identifier id);
    void add(TickableImGuiTexture texture);
    void sweep(long currentLevelTick);
}
