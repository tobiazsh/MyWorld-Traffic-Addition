package at.tobiazsh.myworld.traffic_addition.exception;

public class PreferenceReadException extends RuntimeException {
    public PreferenceReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreferenceReadException(String message) {
        super(message);
    }
}
