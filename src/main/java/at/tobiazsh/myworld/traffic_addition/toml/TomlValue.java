package at.tobiazsh.myworld.traffic_addition.toml;

import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    T value();
}