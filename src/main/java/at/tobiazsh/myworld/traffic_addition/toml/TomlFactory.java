package at.tobiazsh.myworld.traffic_addition.toml;

public interface TomlFactory<V, T extends TomlValue<V>> {
    T create(V value);
}
