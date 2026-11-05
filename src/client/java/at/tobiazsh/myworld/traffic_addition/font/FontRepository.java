package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FontRepository {

    private static final String FONTS_FOLDER = "/assets/" + MyWorldTrafficAddition.MOD_ID + "/font/";

    private static final FontRepository INSTANCE = new FontRepository();

    private final List<FontDefinition> fonts = new ArrayList<>();

    public static FontRepository getInstance() {
        return INSTANCE;
    }

    public void discover() {

    }

    /**
     * Discovers fonts in the JAR file under the FONTS_FOLDER path and adds them to the fonts list.
     * Each font is expected to have a corresponding .json and .ttf file with the same name.
     * If no fonts are found, a warning is logged.
     */
    private void discoverJar() {
        int foundFonts = 0;

        try {
            FileSystem.Folder fontsFolder = FileSystem.listFilesRecursive(FONTS_FOLDER, true);

            if (fontsFolder == null) {
                MyWorldTrafficAddition.LOGGER.info("No fonts found in JAR at " + FONTS_FOLDER);
                return;
            }

            fontsFolder.concentrateFiles();

            FileSystem.Folder jsons = fontsFolder.clone().concentrateFileType(".json");
            FileSystem.Folder ttfs = fontsFolder.concentrateFileType(".ttf");

            for (FileSystem.DirectoryElement json : jsons) {
                if (!(json instanceof FileSystem.File jsonFile)) {
                    MyWorldTrafficAddition.LOGGER.warn("Skipping non-file JSON element: {}", json.path);
                    continue;
                }

                FileSystem.File ttf = ttfs.getByFilename(jsonFile.name);

                if (ttf == null) {
                    MyWorldTrafficAddition.LOGGER.warn("No matching TTF file found for JSON: {}", jsonFile.path);
                    continue;
                }

                var fontDef = new JarFontDefinition(
                        ttf.path,
                        jsonFile.name,
                        new String(jsonFile.readBytes(), StandardCharsets.UTF_8)
                );

                fonts.add(fontDef);
                foundFonts++;
            }
        } catch (IOException | URISyntaxException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to discover fonts in JAR! Using fallback font!", e);
        }

        if (foundFonts <= 0) {
            MyWorldTrafficAddition.LOGGER.warn("No fonts found in JAR at {}. Every font will be the fallback font!\n" +
                    "This is not normal behaviour. Maybe you have a modified version of the mod?", FONTS_FOLDER);
        }

        MyWorldTrafficAddition.LOGGER.info("Discovered {} fonts in JAR at {}", foundFonts, FONTS_FOLDER);
    }

    public List<FontDefinition> getFonts() {
        return List.copyOf(fonts);
    }
}
