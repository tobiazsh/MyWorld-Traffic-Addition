package at.tobiazsh.myworld.traffic_addition.error;

import java.nio.ByteBuffer;

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

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + title.length() + 4 + message.length() + 1);
        buffer.putInt(title.length());
        buffer.put(title.getBytes());

        buffer.putInt(message.length());
        buffer.put(message.getBytes());

        buffer.put((byte) (isHandled ? 1 : 0));
        return buffer.array();
    }

    public static Error fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int titleLength = buffer.getInt();
        byte[] titleBytes = new byte[titleLength];
        buffer.get(titleBytes);
        String title = new String(titleBytes);

        int messageLength = buffer.getInt();
        byte[] messageBytes = new byte[messageLength];
        buffer.get(messageBytes);
        String message = new String(messageBytes);

        boolean isHandled = buffer.get() == 1;

        Error error = new Error(title, message);
        if (isHandled) {
            error.handled();
        }
        return error;
    }
}
