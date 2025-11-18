package at.tobiazsh.myworld.traffic_addition.utils;

/**
 * Simple error class to represent errors with title and message
 */
public class Error {

    private final String title;
    private final String message;
    private boolean isHandled = false;

    public Error(String title, String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * Sets the error as handled
     * @return this
     */
    public Error handled() {
        isHandled = true;
        return this;
    }

    /**
     * Checks whether the error has been handled
     * @return true if handled, false otherwise
     */
    public boolean isHandled() {
        return isHandled;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
