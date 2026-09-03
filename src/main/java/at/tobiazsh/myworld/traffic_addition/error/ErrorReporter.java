package at.tobiazsh.myworld.traffic_addition.error;

@FunctionalInterface
public interface ErrorReporter {
    void reportError(Error error, Runnable onClose);
}
