package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FontRepository {

    private static final String FONTS_FOLDER = "/assets/" + MyWorldTrafficAddition.MOD_ID + "/font/";

    private static final FontRepository INSTANCE = new FontRepository();

    private final List<FontDefinition> fonts = new ArrayList<>();
    private FontDefinition fallbackFont;
    public static final int DEFAULT_FONT_SIZE = 24;

    public static FontRepository getInstance() {
        return INSTANCE;
    }

    public void discover() {
        discoverJar();

        if (this.fallbackFont == null) {
            throw new RuntimeException("Failed to discover a fallback font! Cannot continue without a fallback font as " +
                    "unsafe behavior may occur.");
        }
    }

    /**
     * Discovers fonts in the JAR file under the FONTS_FOLDER path and adds them to the fonts list.
     * Each font is expected to have a corresponding .json and .ttf file with the same name.
     * If no fonts are found, a warning is logged.
     */
    private void discoverJar() {
        int foundFonts = 0;

        FileSystem.Folder jsons;
        //noinspection SpellCheckingInspection
        FileSystem.Folder ttfs;

        try {
            FileSystem.Folder fontsFolder = FileSystem.listFilesRecursive(FONTS_FOLDER, true);

            if (fontsFolder == null) {
                MyWorldTrafficAddition.LOGGER.info("No fonts found in JAR at " + FONTS_FOLDER);
                return;
            }

            fontsFolder.concentrateFiles();

            jsons = fontsFolder.clone().concentrateFileType("json");
            ttfs = fontsFolder.concentrateFileType("ttf");
        } catch (IOException | URISyntaxException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to discover fonts in JAR! Using fallback font!", e);
            return;
        }

        if (jsons.content.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.warn(
                    "No JSON font definitions found in JAR at {}. Every font will be the fallback font!" +
                    " This is not normal behaviour! Maybe you have a modified version of the mod?",
                    FONTS_FOLDER
            );

            return;
        }

        for (FileSystem.DirectoryElement json : jsons) {
            if (!(json instanceof FileSystem.File jsonFile)) {
                MyWorldTrafficAddition.LOGGER.warn("Skipping non-file JSON element: {}", json.path);
                continue;
            }

            String ttfFilename = jsonFile.name.replaceFirst("\\.json$", ".ttf");
            FileSystem.File ttf = ttfs.getByFilename(ttfFilename);

            if (ttf == null) {
                MyWorldTrafficAddition.LOGGER.warn("No matching TTF file found for JSON: {}", jsonFile.path);
                continue;
            }

            JarFontDefinition fontDef;

            try {
                fontDef = new JarFontDefinition(
                        ttf.path,
                        jsonFile.name,
                        new String(jsonFile.readBytes(), StandardCharsets.UTF_8),
                        LogicalFont.ofFile(ttf)
                );
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to hash font file: {}", ttf.path, e);
                continue;
            }

            // Use first discovered font marked as "default" as the fallback font.
            // If multiple fonts are marked as default, log a warning and use the first one.
            if (fontDef.getOptions().isDefault() && this.fallbackFont == null) {
                MyWorldTrafficAddition.LOGGER.info(
                        "Using default font: {} (hash: {})",
                        fontDef.getDisplayName(),
                        fontDef.getLogicalFont().fontHash()
                );

                this.fallbackFont = fontDef;
            } else if (fontDef.getOptions().isDefault()) {
                MyWorldTrafficAddition.LOGGER.warn(
                        "Multiple fonts marked as default found! Using first discovered font as fallback: {} (hash: {})",
                        this.fallbackFont.getDisplayName(),
                        this.fallbackFont.getLogicalFont().fontHash()
                );
            }

            fonts.add(fontDef);
            foundFonts++;
        }

        MyWorldTrafficAddition.LOGGER.info("Discovered {} fonts in JAR at {}", foundFonts, FONTS_FOLDER);
    }

    private void discoverCached() {

    }

    public List<FontDefinition> getFonts() {
        return List.copyOf(fonts);
    }

    /**
     * Returns the first font definition that matches the given hash. If no matching font is found, returns null.
     *
     * @param hash The hash of the font to search for.
     * @return The matching FontDefinition, or null if not found.
     */
    public @Nullable FontDefinition getByHash(String hash) {
        for (FontDefinition font : fonts) {
            if (font.getLogicalFont() != null && font.getLogicalFont().fontHash().equals(hash)) {
                return font;
            }
        }

        return null;
    }

    /**
     * Returns a set of font definitions that match the given list of hashes.
     * If no matching fonts are found, returns an empty set.
     *
     * <p>If a font is not found, it is not included in the list.</p>
     * @param hashes The list of hashes to search for.
     * @return A set of matching FontDefinitions, or an empty set if none are found.
     */
    public Map<String, @Nullable FontDefinition> getByHashes(List<String> hashes) {
        Map<String, FontDefinition> searchResult = new HashMap<>();

        for (String hash : hashes) {
            FontDefinition font = getByHash(hash);
            searchResult.put(hash, font);
        }

        return searchResult;
    }

    /**
     * Returns the fallback font definition, or null if fonts have not been discovered yet.
     */
    public @Nullable FontDefinition getFallbackFont() {
        return fallbackFont;
    }
}
