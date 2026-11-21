package at.tobiazsh.myworld.traffic_addition.utils;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}
