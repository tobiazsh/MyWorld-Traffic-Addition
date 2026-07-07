package at.tobiazsh.myworld.traffic_addition.toml;

import java.util.Map;

public record TomlTable(Map<String, TomlValue<?>> value) implements TomlValue<Map<String, TomlValue<?>>> {}
