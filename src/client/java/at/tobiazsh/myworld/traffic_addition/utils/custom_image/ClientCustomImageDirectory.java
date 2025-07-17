package at.tobiazsh.myworld.traffic_addition.utils.custom_image;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

// Stores json of all the images the player uploaded
public class ClientCustomImageDirectory {
    private static final String MY_IMAGE_DIR = "myworld_traffic_addition/custom_images/mine";
    private static final String CACHE_IMAGE_DIR = "myworld_traffic_addition/custom_images/cache";

    public static void createCustomImageDir() {
        File cacheImageDir = getCacheImageDir().toFile();
        File myImageDir = getMyImageDir().toFile();

        if (!cacheImageDir.exists()) {
            if (cacheImageDir.mkdirs())
                MyWorldTrafficAddition.LOGGER.info("Created custom image directory: {}", cacheImageDir.getAbsolutePath());
            else
                MyWorldTrafficAddition.LOGGER.error("Failed to create custom image directory: {}", cacheImageDir.getAbsolutePath());
        }

        if (!myImageDir.exists()) {
            if (myImageDir.mkdirs())
                MyWorldTrafficAddition.LOGGER.info("Created custom image directory: {}", myImageDir.getAbsolutePath());
            else
                MyWorldTrafficAddition.LOGGER.error("Failed to create custom image directory: {}", myImageDir.getAbsolutePath());
        }
    }

    public static Path getCacheImageDir() {
        return FabricLoader.getInstance().getGameDir().resolve(CACHE_IMAGE_DIR);
    }

    public static Path getMyImageDir() {
        return FabricLoader.getInstance().getGameDir().resolve(MY_IMAGE_DIR);
    }
}
