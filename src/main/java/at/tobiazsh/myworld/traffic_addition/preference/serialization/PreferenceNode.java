package at.tobiazsh.myworld.traffic_addition.preference.serialization;

import at.tobiazsh.myworld.traffic_addition.preference.Preference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PreferenceNode {
    private final String id;
    private final Object instance;

    private final Map<String, PreferenceNode> children = new HashMap<>();
    private final Map<String, Preference<?>> preferences = new HashMap<>();

    public PreferenceNode(String id, Object instance) {
        this.id = id;
        this.instance = instance;
    }

    public String id() {
        return id;
    }

    public Map<String, PreferenceNode> children() {
        return children;
    }

    public Map<String, Preference<?>> preferences() {
        return preferences;
    }

    /**
     * Compiles a Map of preferences with their ID. Sub-IDs are separated by a dot as per TOML standard.
     */
    public Map<String, Preference<?>> compile() {
        Map<String, Preference<?>> map = new HashMap<>(preferences);

        for (Map.Entry<String, PreferenceNode> entry : children.entrySet()) {
            String childId = entry.getKey();
            PreferenceNode childNode = entry.getValue();

            Map<String, Preference<?>> childPreferences = childNode.compile();
            for (Map.Entry<String, Preference<?>> childEntry : childPreferences.entrySet()) {
                String fullId = childId + "." + childEntry.getKey();
                map.put(fullId, childEntry.getValue());
            }
        }

        return map;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PreferenceNode node)) return false;

        return this.id.equals(node.id) &&
               this.children.equals(node.children) &&
               this.preferences.equals(node.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, children, preferences);
    }

    @Override
    public String toString() {
        return "PreferenceNode{" +
                "id='" + id + '\'' +
                ", children=" + children.keySet() +
                ", preferences=" + preferences.keySet() +
                '}';
    }
}
