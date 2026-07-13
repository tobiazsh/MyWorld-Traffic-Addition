package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.TomlValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Preference<T extends TomlValue<?>> {
    @Nullable private T value;
    @NonNull private final T defaultValue;
    @NonNull private String id;

    public Preference(@NonNull T defaultValue, @NonNull String id) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.id = id;
    }

    @JsonIgnore
    public @NonNull String getId() {
        return id;
    }

    /**
     * Resets the current value to the defined default value.
     */
    public void setDefault() {
        this.value = defaultValue;
    }

    /**
     * Sets the current value to the provided value.
     */
    public void set(T value) {
        this.value = value;
    }

    /**
     * Returns the defined default value.
     */
    @JsonIgnore
    public @NonNull T getDefault() {
        return defaultValue;
    }

    /**
     * Returns the current value.
     */
    @JsonValue
    public @Nullable T getValue() {
        return value;
    }

    /**
     * Returns the current value, or the default value if the current value is unset.
     */
    @JsonIgnore
    public @NonNull T getOrDefault() {
        return value != null ? value : defaultValue;
    }
}
