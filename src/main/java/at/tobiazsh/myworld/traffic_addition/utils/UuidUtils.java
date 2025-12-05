package at.tobiazsh.myworld.traffic_addition.utils;

public class UuidUtils {
    public static boolean isValidUUID(String uuidString) {
        try {
            java.util.UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
