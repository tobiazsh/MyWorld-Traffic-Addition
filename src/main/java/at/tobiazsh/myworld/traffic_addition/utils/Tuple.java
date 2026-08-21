package at.tobiazsh.myworld.traffic_addition.utils;

public record Tuple<A, B>(A a, B b) {
    public static <A, B> Tuple<A, B> of(A a, B b) {
        return new Tuple<>(a, b);
    }
}
