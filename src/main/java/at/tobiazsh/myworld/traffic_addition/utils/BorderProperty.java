package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a BorderProperty with the specified boolean values for each side.
 */
public record BorderProperty(
            boolean up,
            boolean right,
            boolean down,
            boolean left,

            boolean cornerUpRight,
            boolean cornerUpLeft,
            boolean cornerDownRight,
            boolean cornerDownLeft
) {

    public static final String DEFAULT = "BorderProperty{false, false, false, false}";

    /**
     * Converts the BorderProperty to a string representation. Formatted as "BorderProperty{up, right, down, left}".
     */
    @Override
    public @NotNull String toString() {
        return "BorderProperty{%s,%s,%s,%s,%s,%s,%s,%s}".formatted(
                up, right, down, left,
                cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft
        );
    }

    public static BorderProperty valueOf(String borderProperty) {
        String[] parts = borderProperty
                .substring(
                        borderProperty.indexOf("{") + 1,
                        borderProperty.lastIndexOf("}")
                )
                .split(",");

        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid BorderProperty format. Expected format: up, right, down, left, cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft");
        }

        boolean up = Boolean.parseBoolean(parts[0].trim());
        boolean right = Boolean.parseBoolean(parts[1].trim());
        boolean down = Boolean.parseBoolean(parts[2].trim());
        boolean left = Boolean.parseBoolean(parts[3].trim());

        boolean cornerUpRight = false;
        boolean cornerUpLeft = false;
        boolean cornerDownRight = false;
        boolean cornerDownLeft = false;

        if (parts.length != 8) {
            MyWorldTrafficAddition.LOGGER.error("Unable to determine corners for BorderProperty: {}! Interpreting all as false!", borderProperty);
        } else {
            cornerUpRight = Boolean.parseBoolean(parts[4].trim());
            cornerUpLeft = Boolean.parseBoolean(parts[5].trim());
            cornerDownRight = Boolean.parseBoolean(parts[6].trim());
            cornerDownLeft = Boolean.parseBoolean(parts[7].trim());
        }

        return new BorderProperty(
                up, right, down, left,
                cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft
        );
    }

    /**
     * Converts the BorderProperty to a normal string representation. Formatted as "up_right_down_left".
     */
    public String toNormalString() {
        return "%s_%s_%s_%s_%s_%s_%s_%s".formatted(
                up, right, down, left,
                cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft
        );
    }

    public String normalStringWithoutCorners() {
        return "%s_%s_%s_%s".formatted(
                up, right, down, left
        );
    }
}
