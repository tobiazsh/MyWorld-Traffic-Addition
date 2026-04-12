package at.tobiazsh.myworld.traffic_addition.error;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
        byte[] titleBytes = title.getBytes(StandardCharsets.UTF_8);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(
                4 + titleBytes.length +
                        4 + messageBytes.length +
                        1
        );

        buffer.putInt(titleBytes.length);
        buffer.put(titleBytes);

        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);

        buffer.put((byte) (isHandled ? 1 : 0));
        return buffer.array();
    }

    public static Error fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int titleLength = buffer.getInt();
        byte[] titleBytes = new byte[titleLength];
        buffer.get(titleBytes);
        String title = new String(titleBytes, StandardCharsets.UTF_8);

        int messageLength = buffer.getInt();
        byte[] messageBytes = new byte[messageLength];
        buffer.get(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        boolean isHandled = buffer.get() == 1;

        Error error = new Error(title, message);
        if (isHandled) {
            error.handled();
        }
        return error;
    }
}
