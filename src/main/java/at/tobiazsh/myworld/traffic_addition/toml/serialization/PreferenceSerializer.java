package at.tobiazsh.myworld.traffic_addition.toml.serialization;

import at.tobiazsh.myworld.traffic_addition.preference.Preference;
import at.tobiazsh.myworld.traffic_addition.toml.TomlNode;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.util.Map;

public class PreferenceSerializer {

    /**
     * Serializes a root PreferenceNode to TOML
     * @param root PreferenceNode to serialize
     * @return String containing the TOML
     */
    public static String serializeToToml(TomlNode<Preference<?>> root) {
        JToml toml = JToml.jToml();
        TomlTable table = TomlTable.create();

        Map<String, Preference<?>> compiled = root.compile();

        for (Map.Entry<String, Preference<?>> entry : compiled.entrySet()) {
            String id = entry.getKey();
            Preference<?> preference = entry.getValue();
            TomlValue value = preference.getValueSerialized();
            table.put(id, value);
        }

        return toml.writeToString(table);
    }

    /**
     * Deserializes a TOML-String to a class structure using PreferenceNodes
     * @param serialized The serialized String
     * @param rootInstance The class structure to deserialize into, must be annotated with @TomlRoot.
     *                     See {@link TomlScanner#scan(Object) for more information}
     * @return Deserialized class
     * @param <T> The type of class to deserialize into
     */
    public static <T> T deserializeFromToml(String serialized, T rootInstance) {
        JToml toml = JToml.jToml();
        TomlTable table = toml.readFromString(serialized).asTable();
        Map<TomlKey, TomlValue> flat = table.toMap();

        TomlNode<Preference<?>> root = Preference.SCANNER.scan(rootInstance);
        Map<String, Preference<?>> compiled = root.compile();

        for (Map.Entry<TomlKey, TomlValue> entry : flat.entrySet()) {
            String key = entry.getKey().toString();
            Preference<?> preference = compiled.get(key);

            if (preference == null) {
                // No matching field for this TOML key — ignore stale/unknown entries
                continue;
            }

            applyValue(preference, entry.getValue());
        }

        return rootInstance;
    }

    /**
     * Applies a TomlValue to a Preference using Codecs
     * @param preference The preference to apply the value to
     * @param value The value to apply
     * @param <T> The type of the preference
     */
    private static <T> void applyValue(Preference<T> preference, TomlValue value) {
        T decoded = preference.getCodec().deserialize(value);
        preference.set(decoded);
    }
}
