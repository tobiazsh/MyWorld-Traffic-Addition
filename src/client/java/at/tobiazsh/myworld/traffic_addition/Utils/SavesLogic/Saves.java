package at.tobiazsh.myworld.traffic_addition.Utils.SavesLogic;

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

    public static void createSavesDirIfNonExistent() {
        File signSavesDir = getSignSaveDir().toFile();
        File elementsSavesDir = getElementSaveDir().toFile();

        if (!signSavesDir.exists())
            signSavesDir.mkdirs();

        if (!elementsSavesDir.exists())
            elementsSavesDir.mkdirs();
    }

}
