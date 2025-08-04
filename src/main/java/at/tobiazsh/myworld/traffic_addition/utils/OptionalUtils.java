package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;

import java.util.Optional;
import java.util.function.Function;

public class OptionalUtils {
    public static <T> T getOrDefault(String key, Function<String, Optional<T>> getter, T defaultValue, String namespace) {
        return getter.apply(key).orElseGet(() -> {
            MyWorldTrafficAddition.LOGGER.error("{}: NBT key '{}' not found, using default value '{}'", namespace, key, defaultValue);
            return defaultValue;
        });
    }
}
