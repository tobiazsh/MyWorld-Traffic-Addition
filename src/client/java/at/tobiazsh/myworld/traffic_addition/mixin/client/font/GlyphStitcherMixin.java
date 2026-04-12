package at.tobiazsh.myworld.traffic_addition.mixin.client.font;

import at.tobiazsh.myworld.traffic_addition.access.client.FontTextureAccessor;
import at.tobiazsh.myworld.traffic_addition.access.client.GlyphIdentifierHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GlyphStitcher.class)
public abstract class GlyphStitcherMixin {

    @Shadow
    @Final
    public List<FontTexture> textures;

    @Inject(
            method = "stitch",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private void alterBakedSheetGlyph(
            GlyphInfo glyphInfo,
            GlyphBitmap glyphBitmap,
            CallbackInfoReturnable<BakedSheetGlyph> cir,
            @Local Identifier identifier,
            @Local(ordinal = 0) FontTexture fontTexture2
    ) {
        // Store texture identifier inside the font texture
        ((FontTextureAccessor) fontTexture2).myworldTrafficAddition$setFontTexture(identifier);
    }

    @Redirect(
            method = "stitch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/FontTexture;add(Lcom/mojang/blaze3d/font/GlyphInfo;Lcom/mojang/blaze3d/font/GlyphBitmap;)Lnet/minecraft/client/gui/font/glyphs/BakedSheetGlyph;"
            )
    )
    private BakedSheetGlyph redirectAdd(
            FontTexture instance,
            GlyphInfo glyphInfo,
            GlyphBitmap glyphBitmap
    ) {
        BakedSheetGlyph glyph = instance.add(glyphInfo, glyphBitmap);

        if (glyph != null) {
            Identifier id = ((FontTextureAccessor) instance)
                    .myworldTrafficAddition$getFontTexture();

            ((GlyphIdentifierHolder) glyph)
                    .myworldTrafficAddition$setTexture(id);
        }

        return glyph;
    }
}
