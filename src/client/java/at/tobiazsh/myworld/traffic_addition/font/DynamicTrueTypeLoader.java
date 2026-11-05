package at.tobiazsh.myworld.traffic_addition.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DynamicTrueTypeLoader {
    private DynamicTrueTypeLoader() {}

    public record Options (
            float fontSize,
            float oversampleFactor,
            TrueTypeGlyphProviderDefinition.Shift shift,
            String skipChars,
            boolean isDefault
    ) {
        /**
         * Creates an Options instance from JSON content, commonly found inside
         * <code>/assets/&lt;MOD_ID&gt;/font/font_name.json</code>.
         *
         * @param jsonContent The JSON content containing the provider0 configuration.
         * @param skipChars A string of characters to skip when loading the font.
         *
         * @return An Options instance with the parsed configuration.
         * @throws IllegalArgumentException If the JSON content is invalid or missing required fields.
         */
        public static Options fromJsonContent(String jsonContent, String skipChars) throws IllegalArgumentException {
            JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();

            if (!json.has("providers"))
                throw new IllegalArgumentException("Missing 'providers' field in JSON content");

            JsonObject provider0 = json.getAsJsonArray("providers").get(0).getAsJsonObject();

            if (!isJsonValid(provider0))
                throw new IllegalArgumentException("Invalid 'providers' JSON structure");

            float fontSize = provider0.get("size").getAsFloat();
            float oversampleFactor = provider0.get("oversample").getAsFloat();

            boolean isDefault = json.has("default") && json.get("default").getAsBoolean();

            JsonArray shiftArray = provider0.getAsJsonArray("shift");

            try {
                float shiftX = shiftArray.get(0).getAsFloat();
                float shiftY = shiftArray.get(1).getAsFloat();

                TrueTypeGlyphProviderDefinition.Shift shift =
                        new TrueTypeGlyphProviderDefinition.Shift(shiftX, shiftY);

                return new Options(fontSize, oversampleFactor, shift, skipChars, isDefault);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid 'shift' JSON structure", e);
            }
        }

        /**
         * Validates the JSON structure of the provider0 configuration.
         *
         * @param provider The JsonObject representing the provider0 configuration.
         *
         * @return True if the JSON structure is valid, false otherwise.
         */
        private static boolean isJsonValid(JsonObject provider) {
            return provider.has("type") &&
                   provider.has("file") &&
                   provider.has("size") &&
                   provider.has("oversample") &&
                   provider.has("shift");
        }
    }

    public static GlyphProvider fromBytes(byte[] fontBytes, Options options) throws IOException {
        ByteBuffer fontData = MemoryUtil.memAlloc(fontBytes.length);
        fontData.put(fontBytes).flip();

        boolean ownedByProvider = false; // We don't want the provider0 to free the buffer, we manage it ourselves

        try {
            FT_Face fontFace;

            synchronized (FreeTypeUtil.LIBRARY_LOCK) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    PointerBuffer pointerBuffer = stack.mallocPointer(1);

                    FreeTypeUtil.assertError(
                            FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), fontData, 0L, pointerBuffer),
                            "Initializing font face from memory"
                    );

                    fontFace = FT_Face.create(pointerBuffer.get());
                }

                String format = FreeType.FT_Get_Font_Format(fontFace);

                if (!"TrueType".equals(format))
                    throw new IOException("Invalid font format: " + format);

                FreeTypeUtil.assertError(
                        FreeType.FT_Select_Charmap(fontFace, FreeType.FT_ENCODING_UNICODE),
                        "Selecting Unicode charmap"
                );
            }

            // TrueTypeGlyphProvider will take ownership of the fontData buffer and free it when done
            // Freeing must be done in close()

            GlyphProvider provider = new TrueTypeGlyphProvider(
                    fontData,
                    fontFace,
                    options.fontSize(),
                    options.oversampleFactor(),
                    options.shift().x(),
                    options.shift().y(),
                    options.skipChars()
            );

            ownedByProvider = true;

            return provider;
        } finally {
            if (!ownedByProvider) {
                MemoryUtil.memFree(fontData); // only free if the provider0 didn't take ownership of the buffer (failed to create provider0)
            }
        }
    }

    /**
     * Loads a TrueType font from an InputStream and returns a GlyphProvider.
     *
     * <p>Can be used on files, resources and even network responses.</p>
     *
     * @param fontStream The InputStream containing the TrueType font data.
     * @param options The {@link Options} for loading the font.
     *
     * @return A GlyphProvider for the loaded font.
     * @throws IOException If an I/O error occurs while reading the font data.
     */
    public static GlyphProvider fromStream(InputStream fontStream, Options options) throws IOException {
        return fromBytes(fontStream.readAllBytes(), options);
    }

    /**
     * Loads a TrueType font from a file and returns a GlyphProvider.
     *
     * <p>To be used if a TTF file which exists outside of resource must be loaded.</p>
     *
     * <p>You may also use {@link #fromStream(InputStream, Options)} if that's more desirable.</p>
     *
     * @param path The absolute path to the TrueType font file outside of resources.
     * @param options The {@link Options} for loading the font.
     *
     * @return A GlyphProvider for the loaded font.
     * @throws IOException If an I/O error occurs while reading the font file.
     */
    public static GlyphProvider fromFile(Path path, Options options) throws IOException {
        return fromStream(Files.newInputStream(path), options);
    }

    /**
     * Loads a TrueType font from a resource path and returns a GlyphProvider.
     *
     * @param resourcePath The path to the TrueType font resource (e.g., "/assets/modid/fonts/myfont.ttf").
     * @param options The {@link Options} for loading the font.
     *
     * @return A GlyphProvider for the loaded font.
     * @throws IOException If an I/O error occurs while reading the font resource or if the resource is not found.
     */
    public static GlyphProvider fromResource(String resourcePath, Options options) throws IOException {
        try (InputStream fontStream = DynamicTrueTypeLoader.class.getResourceAsStream(resourcePath)) {
            if (fontStream == null) {
                throw new IOException("Font resource not found at path: " + resourcePath);
            }
            return fromStream(fontStream, options);
        }
    }
}
