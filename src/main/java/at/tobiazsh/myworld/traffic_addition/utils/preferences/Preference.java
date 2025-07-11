package at.tobiazsh.myworld.traffic_addition.utils.preferences;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Preference {

    protected final String CONFIG_FILE_PATH;

    public Preference(String path) {
        this.CONFIG_FILE_PATH = path;
    }

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

    public static final long INVALID_LONG = -200L;

    public void saveToDisk(String key, long value) {
        saveToDisk(key, new JsonPrimitive(value));
    }

    private void saveToDisk(String key, JsonPrimitive value) {
        try {
            createFileIfNotExist();
        } catch (URISyntaxException | IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Error: Could not create config file", e);
        }

        JsonObject content = readConfigFile() == null ? new JsonObject() : readConfigFile();
        content.add(key, value);
        writeConfigFile(content);
    }

    public String getString(String key) {
        JsonPrimitive prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsString();
    }

    public Integer getInt(String key) {
        JsonPrimitive prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsInt();
    }

    public Boolean getBoolean(String key) {
        JsonPrimitive prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsBoolean();
    }

    public Float getFloat(String key) {
        JsonPrimitive prim = loadFromDisk(key);
        return prim == null ? null : prim.getAsFloat();
    }

    public long getLong(String key) {
        JsonPrimitive prim = loadFromDisk(key);
        return prim == null ? INVALID_LONG : prim.getAsLong();
    }

    private JsonPrimitive loadFromDisk(String key) {
        JsonObject content = readConfigFile();

        if (content == null || !content.has(key)) {
            MyWorldTrafficAddition.LOGGER.error("Error: Could not read key from config file");
            return null;
        }

        return content.getAsJsonPrimitive(key);
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
        return configFolder.resolve(CONFIG_FILE_PATH).toFile();
    }

    public void createFileIfNotExist() throws URISyntaxException, IOException {
        File configFile = getConfigFile();

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }
    }

}
