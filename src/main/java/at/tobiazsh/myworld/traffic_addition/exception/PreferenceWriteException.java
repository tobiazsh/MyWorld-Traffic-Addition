package at.tobiazsh.myworld.traffic_addition.exception;

public class PreferenceWriteException extends RuntimeException {
    public PreferenceWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreferenceWriteException(String message) {
        super(message);
    }
}
