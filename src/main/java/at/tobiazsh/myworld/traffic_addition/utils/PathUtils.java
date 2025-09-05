package at.tobiazsh.myworld.traffic_addition.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.MOD_RESOURCES;

public class PathUtils {

    @Contract(pure = true)
    public static @NotNull Path relativizeResourcePath(@NotNull Path absoluteResourcePath) {
        Path normalizedPath = Path.of(absoluteResourcePath.toString().replaceAll("\\\\", "/"));
        return MOD_RESOURCES.relativize(absoluteResourcePath);
    }

    @Contract(pure = true)
    public static @NotNull String windowsToUnixPath(@NotNull String path) {
        return path.replaceAll("\\\\", "/");
    }

}
