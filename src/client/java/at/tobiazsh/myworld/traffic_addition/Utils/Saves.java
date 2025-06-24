package at.tobiazsh.myworld.traffic_addition.Utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

public class Saves {

    private static final String SAVES_DIR = "myworld_traffic_addition/saves";
    private static final String SIGN_SAVES_SUBDIR = "Signs";
    private static final String ELEMENTS_SAVES_SUBDIR = "Elements";

    private static Path getSavesDir() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve(SAVES_DIR);
    }

    public static Path getSignSaveDir() {
        return getSavesDir().resolve(SIGN_SAVES_SUBDIR);
    }

    public static Path getElementSaveDir() {
        return getSavesDir().resolve(ELEMENTS_SAVES_SUBDIR);
    }

    /**
     * Creates the saves directory and its subdirectories if they don't exist. Otherwise, do nothing.
     */
    public static void createSavesDir() {
        File signSavesDir = getSignSaveDir().toFile();
        File elementsSavesDir = getElementSaveDir().toFile();

        if (!signSavesDir.exists()) {
            if (signSavesDir.mkdirs())
                MyWorldTrafficAddition.LOGGER.debug("Created Saves Directory for Signs successfully! View at {}", signSavesDir.getAbsolutePath());
            else
                MyWorldTrafficAddition.LOGGER.error("Failed to create Saves Directory for Signs! Directory probably already exists! View at {}", signSavesDir.getAbsolutePath());
        }

        if (!elementsSavesDir.exists()) {
            if (elementsSavesDir.mkdirs())
                MyWorldTrafficAddition.LOGGER.debug("Created Saves Directory for Elements successfully! View at {}", elementsSavesDir.getAbsolutePath());
            else
                MyWorldTrafficAddition.LOGGER.error("Failed to create Saves Directory for Elements! Directory probably already exists! View at {}", elementsSavesDir.getAbsolutePath());
        }
    }

}
