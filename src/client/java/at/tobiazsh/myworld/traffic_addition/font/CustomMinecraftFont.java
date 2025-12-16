package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.FontManager;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import at.tobiazsh.myworld.traffic_addition.mixin.client.font.FontManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomMinecraftFont extends BasicFont {

    private static final Minecraft client = Minecraft.getInstance();
    public Font renderer;

    public static List<CustomMinecraftFont> loadedFonts = new ArrayList<>();
    public static final List<String> availableFonts = new ArrayList<>();

    static {
        FileSystem.Folder fontsFolder = FontManager.getAvailableFonts();

        if (fontsFolder.content.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.error("No fonts found in /assets/" + MyWorldTrafficAddition.MOD_ID + "/font/");
        }

        for (FileSystem.DirectoryElement font : fontsFolder) {
            availableFonts.add(font.path.replaceFirst("/assets/" + MyWorldTrafficAddition.MOD_ID + "/", ""));
        }
    }

    public CustomMinecraftFont(String fontPath, Font renderer) {
        super(fontPath, SPECIAL_FONT_SIZE.MINECRAFT.getSize());
        this.renderer = renderer;
    }

    public CustomMinecraftFont(String fontPath) {
        this(fontPath, loadFont(fontPath));
    }

    public static Font loadFont(String fontPath) {
        FontManagerAccessor fma = ((FontManagerAccessor) ((MinecraftClientAccessor) client).myworldTrafficAddition$getFontManager());
        AtomicBoolean isDefault = new AtomicBoolean(false);

        Font.Provider provider = new Font.Provider() {
            private FontSet pickStorage() {
                Identifier id = Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, fontPath);
                FontSet storage = fma.getFontSets().getOrDefault(id, fma.getMissingFontSet());

                if (storage == fma.getMissingFontSet()) {
                    isDefault.set(true);
                }
                return storage;
            }

            @Override
            public GlyphSource glyphs(FontDescription source) {
                FontSet storage = pickStorage();
                return storage.source(true);
            }

            @Override
            public EffectGlyph effect() {
                FontSet storage = pickStorage();
                return storage.whiteGlyph();
            }
        };

        Font tr = new CustomTextRenderer(provider);

        if (isDefault.get()) {
            MyWorldTrafficAddition.LOGGER.error("Error initializing TTF renderer, defaulting to Minecraft font");
        } else {
            loadedFonts.add(new CustomMinecraftFont(fontPath, tr));
        }

        return tr;
    }

    /**
     * Will load all fonts the program has found in /assets/mod_id/font/
     */
    public static void loadAllAvailableFonts() {
        availableFonts.forEach(font -> CustomMinecraftFont.loadFont(normalizeFontPath(font)));
    }

    /**
     * Will load a list of fonts
     * @param fontPaths The list of the paths to the font
     */
    public static List<CustomMinecraftFont> loadFontList(List<String> fontPaths) {
        List<CustomMinecraftFont> fonts = new ArrayList<>();

        for (String fontPath : fontPaths) {
            fonts.add(new CustomMinecraftFont(fontPath, loadFont(fontPath)));
        }

        return fonts;
    }

    public static void initFonts() {
        loadAllAvailableFonts();
    }

    public static String normalizeFontPath(String fontPath) {
        return fontPath.substring(fontPath.lastIndexOf("/") + 1, fontPath.lastIndexOf("."));
    }

    public static Font getTextRendererByPath(String path) {
        String name = normalizeFontPath(path);
        return loadedFonts.stream()
                .filter(font -> font.getFontPath().equals(name))
                .findFirst()
                .map(font -> font.renderer)
                .orElse(null);
    }
}
