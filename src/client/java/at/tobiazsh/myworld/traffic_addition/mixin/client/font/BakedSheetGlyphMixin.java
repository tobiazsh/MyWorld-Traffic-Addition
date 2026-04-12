package at.tobiazsh.myworld.traffic_addition.mixin.client.font;

import at.tobiazsh.myworld.traffic_addition.access.client.GlyphIdentifierHolder;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedSheetGlyph.class)
public class BakedSheetGlyphMixin implements GlyphIdentifierHolder {

    @Unique
    private Identifier textureId;

    @Override
    public Identifier myworldTrafficAddition$getTexture() {
        return textureId;
    }

    @Override
    public void myworldTrafficAddition$setTexture(Identifier texture) {
        this.textureId = texture;
    }
}
