package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

public class TempDirectory {

    private static final String TEMP_DIR = "myworld_traffic_addition/temp";

    /**
     * Create the temp directory if it doesn't exist. Otherwise, do nothing.
     */
    public static void createTempDir() {
        File tempDir = getTempDir().toFile();

        if (!tempDir.exists()) {
            if (tempDir.mkdirs())
                MyWorldTrafficAddition.LOGGER.debug("Created Temp Directory successfully! View at {}", tempDir.getAbsolutePath());
            else
                MyWorldTrafficAddition.LOGGER.error("Failed to create Temp Directory! Directory probably already exists! View at {}", tempDir.getAbsolutePath());
        }
    }

    public static Path getTempDir() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve(TEMP_DIR);
    }
}
