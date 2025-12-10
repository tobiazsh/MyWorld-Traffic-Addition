package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.resource.Location;
import at.tobiazsh.myworld.traffic_addition.resource.ResourceLoader;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.font.*;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public record CustomTrueTypeFontLoader(
        Location location,
        float fontSize,
        float oversampleFactor,
        TrueTypeFontLoader.Shift shift,
        String skipChars
) implements FontLoader {

    public Font loadFont(ResourceManager ignored) throws IOException {
        FT_Face freeTypeFace;
        ByteBuffer fontData = null;

        // Load font data from the specified location
        try (InputStream fontStream = ResourceLoader.getResourceAsStream(location)) { // IS NOT ALWAYS found in RESOURCES! Depends if Location is outside or inside resources. ResourceManager will load both locations!
            TrueTypeFont font;

            if (fontStream == null) {
                throw new IOException("Font resource not found at location: " + location);
            }

            byte[] fontBytes = fontStream.readAllBytes();
            fontData = MemoryUtil.memAlloc(fontBytes.length);
            fontData.put(fontBytes);
            fontData.flip();

            synchronized (FreeTypeUtil.LOCK) { // Maybe not synchronise?
                try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                    PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                    FreeTypeUtil.checkFatalError(FreeType.FT_New_Memory_Face(FreeTypeUtil.initialize(), fontData, 0L, pointerBuffer), "Initializing font face from memory");
                    freeTypeFace = FT_Face.create(pointerBuffer.get());
                }

                String format = FreeType.FT_Get_Font_Format(freeTypeFace);
                if (!"TrueType".equals(format)) {
                    throw new IOException("Invalid font format: " + format);
                }

                FreeTypeUtil.checkFatalError(FreeType.FT_Select_Charmap(freeTypeFace, FreeType.FT_ENCODING_UNICODE), "Selecting Unicode charmap");
                font = new TrueTypeFont(fontData, freeTypeFace, fontSize, oversampleFactor, shift.x(), shift.y(), skipChars);
            }

            return font;
        } finally {
            if (fontData != null) {
                MemoryUtil.memFree(fontData);
            }
        }
    }

    @Override
    public FontType getType() {
        return FontType.TTF;
    }

    @Override
    public Either<FontLoader.Loadable, FontLoader.Reference> build() {
        return Either.left(this::loadFont);
    }
}
