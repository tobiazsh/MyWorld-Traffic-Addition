package at.tobiazsh.myworld.traffic_addition.toml;

import java.util.List;

public record TomlArray(List<TomlValue<?>> value) implements TomlValue<List<TomlValue<?>>> { }