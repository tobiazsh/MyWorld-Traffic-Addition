package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OnlineImageCache {

    /**
     * Creates the cache directory for storing custom images.
     * This method should be called during the initialization phase of the mod.
     */
    public static void createCacheDir() {
        ClientCustomImageDir.createCustomImageDir();
    }

    /**
     * Clears all files in the cache directory.
     */
    public static void clearCache() {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {
                    Files.delete(entry);
                }
            }
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to clear cache: {}", e.getMessage());
        }
    }

    /**
     * Removes the specified file from the cache directory.
     * @param fileName The exact name of the file to remove from the cache including its extension.
     */
    public static void removeFromCache(String fileName) {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        Path filePath = cacheDir.resolve(fileName);

        try {
            if (Files.exists(filePath)) Files.delete(filePath);
            else MyWorldTrafficAddition.LOGGER.warn("File {} does not exist in cache.", fileName);
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to remove file from cache: {}", e.getMessage());
        }
    }

    /**
     * Caches an image to the cache directory. Overwrites any existing file with the same name.
     * @param imageBytesPng The byte array of the image (any format).
     * @param imageName The name of the image file to be saved in the cache directory - including the extension!
     */
    public static Path cacheImage(byte[] imageBytesPng, String imageName) {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        Path imagePath = cacheDir.resolve(imageName);

        try {
            Files.write(imagePath, imageBytesPng);
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to cache image: {}", e.getMessage());
        }

        return imagePath;
    }

    public static Path getCachedImagePath(String imageName) {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        return cacheDir.resolve(imageName);
    }

    /**
     * Loads an image from the cache directory.
     * @param imageName The name of the image file to be loaded from the cache directory - including the extension!
     * @return The byte array of the image if it exists, null otherwise.
     */
    public static byte[] loadImage(String imageName) {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        Path imagePath = cacheDir.resolve(imageName);

        if (Files.exists(imagePath)) {
            try {
                return Files.readAllBytes(imagePath);
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load cached image: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * Retrieves a list of UUIDs from the cache directory that match the specified suffix.
     * @param suffix The suffix to filter the cached image files (e.g., ".png" or "_thumbnail.png").
     * @return A list of UUIDs extracted from the cached image file names.
     */
    public static List<UUID> getCachedUUIDs(String suffix) {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        try (var stream = Files.list(cacheDir)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .map(path -> UUID.fromString(path.getFileName().toString().replace(suffix, "").replace(".png", "")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to load cached image UUIDs: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Checks if an image with the specified name is cached.
     * @param imageName The name of the image file to check in the cache directory - including the extension!
     * @return true if the image is cached, false otherwise.
     */
    public static boolean isImageCached(String imageName) {
        Path cacheDir = ClientCustomImageDir.getCacheImageDir();
        Path imagePath = cacheDir.resolve(imageName);
        return Files.exists(imagePath);
    }
}
