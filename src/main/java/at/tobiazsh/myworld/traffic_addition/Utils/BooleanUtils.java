package at.tobiazsh.myworld.traffic_addition.Utils;

public class BooleanUtils {

    public static byte toByte(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    public static boolean fromByte(byte value) {
        return value == 1;
    }
}
