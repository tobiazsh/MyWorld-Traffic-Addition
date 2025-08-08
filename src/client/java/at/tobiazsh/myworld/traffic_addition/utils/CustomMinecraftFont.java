package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.imgui.utils.FontManager;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import at.tobiazsh.myworld.traffic_addition.mixin.client.FontManagerAccessor;
import at.tobiazsh.myworld.traffic_addition.access.client.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomMinecraftFont extends BasicFont {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    public TextRenderer renderer;

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

    public CustomMinecraftFont(String fontPath, TextRenderer renderer) {
        super(fontPath, SPECIAL_FONT_SIZE.MINECRAFT.getSize());
        this.renderer = renderer;
    }

    public CustomMinecraftFont(String fontPath) {
        this(fontPath, loadFont(fontPath));
    }

    public static TextRenderer loadFont(String fontPath) {
        FontManagerAccessor fma = ((FontManagerAccessor) ((MinecraftClientAccessor) client).myworldTrafficAddition$getFontManager());
        AtomicBoolean d = new AtomicBoolean(false);

        TextRenderer tr = new CustomTextRenderer(id -> {
            FontStorage storage = fma.getFontStorages().getOrDefault(Identifier.of(MyWorldTrafficAddition.MOD_ID, fontPath), fma.getFontStorages().getOrDefault(Identifier.of("default"), fma.getMissingStorage()));
            if (storage == fma.getFontStorages().get(Identifier.of("default"))) {
                d.set(true);
            }
            return storage;
        }, true);

        if (d.get()) {
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

    public static TextRenderer getTextRendererByPath(String path) {
        String name = normalizeFontPath(path);
        return loadedFonts.stream()
                .filter(font -> font.getFontPath().equals(name))
                .findFirst()
                .map(font -> font.renderer)
                .orElse(null);
    }
}
