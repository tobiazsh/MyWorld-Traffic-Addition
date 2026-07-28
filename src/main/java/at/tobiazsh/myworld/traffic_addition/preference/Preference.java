package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.permission.Permission;
import at.tobiazsh.myworld.traffic_addition.toml.LeafHandler;
import at.tobiazsh.myworld.traffic_addition.toml.NodeFactory;
import at.tobiazsh.myworld.traffic_addition.toml.TomlLeaf;
import at.tobiazsh.myworld.traffic_addition.toml.TomlNode;
import at.tobiazsh.myworld.traffic_addition.toml.codec.Codec;
import at.tobiazsh.myworld.traffic_addition.toml.serialization.TomlScanner;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class Preference<T> implements TomlLeaf {

    @SuppressWarnings("unchecked")
    public static final Class<Preference<?>> LEAF_TYPE =
            (Class<Preference<?>>) (Class<?>) Preference.class;

    public static final TomlScanner<TomlNode<Preference<?>>, Preference<?>> SCANNER =
            new TomlScanner<>(
                    NodeFactory.of(TomlNode<Preference<?>>::new),
                    LeafHandler.of(
                            (node, permission) -> node.entries().put(
                                    permission.getId(),
                                    permission
                            )
                    ),
                    LEAF_TYPE
            );

    @Nullable private T value;
    @NonNull private final T defaultValue;
    @NonNull private final String id;
    @NonNull private final Codec<T> codec;

    public Preference(@NonNull T defaultValue, @NonNull String id, @NonNull Codec<T> codec) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.id = id;
        this.codec = codec;
    }

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
    public @NonNull T getDefault() {
        return defaultValue;
    }

    /**
     * Returns the current value.
     */
    public @Nullable T getValue() {
        return value;
    }

    /**
     * Returns the current value, or the default value if the current value is unset.
     */
    public @NonNull T getOrDefault() {
        return value != null ? value : defaultValue;
    }

    public @NonNull TomlValue getValueSerialized() {
        return codec.serialize(getOrDefault());
    }

    public @NonNull Codec<T> getCodec() {
        return codec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Preference<?> preference)) return false;

        return this.id.equals(preference.id) &&
                Objects.equals(getOrDefault(), preference.getOrDefault());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getOrDefault());
    }
}
