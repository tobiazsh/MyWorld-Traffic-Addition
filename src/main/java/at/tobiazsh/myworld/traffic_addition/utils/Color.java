package at.tobiazsh.myworld.traffic_addition.utils;

/**
 * Represents a color with alpha, red, green, and blue components.
 * @param a Alpha
 * @param r Red
 * @param g Green
 * @param b Blue
 */
public record Color(int a, int r, int g, int b) {

    public Color {
        if (isInvalid(a) || isInvalid(r) || isInvalid(g) || isInvalid(b)) {
            throw new IllegalArgumentException("Color values must be between 0 and 255");
        }
    }

    /**
     * Creates a new Color instance with full opacity
     * @param r The red value
     * @param g The green value
     * @param b The blue value
     */
    public Color(int r, int g, int b) {
        this(255, r, g, b);
    }

    /**
     * Creates a new Color instance from a single ARGB integer
     * @param rgba The RGBA integer
     */
    public Color(int rgba, boolean hasAlpha) {
        this(
            hasAlpha ? (rgba >> 24) & 0xFF : 255,
            (rgba >> 16) & 0xFF,
            (rgba >> 8)& 0xFF,
            rgba & 0xFF
        );
    }

    private static boolean isInvalid(int colorVal) {
        return colorVal < 0 || colorVal > 255;
    }

    /**
     * Converts an ARGB integer to RGBA format
     * @param rgba The ARGB integer
     * @return The RGBA integer
     */
    public static int ARGB2RGBA(int rgba) {
        return ((rgba & 0x00FFFFFF) << 8) | ((rgba >> 24) & 0xFF);
    }

    /**
     * Converts an RGBA integer to ARGB format
     * @param rgba The RGBA integer
     * @return The ARGB integer
     */
    public static int RGBA2ARGB(int rgba) {
        return ((rgba & 0xFFFFFF00) >> 8) | ((rgba & 0xFF) << 24);
    }

    /**
     * Converts the color to a hexadecimal string in the format #AARRGGBB
     * @return The hexadecimal string representation of the color
     */
    public String toHexString() {
        return String.format("#%02X%02X%02X%02X", a, r, g, b);
    }
}
