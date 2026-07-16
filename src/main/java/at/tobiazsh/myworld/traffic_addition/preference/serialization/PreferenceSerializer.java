package at.tobiazsh.myworld.traffic_addition.preference.serialization;

import at.tobiazsh.myworld.traffic_addition.preference.Preference;
import at.tobiazsh.myworld.traffic_addition.preference.annotation.*;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public class PreferenceSerializer {

    /**
     * Serializes a root PreferenceNode to TOML
     * @param root PreferenceNode to serialize
     * @return String containing the TOML
     */
    public static String serializeToToml(PreferenceNode root) {
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
     * @param rootInstance The class structure to deserialize into, must be annotated with @PreferenceRoot.
     *                     See {@link PreferenceSerializer#scan(Object) for more information}
     * @return Deserialized class
     * @param <T> The type of class to deserialize into
     */
    public static <T> T deserializeFromToml(String serialized, T rootInstance) {
        JToml toml = JToml.jToml();
        TomlTable table = toml.readFromString(serialized).asTable();
        Map<TomlKey, TomlValue> flat = table.toMap();

        PreferenceNode root = scan(rootInstance);
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

    /**
     * Scans a class annotated with @PreferenceRoot and its nested children annotated with @PreferenceChild,
     * building a tree of PreferenceNode objects representing the structure of preferences.
     * @param root The root class to scan, which must be annotated with @PreferenceRoot.
     * @return The root {@link PreferenceNode} representing the scanned structure.
     */
    public static PreferenceNode scan(Object root) {
        Class<?> clazz = root.getClass();

        if (!clazz.isAnnotationPresent(PreferenceRoot.class)) {
            throw new IllegalArgumentException("Missing @PreferenceRoot annotation on class: " + clazz.getName());
        }

        return scanNode(null, root);
    }

    private static PreferenceNode scanNode(@Nullable Field field, Object instance) {
        Class<?> clazz = instance.getClass();

        String id = resolveId(field, clazz);

        PreferenceNode node = new PreferenceNode(id, instance);

        for (Field childField : clazz.getFields()) {
            Object value = get(childField, instance);

            if (value == null)
                continue;

            if (value instanceof Preference<?> preference) {
                node.preferences().put(
                        preference.getId(),
                        preference
                );
                continue;
            }

            if (isPreferenceNode(value.getClass())) {
                PreferenceNode childNode = scanNode(childField, value);

                node.children().put(
                        childNode.id(),
                        childNode
                );
            }
        }

        return node;
    }

    /**
     * Returns the id of a given field or class. If an annotation with a child class contains a value,
     * the value is being used as an id, otherwise the field name is being used.
     * @param field The declared object field, which may be null if the id is being resolved for the root class.
     * @param clazz The class of the object for which the id is being resolved.
     */
    private static String resolveId(@Nullable Field field, Class<?> clazz) {
        PreferenceChild child = clazz.getAnnotation(PreferenceChild.class);

        if (child != null && !child.value().isBlank())
            return child.value();

        if (field != null)
            return field.getName();

        return clazz.getSimpleName();
    }

    private static Object get(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }

    private static boolean isPreferenceNode(Class<?> clazz) {
        return clazz.isAnnotationPresent(PreferenceRoot.class)
                || clazz.isAnnotationPresent(PreferenceChild.class);
    }
}
