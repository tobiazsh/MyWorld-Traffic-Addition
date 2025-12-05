package at.tobiazsh.myworld.traffic_addition.mixin.client.font;

import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FontManager.class)
public interface FontManagerAccessor {
    @Accessor("fontStorages")
    Map<Identifier, FontStorage> getFontStorages();

    /**
     * Gets the missing font storage used as a fallback.
     * @return The missing FontStorage.
     */
    @Accessor("missingStorage")
    FontStorage getMissingStorage();
}
