package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerFontDiscovery {

    private final static Path FONT_DIRECTORY = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(MyWorldTrafficAddition.MOD_ID)
            .resolve("server_fonts")
            .toAbsolutePath();

    private final static File DEFAULT_DEFINITION_PATH = FONT_DIRECTORY.resolve("default_font.json").toFile();

    private ServerFontDiscovery() {
    }

    private static final ServerFontDiscovery INSTANCE = new ServerFontDiscovery();

    public static ServerFontDiscovery getInstance() {
        return INSTANCE;
    }

    public void discover() {
        discoverDirectory();
    }

    /**
     * Discover fonts in the server's font directory.
     */
    private void discoverDirectory() {
        FileSystem.Folder fontsFolder;

        if (!Files.exists(FONT_DIRECTORY)) {
            try {
                Files.createDirectories(FONT_DIRECTORY);
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to create server font directory: {}", FONT_DIRECTORY, e);
                return;
            }
        }

        try {
            fontsFolder = FileSystem.listAllRecursive(FONT_DIRECTORY.toString(), false);
        } catch (IOException | URISyntaxException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to discover fonts in server font directory: {}", FONT_DIRECTORY, e);
            return;
        }

        if (fontsFolder.content.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.info("No fonts found in server font directory: {}", FONT_DIRECTORY);
            return;
        }

        fontsFolder.concentrateFiles(); // Move all files to the root of the folder structure.
        fontsFolder.concentrateFileType(".ttf"); // No JSON files necessary here. Only the client needs to know stuff like oversample, shift, etc...

        for (FileSystem.DirectoryElement element : fontsFolder.content) {
            if (!(element instanceof FileSystem.File fontFile)) continue; // Make java happy

            try {
                addEntry(LogicalFont.ofFile(fontFile));
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to read font file: {}", fontFile.path, e);
            }
        }
    }

    private void addEntry(LogicalFont font) {
        // Add to allowlist
        FontGlossary.getInstance().meetFont(font);
    }

}
