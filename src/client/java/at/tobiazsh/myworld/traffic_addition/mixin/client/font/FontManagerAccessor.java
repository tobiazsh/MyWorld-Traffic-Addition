package at.tobiazsh.myworld.traffic_addition.mixin.client.font;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FontManager.class)
public interface FontManagerAccessor {
    @Accessor("fontSets")
    Map<Identifier, FontSet> getFontSets();

    /**
     * Gets the missing font storage used as a fallback.
     * @return The missing FontStorage.
     */
    @Accessor("missingFontSet")
    FontSet getMissingFontSet();
}
