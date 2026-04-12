package at.tobiazsh.myworld.traffic_addition.mixin.client.font;

import at.tobiazsh.myworld.traffic_addition.access.client.FontTextureAccessor;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FontTexture.class)
public class FontTextureMixin implements FontTextureAccessor {

    @Unique
    private Identifier textureId;

    @Override
    public void myworldTrafficAddition$setFontTexture(Identifier texture) {
        this.textureId = texture;
    }

    @Override
    public Identifier myworldTrafficAddition$getFontTexture() {
        return this.textureId;
    }

}
