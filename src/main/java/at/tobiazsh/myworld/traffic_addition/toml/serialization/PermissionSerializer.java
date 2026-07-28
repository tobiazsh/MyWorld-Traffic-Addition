package at.tobiazsh.myworld.traffic_addition.toml.serialization;

import at.tobiazsh.myworld.traffic_addition.exception.PermissionExistsException;
import at.tobiazsh.myworld.traffic_addition.permission.Permission;
import at.tobiazsh.myworld.traffic_addition.toml.TomlNode;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.util.Map;
import java.util.UUID;

/**
 * Serializer for Permission objects to TOML format.
 * Uses reflection to scan for Permission fields annotated with @TomlRoot and @TomlChild.
 */
public class PermissionSerializer {

    /**
     * Serializes a root PermissionNode to TOML
     * @param root PermissionNode to serialize
     * @return String containing the TOML
     */
    public static String serializeToToml(TomlNode<Permission<?>> root) {
        JToml toml = JToml.jToml();
        TomlTable mainTable = TomlTable.create();

        Map<String, Permission<?>> compiled = root.compile();

        for (Map.Entry<String, Permission<?>> entry : compiled.entrySet()) {
            String id = entry.getKey();
            Permission<?> permission = entry.getValue();

            TomlTable permissionTable = serializePermissionEntries(permission);
            mainTable.put(id, permissionTable);
        }

        return toml.writeToString(mainTable);
    }

    /**
     * Deserializes a TOML-String to a class structure using PermissionNodes
     * @param serialized The serialized String
     * @param rootInstance The class structure to deserialize into, must be annotated with @TomlRoot.
     * @return Deserialized class
     * @param <T> The type of class to deserialize into
     */
    public static <T> T deserializeFromToml(String serialized, T rootInstance) {
        JToml toml = JToml.jToml();
        TomlTable table = toml.readFromString(serialized).asTable();

        TomlNode<Permission<?>> root = Permission.SCANNER.scan(rootInstance);
        Map<String, Permission<?>> compiled = root.compile();

        for (Map.Entry<String, Permission<?>> entry : compiled.entrySet()) {
            String permissionId = entry.getKey();
            Permission<?> permission = entry.getValue();

            TomlValue permissionValue = table.get(permissionId);
            if (permissionValue == null || !permissionValue.isTable()) {
                // no matching entry for this permission; skip
                continue;
            }

            applyPermissionEntries(permission, permissionValue.asTable());
        }

        return rootInstance;
    }

    /**
     * Serializes all entries of a permission (UUID -> Value mappings) to a TOML table
     * @param permission The permission to serialize
     * @return A TOML table with UUID keys and serialized values
     */
    private static <T> TomlTable serializePermissionEntries(Permission<T> permission) {
        TomlTable table = TomlTable.create();
        Map<UUID, T> entries = permission.getEntries();

        for (Map.Entry<UUID, T> entry : entries.entrySet()) {
            String uuidString = entry.getKey().toString();
            T value = entry.getValue();
            TomlValue serializedValue = permission.getCodec().serialize(value);
            table.put(uuidString, serializedValue);
        }

        return table;
    }

    /**
     * Applies TOML table entries to a Permission, deserializing UUID -> Value mappings
     * @param permission The permission to apply values to
     * @param table The TOML table with UUID keys
     */
    private static <T> void applyPermissionEntries(Permission<T> permission, TomlTable table) {
        for (Map.Entry<?, TomlValue> entry : table.toMap().entrySet()) {
            String uuidString = entry.getKey().toString();
            TomlValue value = entry.getValue();

            try {
                UUID playerUuid = UUID.fromString(uuidString);
                T deserialized = permission.getCodec().deserialize(value);
                try {
                    permission.allow(playerUuid, deserialized, true);
                } catch (PermissionExistsException e) {
                    // Ignore, we're overwriting anyway
                }
            } catch (IllegalArgumentException e) {
                // Invalid UUID format — skip this entry
            }
        }
    }
}

