package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontSet;
import java.util.concurrent.atomic.AtomicLong;

public record DynamicMinecraftFont(
        LogicalFont logicalFont,
        CustomTextRenderer renderer,
        GlyphProvider provider,
        FontSet fontSet,
        FontLifetime lifetime,
        AtomicLong lastUsedTick
) {

    public void use() {
        lastUsedTick.set(System.currentTimeMillis() / 1000L);
    }

}
