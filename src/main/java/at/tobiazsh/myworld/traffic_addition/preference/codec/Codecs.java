package at.tobiazsh.myworld.traffic_addition.preference.codec;

import at.tobiazsh.myworld.traffic_addition.exception.DecodeException;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;

@SuppressWarnings("unused")
public final class Codecs {
    public static final Codec<Integer> INTEGER = Codec.of(
            o -> {
                if (o.isPrimitive() && o.asPrimitive().isInteger()) return o.asPrimitive().asInteger();
                throw new DecodeException("Expected Integer, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );

    public static final Codec<Boolean> BOOLEAN = Codec.of(
            o -> {
                if (o.isPrimitive() && o.asPrimitive().isBoolean()) return o.asPrimitive().asBoolean();
                throw new DecodeException("Expected Boolean, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );

    public static final Codec<String> STRING = Codec.of(
            o -> {
                if (o.isPrimitive() && o.asPrimitive().isString()) return o.asPrimitive().asString();
                throw new DecodeException("Expected String, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );

    public static final Codec<Float> FLOAT = Codec.of(
            o -> {
                if (o.isPrimitive() && o.asPrimitive().isFloat()) return o.asPrimitive().asFloat();
                throw new DecodeException("Expected Float, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );

    public static final Codec<Long> LONG = Codec.of(
            o -> {
                if (o.isPrimitive()) return o.asPrimitive().asLong(); // There's no `asLong()`, so this will be fine I guess
                throw new DecodeException("Expected Long, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );

    public static final Codec<Short> SHORT = Codec.of(
            o -> {
                if (o.isPrimitive() && o.asPrimitive().isInteger()) return (short) o.asPrimitive().asInteger(); // There's no `asShort()`, so this we'll just use integer
                throw new DecodeException("Expected Short, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );

    public static final Codec<Double> DOUBLE = Codec.of(
            o -> {
                if (o.isPrimitive() && o.asPrimitive().isFloat()) return o.asPrimitive().asDouble(); // isFloat is not a bug, it works that way too!
                throw new DecodeException("Expected Double, got: " + o.getClass().getSimpleName());
            },
            TomlPrimitive::of
    );
}
