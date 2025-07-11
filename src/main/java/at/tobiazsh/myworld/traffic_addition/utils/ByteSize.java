package at.tobiazsh.myworld.traffic_addition.utils;

public class ByteSize {

    public enum ByteUnitsReal {
        B, KiB, MiB, GiB, TiB, PiB, EiB, ZiB, YiB
    }

    public enum ByteUnits {
        B, KB, MB, GB, TB, PB, EB, ZB, YB
    }

    public static long convertReal(long bytes, ByteUnitsReal from, ByteUnitsReal to) {
        int conversion = from.ordinal() - to.ordinal();

        if (conversion == 0) return bytes;
        else if (conversion < 0) {
            for (int i = 0; i < -conversion; i++) {
                bytes /= 1024;
            }
        } else {
            for (int i = 0; i < conversion; i++) {
                bytes *= 1024;
            }
        }

        return bytes;
    }

    public static long convert(long bytes, ByteUnits from, ByteUnits to) {
        int conversion = from.ordinal() - to.ordinal();

        if (conversion == 0) return bytes;
        else if (conversion < 0) {
            for (int i = 0; i < -conversion; i++) {
                bytes /= 1000;
            }
        } else {
            for (int i = 0; i < conversion; i++) {
                bytes *= 1000;
            }
        }

        return bytes;
    }
}
