package at.tobiazsh.myworld.traffic_addition.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Creates a BorderProperty with the specified boolean values for each side.
 */
public record BorderProperty(boolean up, boolean right, boolean down, boolean left) {

    public static final String DEFAULT = "BorderProperty{up=false, right=false, down=false, left=false}";

    /**
     * Converts the BorderProperty to a string representation. Formatted as "BorderProperty{up, right, down, left}".
     */
    @Override
    public @NotNull String toString() {
        return "BorderProperty{%s,%s,%s,%s}".formatted(up, right, down, left);
    }

    public static BorderProperty valueOf(String borderProperty) {
        String[] parts = borderProperty
                .substring(
                        borderProperty.indexOf("{") + 1,
                        borderProperty.lastIndexOf("}")
                )
                .split(",");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid BorderProperty format. Expected format: up,right,down,left");
        }

        boolean up = Boolean.parseBoolean(parts[0].trim());
        boolean right = Boolean.parseBoolean(parts[1].trim());
        boolean down = Boolean.parseBoolean(parts[2].trim());
        boolean left = Boolean.parseBoolean(parts[3].trim());

        return new BorderProperty(up, right, down, left);
    }

    /**
     * Converts the BorderProperty to a normal string representation. Formatted as "up_right_down_left".
     */
    public String toNormalString() {
        return "%s_%s_%s_%s".formatted(up, right, down, left);
    }
}
