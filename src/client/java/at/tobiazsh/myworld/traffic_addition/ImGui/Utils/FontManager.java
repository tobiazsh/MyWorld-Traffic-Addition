package at.tobiazsh.myworld.traffic_addition.ImGui.Utils;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.FileSystem;
import imgui.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

import static at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl.*;

public class FontManager {
    public static final Map<String, ImGuiFont> fontCache = new ConcurrentHashMap<>();
    private static final List<FontRequest> fontRequests = new CopyOnWriteArrayList<>();

    private static final FileSystem.Folder availableFonts;

    public static short[] defaultGlyphRanges;

    // Just a few necessary letters that are not included otherwise
    private static final short[] specialCharacters = new short[] {
            0x0110, // Đ (DJ) e.g. Croatian
            0x010F, // đ (dj) e.g. Croatian
            0x0160, // Š (SH) e.g. Croatian
            0x0161, // š (sh) e.g. Croatian
            0x017D, // Ž (ZH) e.g. Croatian
            0x017E, // ž (zh) e.g. Croatian
            0x0106, // Ć (CH) e.g. Croatian
            0x0107, // ć (ch) e.g. Croatian
            0x010C, // Č (CH) e.g. Croatian
            0x010D, // č (ch) e.g. Croatian
            0x010E, // Ď (D) e.g. Slovak
            0x010F, // ď (d) e.g. Slovak
            0x00C1, // Á (A) e.g. Hungarian
            0x00E1, // á (a) e.g. Hungarian
            0x00C9, // É (E) e.g. Hungarian
            0x00E9, // é (e) e.g. Hungarian
            0x00CD, // Í (I) e.g. Hungarian
            0x00ED, // í (i) e.g. Hungarian
            0x00C2, // Â (A) e.g. Romanian
            0x00E2, // â (a) e.g. Romanian
            0x0102, // Ă (A) e.g. Romanian
            0x0103, // ă (a) e.g. Romanian
            0x00CE, // Î (I) e.g. Romanian
            0x00EE, // î (i) e.g. Romanian
            0x00F1, // ñ (n) e.g. Spanish
            0x00D1, // Ñ (N) e.g. Spanish
            0x00D3, // Ó (O) e.g. Spanish
            0x00F3, // ó (o) e.g. Spanish
            0x0141, // Ł (L) e.g. Polish
            0x0142, // ł (l) e.g. Polish
            0x00D8, // Ø (O) e.g. Danish
            0x00F8, // ø (o) e.g. Danish
            0x0147, // Ň (N) e.g. Slovak
            0x0148, // ň (n) e.g. Slovak
            0x0158, // Ř (R) e.g. Czech
            0x0159, // ř (r) e.g. Czech
            0x015E, // Ş (S) e.g. Turkish
            0x015F, // ş (s) e.g. Turkish
            0x0162, // Ţ (T) e.g. Romanian
            0x0163, // ţ (t) e.g. Romanian
            0x0164, // Ť (T) e.g. Slovak
            0x0165, // ť (t) e.g. Slovak
            0x00FD, // ý (y) e.g. Czech
            0x00DD, // Ý (Y) e.g. Czech
            0x016E, // Ů (U) e.g. Czech
            0x016F, // ů (u) e.g. Czech
            0x00DA, // Ú (U) e.g. Czech
            0x00FA, // ú (u) e.g. Czech
            0x0170, // Ű (U) e.g. Hungarian
            0x0171, // ű (u) e.g. Hungarian
            0x0150, // Ő (O) e.g. Hungarian
            0x0151, // ő (o) e.g. Hungarian
    };

    static {
        try {
            availableFonts = Objects.requireNonNull(FileSystem.listFilesRecursive("/assets/" + MyWorldTrafficAddition.MOD_ID + "/font/", true)).removeFoldersCurrentDir().concentrateFileType("TTF");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void buildFontRanges() {
        ImFontGlyphRangesBuilder glyphRangesBuilder = new ImFontGlyphRangesBuilder();
        glyphRangesBuilder.addRanges(ImGui.getIO().getFonts().getGlyphRangesDefault());
        glyphRangesBuilder.addRanges(specialCharacters);

        defaultGlyphRanges = glyphRangesBuilder.buildRanges();
    }

    /**
     * Returns already existing font in the cache
     * @param path The path to the font in the resources
     * @param fontSize The desired font size
     * @return ImGuiFont if it exists, null otherwise
     */
    public static ImGuiFont getFont(String path, float fontSize) {
        String key = path + ":" + fontSize;
        return fontCache.get(key);
    }

    /**
     * Adds font to list of fonts to be registered
     * @param path The path to the font in the resources
     * @param fontSize The desired font size
     * @return CompletableFuture<ImGuiFont> that is probably finished after the next frame finished
     */
    public static CompletableFuture<ImGuiFont> registerFontAsync(String path, float fontSize) {
        String key = path + ":" + fontSize;
        if (fontCache.containsKey(key)) {
            return CompletableFuture.completedFuture(fontCache.get(key));
        }

        FontRequest request = new FontRequest(path, fontSize);
        fontRequests.add(request);
        return request.getFuture();
    }

    /**
     * Rebuilds the font atlas to include newly registered fonts
     * This method should be called after registering new fonts
     */
    public static void rebuildFontAtlas() {
        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();

        // Rebuild the font atlas
        fontAtlas.clear();
        reRegisterFonts();
        registerDefaultFonts();
        fontAtlas.build();

        uploadFontTexture();

        // Notify the renderer (OpenGL) to create the font texture
        ImGuiImpl.imGuiImplGl3.createFontsTexture();

        ImGui.getIO().setFontDefault(Roboto);
    }

    /**
     * Re-registers all registered fonts. Used for rebuilding the font atlas
     */
    private static void reRegisterFonts() {
        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setOversampleV(2);
        fontConfig.setOversampleH(2);

        for (ImGuiFont font : fontCache.values()) {
            byte[] fontBytes = ImGuiImpl.loadFromResource(font.getFontPath());

            if (fontBytes == null) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load font ({}) from resource! Maybe file doesn't exist? Using default font!", font.getFontPath());
                fontBytes = ImGuiImpl.loadFromResource(defaultFontPath);
            }

            font.font = fontAtlas.addFontFromMemoryTTF(fontBytes, font.getFontSize(), fontConfig, defaultGlyphRanges);
        }
    }

    /**
     * Register all pending font futures
     * @see FontManager#fontRequests
     */
    public static void registerPendingFonts() {

        if (fontRequests.isEmpty()) {
            return;
        }

        //new Thread(() -> {
            for (FontRequest request : fontRequests) {
                try {
                    ImFontConfig fontConfig = new ImFontConfig();
                    fontConfig.setOversampleV(2);
                    fontConfig.setOversampleH(2);

                    byte[] fontBytes = ImGuiImpl.loadFromResource(request.getPath());

                    if (fontBytes == null) {
                        MyWorldTrafficAddition.LOGGER.error("Failed to load font ({}) from resource! Maybe file doesn't exist? Using default font!", request.getPath());
                        fontBytes = ImGuiImpl.loadFromResource(defaultFontPath);
                    }

                    ImFont font = ImGui.getIO().getFonts().addFontFromMemoryTTF(fontBytes, request.getFontSize(), fontConfig, defaultGlyphRanges);
                    ImGuiFont imGuiFont = new ImGuiFont(request.getPath(), font, request.getFontSize());
                    fontCache.put(request.getKey(), imGuiFont);
                    request.complete(imGuiFont);
                } catch (Exception e) {
                    MyWorldTrafficAddition.LOGGER.error("Failed to register font {} with size {}: {}", request.getPath(), request.getFontSize(), e.getMessage());
                }
            }

            fontRequests.clear();
            scheduleFontAtlasRebuild();
        //}).start();
    }

    public static FileSystem.Folder getAvailableFonts() {
        return availableFonts;
    }

    /**
     * Font Request
     * @see FontManager#fontRequests
     */
    private static class FontRequest {
        private final String path;
        private final float fontSize;
        private final CompletableFuture<ImGuiFont> future;

        public FontRequest(String path, float fontSize) {
            this.path = path;
            this.fontSize = fontSize;
            this.future = new CompletableFuture<>();
        }

        public String getPath() {
            return path;
        }

        public float getFontSize() {
            return fontSize;
        }

        public String getKey() {
            return path + ":" + fontSize;
        }

        public CompletableFuture<ImGuiFont> getFuture() {
            return future;
        }

        public void complete(ImGuiFont font) {
            future.complete(font);
        }
    }
}
