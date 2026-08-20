package at.tobiazsh.myworld.traffic_addition.utils;

public final class Tuple<A, B> {

    private final A a;
    private final B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Tuple<A, B> of(A a, B b) {
        return new Tuple<>(a, b);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }
}
