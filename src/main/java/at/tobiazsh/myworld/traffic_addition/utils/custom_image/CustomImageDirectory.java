package at.tobiazsh.myworld.traffic_addition.utils.custom_image;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

public class CustomImageDirectory {

    private static final String CUSTOM_IMAGE_DIR = "myworld_traffic_addition/uploaded_images";
    private static final String HIDDEN_CUSTOM_IMAGE_DIR = "myworld_traffic_addition/uploaded_images/hidden";

    /**
     * Create the temp directory if it doesn't exist. Otherwise, do nothing.
     */
    public static void createCustomImageDir() {
        File hiddenImageDir = getHiddenCustomImageDir().toFile();

        if (!hiddenImageDir.exists()) {
            if (hiddenImageDir.mkdirs())
                MyWorldTrafficAddition.LOGGER.debug("Created Custom Image Directory successfully! View at {}", hiddenImageDir.getAbsolutePath());
            else
                MyWorldTrafficAddition.LOGGER.error("Failed to create Custom Image Directory! Directory probably already exists! View at {}", hiddenImageDir.getAbsolutePath());
        }
    }

    public static Path getCustomImageDir() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve(CUSTOM_IMAGE_DIR);
    }

    public static Path getHiddenCustomImageDir() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve(HIDDEN_CUSTOM_IMAGE_DIR);
    }
}
