package at.tobiazsh.myworld.traffic_addition.toml;

public sealed interface TomlValue<T> permits
        TomlArray,
        TomlBoolean,
        TomlDouble,
        TomlFloat,
        TomlInteger,
        TomlLong,
        TomlShort,
        TomlString,
        TomlTable {

    T value();
}