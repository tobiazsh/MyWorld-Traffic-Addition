package at.tobiazsh.myworld.traffic_addition.toml;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TomlNode<T> {
    private final String id;
    private final Object instance;

    private final Map<String, TomlNode<T>> children = new HashMap<>();
    private final Map<String, T> entries = new HashMap<>();

    public TomlNode(String id, Object instance) {
        this.id = id;
        this.instance = instance;
    }

    public String id() {
        return id;
    }

    public Map<String, TomlNode<T>> children() {
        return children;
    }

    public Map<String, T> entries() {
        return entries;
    }

    /**
     * Compiles a Map of preferences with their ID. Sub-IDs are separated by a dot as per TOML standard.
     */
    public Map<String, T> compile() {
        Map<String, T> map = new HashMap<>(entries);

        for (Map.Entry<String, TomlNode<T>> entry : children.entrySet()) {
            String childId = entry.getKey();
            TomlNode<T> childNode = entry.getValue();

            Map<String, T> childPreferences = childNode.compile();
            for (Map.Entry<String, T> childEntry : childPreferences.entrySet()) {
                String fullId = childId + "." + childEntry.getKey();
                map.put(fullId, childEntry.getValue());
            }
        }

        return map;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof TomlNode<?> node)) return false;

        return this.id.equals(node.id) &&
                this.children.equals(node.children) &&
                this.entries.equals(node.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, children, entries);
    }

    @Override
    public String toString() {
        return "PreferenceNode{" +
                "id='" + id + '\'' +
                ", children=" + children.keySet() +
                ", preferences=" + entries.keySet() +
                '}';
    }
}
