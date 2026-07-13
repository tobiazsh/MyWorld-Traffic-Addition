package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.exception.PreferenceReadException;
import at.tobiazsh.myworld.traffic_addition.exception.PreferenceWriteException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

import java.io.File;
import java.io.IOException;

public class PreferenceLoader {

    /**
     * Loads a TOML file from storage and tries to deserialize to any class of type PreferenceHierarchy.
     * @param file The file to deserialize
     * @return The serialized PreferenceHierarchy class
     * @param <P> The type of PreferenceHierarchy class
     * @throws PreferenceReadException If the file could not be read or deserialized
     */
    public static <P extends PreferenceHierarchy> P load(File file, Class<P> preferenceClass) throws PreferenceReadException {
        TomlMapper mapper = new TomlMapper();

        try {
            return mapper.readValue(file, preferenceClass);
        } catch (IOException e) {
            throw new PreferenceReadException(
                    String.format("Failed to read preferences from %s", file.getAbsolutePath()),
                    e
            );
        }
    }

    /**
     * Serializes and saves a class of type PreferenceHierarchy to a file in the TOML format.
     * @param file The TOML file to save it to
     * @param preferences The PreferenceHierarchy class
     * @param <P> The type of PreferenceHierarchy class
     * @throws PreferenceWriteException If the file could not be written or serialized
     */
    public static <P extends PreferenceHierarchy> void save(File file, P preferences) throws PreferenceWriteException {
        TomlMapper mapper = new TomlMapper();

        try {
            mapper.writeValue(file, preferences);
        } catch (IOException e) {
            throw new PreferenceWriteException("Failed to serialize preferences", e);
        }
    }

}
