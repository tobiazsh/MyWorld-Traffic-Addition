package at.tobiazsh.myworld.traffic_addition.toml.codec;

import io.github.wasabithumb.jtoml.value.TomlValue;

import java.util.function.Function;

public final class Codec<T> {

    private final Function<TomlValue, T> deserializer;
    private final Function<T, TomlValue> serializer;

    private Codec(Function<TomlValue, T> deserializer, Function<T, TomlValue> serializer) {
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    public static <T> Codec<T> of(Function<TomlValue, T> decoder, Function<T, TomlValue> encoder) {
        return new Codec<>(decoder, encoder);
    }

    public T deserialize(TomlValue value) {
        return deserializer.apply(value);
    }

    public TomlValue serialize(T value) {
        return serializer.apply(value);
    }
}