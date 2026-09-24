package at.tobiazsh.myworld.traffic_addition.texture;

import at.tobiazsh.myworld.traffic_addition.mixin.client.TextureManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class to manage the lifecycle of HybridTextures inside Minecraft's TextureManager.
 */
public final class HybridTextureSweeper {

    private static final HybridTextureSweeper INSTANCE = new HybridTextureSweeper();
    private static final int GRACE_PERIOD_TICKS = 5 * 20; // 5 seconds á 20 tps

    private HybridTextureSweeper() {}

    public static HybridTextureSweeper getInstance() {
        return INSTANCE;
    }

    public void sweep(long currentLevelTick) {
        var textureManager = Minecraft.getInstance().getTextureManager();
        Map<Identifier, AbstractTexture> textures = ((TextureManagerAccessor) textureManager).getByPath();

        List<HybridTexture> toDispose = new ArrayList<>();

        for (AbstractTexture texture : textures.values()) {
            if (!(texture instanceof HybridTexture hybridTexture))
                continue; // Do not dispose of non-HybridTextures

            if (hybridTexture.isPinned())
                continue; // Do not dispose of pinned textures

            long lastUsedTick = hybridTexture.getLastUsedTick();

            if (lastUsedTick + GRACE_PERIOD_TICKS < currentLevelTick)
                toDispose.add(hybridTexture); // Mark for disposal
        }

        for (HybridTexture hybridTexture : toDispose)
            hybridTexture.disposeSafe();
    }

    /**
     * Disposes of all HybridTextures except the pinned ones.
     */
    public void sweepAll() {
        var textureManager = Minecraft.getInstance().getTextureManager();
        Map<Identifier, AbstractTexture> textures = ((TextureManagerAccessor) textureManager).getByPath();

        List<HybridTexture> toDispose = new ArrayList<>();

        for (AbstractTexture texture : textures.values()) {
            if (!(texture instanceof HybridTexture hybridTexture))
                continue; // Do not dispose of non-HybridTextures

            if (hybridTexture.isPinned())
                continue; // Do not dispose of pinned textures

            toDispose.add(hybridTexture); // Mark for disposal
        }

        for (HybridTexture hybridTexture : toDispose)
            hybridTexture.disposeSafe();
    }

    /**
     * Yanks all HybridTextures, including the pinned ones, from Minecraft's TextureManager.
     */
    public void yank() {
        var textureManager = Minecraft.getInstance().getTextureManager();
        Map<Identifier, AbstractTexture> textures = ((TextureManagerAccessor) textureManager).getByPath();

        List<HybridTexture> toDispose = new ArrayList<>();

        for (AbstractTexture texture : textures.values()) {
            if (!(texture instanceof HybridTexture hybridTexture))
                continue; // Do not dispose of non-HybridTextures

            toDispose.add(hybridTexture); // Mark for disposal
        }

        for (HybridTexture hybridTexture : toDispose)
            hybridTexture.dispose();
    }
}
