package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.TomlValue;

public class Preference<T extends TomlValue> {
    private T value;
    private final T defaultValue;
    private String id;

    public Preference(T defaultValue, String id) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.id = id;
    }

    public void setDefault() {
        this.value = defaultValue;
    }

    public void set(T value) {
        this.value = value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }
}
