package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

// Kept as legacy conversion system
@Deprecated(forRemoval = false, since = "1.9.0")
public record PreferenceJsonLoader(String configFilePath) {

    public void saveToDisk(String key, String value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    public void saveToDisk(String key, int value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    public void saveToDisk(String key, boolean value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    public void saveToDisk(String key, float value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    public void saveToDisk(String key, long value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    public void saveToDisk(String key, short value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    public void saveToDisk(String key, JsonElement value) {
        try {
            createFileIfNotExist();
        } catch (URISyntaxException | IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Error: Could not create config file", e);
        }

        JsonObject content = readConfigFile() == null ? new JsonObject() : readConfigFile();

        if (content == null) {
            content = new JsonObject(); // Fallback in case reading fails
        }

        content.add(key, value);
        writeConfigFile(content);
    }

    @Nullable
    public String getString(String key) {
        JsonElement prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsString();
    }

    @Nullable
    public Integer getInt(String key) {
        JsonElement prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsInt();
    }

    @Nullable
    public Short getShort(String key) {
        JsonElement prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsShort();
    }

    @Nullable
    public Boolean getBoolean(String key) {
        JsonElement prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsBoolean();
    }

    @Nullable
    public Float getFloat(String key) {
        JsonElement prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsFloat();
    }

    @Nullable
    public Long getLong(String key) {
        JsonElement prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsLong();
    }

    @Nullable
    public JsonElement getJsonElement(String key) {
        return loadFromDisk(key);
    }

    @Nullable
    public JsonObject getJsonObject(String key) {
        JsonElement content = loadFromDisk(key);

        if (content != null && content.isJsonObject()) {
            return content.getAsJsonObject();
        }

        return null;
    }

    @Nullable
    public JsonArray getJsonArray(String key) {
        JsonElement array = loadFromDisk(key);

        if (array != null && array.isJsonArray()) {
            return array.getAsJsonArray();
        }

        return null;
    }

    /**
     * Loads the given key from the config file on disk
     * @param key Key to load
     * @return JsonObject containing the value, or null if not found
     */
    private JsonElement loadFromDisk(String key) {
        JsonObject content = readConfigFile();

        if (content == null || !content.has(key)) {
            MyWorldTrafficAddition.LOGGER.debug("Could not read key \"{}\" from config file", key);
            return null;
        }

        return content.get(key);
    }

    private JsonObject readConfigFile() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return null;
        }

        String content = null;

        try {
            content = Files.readString(configFile.toPath().toAbsolutePath());
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Error: Could not read config file", e);
        }

        if (content == null || content.isEmpty()) {
            return null;
        }

        return JsonParser.parseString(content).getAsJsonObject();
    }

    private void writeConfigFile(JsonObject content) {
        File configFile = getConfigFile();

        try {
            Files.writeString(configFile.toPath().toAbsolutePath(), content.toString());
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Error: Could not write config file", e);
        }
    }

    private File getConfigFile() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().toAbsolutePath();
        return configFolder.resolve(configFilePath).toFile();
    }

    /**
     * Creates preferences file along parent directories if it does not exist
     * @throws URISyntaxException If URI has faulty syntax
     * @throws IOException If path could not be read or written
     */
    public void createFileIfNotExist() throws URISyntaxException, IOException {
        File configFile = getConfigFile();

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }
    }

}
