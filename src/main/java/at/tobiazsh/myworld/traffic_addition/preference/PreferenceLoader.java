package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.serialization.PreferenceSerializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PreferenceLoader {

    /**
     * Tries to load a preference class from TOML in the specified location, otherwise resorts to default value and
     * invokes callback.
     *
     * @param file The file to load the TOML from.
     * @param clazz The class to deserializes the TOML into.
     * @param defaultValueSupplier The value to return if an exception is caught.
     * @param onFallbackCallback Message supplier containing the error message. Can be null if logging is irrelevant.
     * @return The deserialized preference object.
     * @param <T> The type of preference class.
     */
    public static <T extends PreferenceHierarchy> T loadPreferenceFromFileOrDefault(
            File file,
            T clazz,
            Supplier<T> defaultValueSupplier,
            @Nullable Consumer<String> onFallbackCallback
    ) {
        try {
            return loadPreferencesFromFile(file, clazz);
        } catch (IOException e) {
            if (onFallbackCallback != null) {
                onFallbackCallback.accept(
                        "Failed to load preferences from file: "
                                + file.getAbsolutePath() +
                                ". Falling back to default preferences."
                );
            }

            return defaultValueSupplier.get();
        }
    }

    /**
     * Tries to save the preference class as TOML in the specified location, otherwise invokes callback.
     *
     * @param file The location to save the preferences.
     * @param clazz The class to deserializes and save.
     * @param onErrorCallback Message supplier containing the error message. Can be null if logging is irrelevant.
     * @param <T> The type of preference class.
     */
    public static <T extends PreferenceHierarchy> void savePreferenceToFileOrCallback(
            File file,
            T clazz,
            @NonNull Consumer<String> onErrorCallback
    ) {
        try {
            savePreferencesToFile(file, clazz);
        } catch (IOException e) {
            onErrorCallback.accept("Failed to save preferences to: " + file.getAbsolutePath());
        }
    }

    /**
     * Loads a preference class from TOML in the specified location.
     *
     * @param file The file to load the TOML from.
     * @param clazz The clazz to deserialize the TOML into.
     * @return The deserialized preference object.
     * @param <T> The type of preference class.
     * @throws IOException In case something file-related goes wrong.
     */
    public static <T extends PreferenceHierarchy> T loadPreferencesFromFile(File file, T clazz)
            throws IOException
    {
        String read = Files.readString(Path.of(file.getAbsolutePath()));
        return PreferenceSerializer.deserializeFromToml(read, clazz);
    }

    /**
     * Saves a preference class to a file in the specified location in the TOML format.
     *
     * @param file The file location to save the preference class to.
     * @param clazz The class to serialize.
     * @param <T> The type of preference class.
     * @throws IOException In case something file-related goes wrong.
     */
    public static <T extends PreferenceHierarchy> void savePreferencesToFile(File file, T clazz)
            throws IOException
    {
        String toml = PreferenceSerializer.serializeToToml(Preference.SCANNER.scan(clazz)).trim();
        Files.write(Path.of(file.getAbsolutePath()), toml.getBytes());
    }

}
